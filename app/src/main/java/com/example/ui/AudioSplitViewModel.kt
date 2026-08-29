package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioSplitEngine
import com.example.bluetooth.BluetoothAudioController
import com.example.model.AudioMode
import com.example.model.BluetoothDeviceItem
import com.example.model.CallAudioType
import com.example.model.MediaTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioSplitViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothController = BluetoothAudioController(application)
    private val audioEngine = AudioSplitEngine(application)

    val isBluetoothEnabled = bluetoothController.isBluetoothEnabled
    val pairedDevices = bluetoothController.pairedDevices
    val connectedDevice = bluetoothController.connectedDevice
    val isScanning = bluetoothController.isScanning

    private val _hasPermissions = MutableStateFlow(true)
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    private val _audioMode = MutableStateFlow(AudioMode.SPLIT_CHANNELS)
    val audioMode: StateFlow<AudioMode> = _audioMode.asStateFlow()

    private val _isMediaPlaying = MutableStateFlow(true)
    val isMediaPlaying: StateFlow<Boolean> = _isMediaPlaying.asStateFlow()

    private val _isCallPlaying = MutableStateFlow(true)
    val isCallPlaying: StateFlow<Boolean> = _isCallPlaying.asStateFlow()

    private val _isMicMonitoring = MutableStateFlow(false)
    val isMicMonitoring: StateFlow<Boolean> = _isMicMonitoring.asStateFlow()

    private val _currentTrack = MutableStateFlow(MediaTrack.SYNTH_GROOVE)
    val currentTrack: StateFlow<MediaTrack> = _currentTrack.asStateFlow()

    private val _callAudioType = MutableStateFlow(CallAudioType.PHONE_CONVERSATION)
    val callAudioType: StateFlow<CallAudioType> = _callAudioType.asStateFlow()

    private val _mediaVolume = MutableStateFlow(0.85f)
    val mediaVolume: StateFlow<Float> = _mediaVolume.asStateFlow()

    private val _callVolume = MutableStateFlow(0.85f)
    val callVolume: StateFlow<Float> = _callVolume.asStateFlow()

    private val _panBalance = MutableStateFlow(0.0f)
    val panBalance: StateFlow<Float> = _panBalance.asStateFlow()

    private val _leftAmp = MutableStateFlow(0.0f)
    val leftAmp: StateFlow<Float> = _leftAmp.asStateFlow()

    private val _rightAmp = MutableStateFlow(0.0f)
    val rightAmp: StateFlow<Float> = _rightAmp.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Wire AudioEngine callback for live visualizer meters
        audioEngine.onAmplitudeUpdated = { l, r ->
            _leftAmp.value = l
            _rightAmp.value = r
        }

        syncEngineState()
        audioEngine.startEngine(viewModelScope)
    }

    private fun syncEngineState() {
        audioEngine.audioMode = _audioMode.value
        audioEngine.currentTrack = _currentTrack.value
        audioEngine.callAudioType = _callAudioType.value
        audioEngine.isMediaPlaying = _isMediaPlaying.value
        audioEngine.isCallPlaying = _isCallPlaying.value
        audioEngine.isMicMonitoring = _isMicMonitoring.value
        audioEngine.mediaVolume = _mediaVolume.value
        audioEngine.callVolume = _callVolume.value
        audioEngine.panBalance = _panBalance.value
    }

    fun connectDevice(device: BluetoothDeviceItem) {
        bluetoothController.connectToDevice(device) { success ->
            if (success) {
                _toastMessage.value = "Connected to ${device.name}"
            } else {
                _toastMessage.value = "Failed to connect to ${device.name}"
            }
        }
    }

    fun disconnectDevice(device: BluetoothDeviceItem) {
        bluetoothController.disconnectDevice(device)
        _toastMessage.value = "Disconnected from ${device.name}"
    }

    fun refreshDevices() {
        bluetoothController.refreshPairedDevices()
    }

    /**
     * Primary feature requested by user:
     * 1-Click switch between Split Mode (Left: Media, Right: Call)
     * and Unified Media Mode (Both speakers output Media Audio).
     */
    fun toggleAudioMode() {
        val newMode = if (_audioMode.value == AudioMode.SPLIT_CHANNELS) {
            AudioMode.UNIFIED_MEDIA
        } else {
            AudioMode.SPLIT_CHANNELS
        }
        setAudioMode(newMode)
    }

    fun setAudioMode(mode: AudioMode) {
        _audioMode.value = mode
        audioEngine.audioMode = mode
        _toastMessage.value = when (mode) {
            AudioMode.SPLIT_CHANNELS -> "Split Mode: Left = Media Audio 🎵 | Right = Call Audio 📞"
            AudioMode.UNIFIED_MEDIA -> "Unified Mode: Media Audio on Both Speakers 🎶"
            AudioMode.LEFT_ONLY_MEDIA -> "Left Speaker Only Mode"
            AudioMode.RIGHT_ONLY_MEDIA -> "Right Speaker Only Mode"
        }
    }

    fun toggleMediaPlayback() {
        val next = !_isMediaPlaying.value
        _isMediaPlaying.value = next
        audioEngine.isMediaPlaying = next
    }

    fun toggleCallAudio() {
        val next = !_isCallPlaying.value
        _isCallPlaying.value = next
        audioEngine.isCallPlaying = next
    }

    fun toggleMicMonitoring() {
        val next = !_isMicMonitoring.value
        _isMicMonitoring.value = next
        audioEngine.isMicMonitoring = next
        if (next) {
            audioEngine.startMicRecord(viewModelScope)
            _toastMessage.value = "Live Mic Monitoring Active on Right Speaker"
        } else {
            audioEngine.stopMicRecord()
        }
    }

    fun selectTrack(track: MediaTrack) {
        _currentTrack.value = track
        audioEngine.currentTrack = track
    }

    fun selectCallAudioType(type: CallAudioType) {
        _callAudioType.value = type
        audioEngine.callAudioType = type
    }

    fun setMediaVolume(vol: Float) {
        _mediaVolume.value = vol
        audioEngine.mediaVolume = vol
    }

    fun setCallVolume(vol: Float) {
        _callVolume.value = vol
        audioEngine.callVolume = vol
    }

    fun setPanBalance(balance: Float) {
        _panBalance.value = balance
        audioEngine.panBalance = balance
    }

    fun testSpeaker(isLeftSpeaker: Boolean) {
        audioEngine.playSingleSpeakerVoiceTest(isLeftSpeaker, viewModelScope)
        _toastMessage.value = if (isLeftSpeaker) "Testing Left Speaker (Media)..." else "Testing Right Speaker (Call)..."
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }

    fun updatePermissionsGranted(granted: Boolean) {
        _hasPermissions.value = granted
        if (granted) {
            refreshDevices()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stopEngine()
        bluetoothController.cleanup()
    }
}
