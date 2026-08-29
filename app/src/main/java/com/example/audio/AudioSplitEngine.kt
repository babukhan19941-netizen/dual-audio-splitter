package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import com.example.model.AudioMode
import com.example.model.CallAudioType
import com.example.model.MediaTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class AudioSplitEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var audioTrack: AudioTrack? = null
    private var audioRecord: AudioRecord? = null

    private val sampleRate = 44100
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_STEREO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    private val isRunning = AtomicBoolean(false)
    private var playbackJob: Job? = null
    private var micRecordJob: Job? = null

    // State parameters
    var audioMode: AudioMode = AudioMode.SPLIT_CHANNELS
    var currentTrack: MediaTrack = MediaTrack.SYNTH_GROOVE
    var callAudioType: CallAudioType = CallAudioType.PHONE_CONVERSATION

    var isMediaPlaying: Boolean = false
    var isCallPlaying: Boolean = false
    var isMicMonitoring: Boolean = false

    var mediaVolume: Float = 0.85f
    var callVolume: Float = 0.85f
    var panBalance: Float = 0.0f // -1.0 (Full Left) to +1.0 (Full Right)

    // Amplitudes for VU Visualizers (0.0f to 1.0f)
    var onAmplitudeUpdated: ((leftAmp: Float, rightAmp: Float) -> Unit)? = null

    // Mic buffer
    private val micBuffer = ShortArray(2048)
    private var latestMicSample: Short = 0

    fun startEngine(scope: CoroutineScope) {
        if (isRunning.getAndSet(true)) return

        try {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        playbackJob = scope.launch(Dispatchers.Default) {
            runAudioLoop()
        }
    }

    fun stopEngine() {
        isRunning.set(false)
        stopMicRecord()
        playbackJob?.cancel()
        playbackJob = null

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }

    private fun runAudioLoop() {
        val frameCount = 1024
        val stereoBuffer = ShortArray(frameCount * 2)

        var mediaPhase = 0.0
        var mediaPhase2 = 0.0
        var mediaBeatTick = 0
        var callPhase = 0.0
        var callFormantPhase = 0.0
        var callSpeechTick = 0

        var reportCounter = 0

        while (isRunning.get()) {
            var sumSquareLeft = 0.0
            var sumSquareRight = 0.0

            for (i in 0 until frameCount) {
                // 1. Generate Media Sample (Rich Synth/Music)
                var rawMedia = 0.0
                if (isMediaPlaying) {
                    val baseFreq = currentTrack.baseFreq
                    val tempoMod = (mediaBeatTick / (sampleRate / 2)) % 4
                    val chordNote = when (tempoMod) {
                        0 -> baseFreq
                        1 -> baseFreq * 1.2599f // Major third
                        2 -> baseFreq * 1.4983f // Fifth
                        else -> baseFreq * 1.8877f // Seventh
                    }

                    // Rich multi-oscillator synth
                    val osc1 = sin(mediaPhase)
                    val osc2 = sin(mediaPhase2) * 0.5
                    val beatPulse = (1.0 - (mediaBeatTick % (sampleRate / 4)).toDouble() / (sampleRate / 4)).coerceIn(0.0, 1.0)
                    val rhythmAcc = sin(mediaPhase * 0.5) * beatPulse * 0.3

                    rawMedia = (osc1 * 0.6 + osc2 * 0.4 + rhythmAcc) * 0.7

                    val phaseIncrement = (2.0 * PI * chordNote) / sampleRate
                    val phaseIncrement2 = (2.0 * PI * (chordNote * 1.5)) / sampleRate
                    mediaPhase = (mediaPhase + phaseIncrement) % (2.0 * PI)
                    mediaPhase2 = (mediaPhase2 + phaseIncrement2) % (2.0 * PI)

                    mediaBeatTick++
                    if (mediaBeatTick > sampleRate * 60) mediaBeatTick = 0
                }

                // 2. Generate Call / Voice Sample
                var rawCall = 0.0
                if (isMicMonitoring) {
                    rawCall = (latestMicSample / 32768.0) * 1.5
                } else if (isCallPlaying) {
                    when (callAudioType) {
                        CallAudioType.PHONE_CONVERSATION -> {
                            // Synthesize voice formant speech cadence (vocal tract simulation)
                            val speechCycle = (callSpeechTick % (sampleRate * 3))
                            val isSpeaking = speechCycle < (sampleRate * 2.2) // Pause between sentences
                            if (isSpeaking) {
                                val pitch = 160.0 + 30.0 * sin((callSpeechTick.toDouble() / sampleRate) * 8.0)
                                val formant1 = 800.0 + 200.0 * sin((callSpeechTick.toDouble() / sampleRate) * 3.0)
                                val formant2 = 2200.0

                                val voiceFundamental = sin(callPhase)
                                val f1 = sin(callFormantPhase) * 0.4
                                val f2 = sin(callFormantPhase * 2.75) * 0.2
                                rawCall = (voiceFundamental * 0.4 + f1 + f2) * 0.65

                                callPhase = (callPhase + (2.0 * PI * pitch) / sampleRate) % (2.0 * PI)
                                callFormantPhase = (callFormantPhase + (2.0 * PI * formant1) / sampleRate) % (2.0 * PI)
                            }
                            callSpeechTick++
                            if (callSpeechTick > sampleRate * 30) callSpeechTick = 0
                        }
                        CallAudioType.CALL_RINGTONE -> {
                            // Standard phone chime ring sequence (440Hz + 480Hz dual tone)
                            val ringSec = (callSpeechTick % (sampleRate * 3))
                            if (ringSec < (sampleRate * 1.2)) {
                                val ringTone1 = sin(callPhase)
                                val ringTone2 = sin(callFormantPhase)
                                rawCall = (ringTone1 * 0.5 + ringTone2 * 0.5) * 0.7
                                callPhase = (callPhase + (2.0 * PI * 440.0) / sampleRate) % (2.0 * PI)
                                callFormantPhase = (callFormantPhase + (2.0 * PI * 480.0) / sampleRate) % (2.0 * PI)
                            }
                            callSpeechTick++
                        }
                        CallAudioType.DIAL_BEEP -> {
                            val beepSec = (callSpeechTick % (sampleRate / 2))
                            if (beepSec < (sampleRate / 4)) {
                                rawCall = sin(callPhase) * 0.6
                                callPhase = (callPhase + (2.0 * PI * 697.0) / sampleRate) % (2.0 * PI)
                            }
                            callSpeechTick++
                        }
                        CallAudioType.MIC_MONITOR -> {
                            rawCall = (latestMicSample / 32768.0) * 1.5
                        }
                    }
                }

                // 3. Channel Routing & Splitting
                var leftOut = 0.0
                var rightOut = 0.0

                when (audioMode) {
                    AudioMode.SPLIT_CHANNELS -> {
                        // Core requirement: Left speaker = Media Audio, Right speaker = Call Phone Audio
                        val panLeftWeight = (1.0f - panBalance).coerceIn(0.0f, 1.0f)
                        val panRightWeight = (1.0f + panBalance).coerceIn(0.0f, 1.0f)

                        leftOut = rawMedia * mediaVolume * panLeftWeight
                        rightOut = rawCall * callVolume * panRightWeight
                    }
                    AudioMode.UNIFIED_MEDIA -> {
                        // User button click switch: Both speakers = Media Audio
                        val panLeftWeight = (1.0f - panBalance).coerceIn(0.0f, 1.0f)
                        val panRightWeight = (1.0f + panBalance).coerceIn(0.0f, 1.0f)

                        leftOut = rawMedia * mediaVolume * panLeftWeight
                        rightOut = rawMedia * mediaVolume * panRightWeight
                    }
                    AudioMode.LEFT_ONLY_MEDIA -> {
                        leftOut = rawMedia * mediaVolume
                        rightOut = 0.0
                    }
                    AudioMode.RIGHT_ONLY_MEDIA -> {
                        leftOut = 0.0
                        rightOut = rawMedia * mediaVolume
                    }
                }

                // Clamp to PCM 16-bit range
                val leftShort = (leftOut.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()
                val rightShort = (rightOut.coerceIn(-1.0, 1.0) * 32767).toInt().toShort()

                stereoBuffer[i * 2] = leftShort
                stereoBuffer[i * 2 + 1] = rightShort

                sumSquareLeft += (leftOut * leftOut)
                sumSquareRight += (rightOut * rightOut)
            }

            audioTrack?.write(stereoBuffer, 0, stereoBuffer.size)

            reportCounter++
            if (reportCounter >= 4) {
                reportCounter = 0
                val rmsLeft = (kotlin.math.sqrt(sumSquareLeft / frameCount)).toFloat().coerceIn(0f, 1f)
                val rmsRight = (kotlin.math.sqrt(sumSquareRight / frameCount)).toFloat().coerceIn(0f, 1f)
                onAmplitudeUpdated?.invoke(rmsLeft, rmsRight)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startMicRecord(scope: CoroutineScope) {
        if (micRecordJob != null) return
        try {
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf
            )

            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                audioRecord?.startRecording()
                micRecordJob = scope.launch(Dispatchers.IO) {
                    while (isActive && isMicMonitoring) {
                        val read = audioRecord?.read(micBuffer, 0, micBuffer.size) ?: 0
                        if (read > 0) {
                            var maxAmp: Short = 0
                            for (j in 0 until read) {
                                if (abs(micBuffer[j].toInt()) > abs(maxAmp.toInt())) {
                                    maxAmp = micBuffer[j]
                                }
                            }
                            latestMicSample = maxAmp
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopMicRecord() {
        micRecordJob?.cancel()
        micRecordJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
        latestMicSample = 0
    }

    fun playSingleSpeakerVoiceTest(isLeftSpeaker: Boolean, scope: CoroutineScope) {
        // Quick tone to confirm speaker isolation
        scope.launch(Dispatchers.Default) {
            val count = 22050 // 0.5s tone
            val testBuf = ShortArray(count * 2)
            val freq = if (isLeftSpeaker) 880.0 else 440.0
            for (i in 0 until count) {
                val s = (sin(2.0 * PI * freq * i / sampleRate) * 28000).toInt().toShort()
                if (isLeftSpeaker) {
                    testBuf[i * 2] = s
                    testBuf[i * 2 + 1] = 0
                } else {
                    testBuf[i * 2] = 0
                    testBuf[i * 2 + 1] = s
                }
            }
            audioTrack?.write(testBuf, 0, testBuf.size)
        }
    }
}
