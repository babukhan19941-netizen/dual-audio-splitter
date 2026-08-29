package com.example.model

enum class AudioMode {
    SPLIT_CHANNELS,     // Left = Media Audio, Right = Call/Phone Audio
    UNIFIED_MEDIA,      // Both Speakers = Media Audio
    LEFT_ONLY_MEDIA,    // Left Speaker = Media Only
    RIGHT_ONLY_MEDIA    // Right Speaker = Media Only
}

enum class MediaTrack(val title: String, val genre: String, val baseFreq: Float) {
    SYNTH_GROOVE("Neon Horizon", "Synthwave 120 BPM", 440f),
    CHILL_MELODY("Midnight Breeze", "Lo-Fi Beats 90 BPM", 330f),
    ACOUSTIC_CHORD("Sunset Acoustic", "Acoustic Harmony", 523.25f),
    STEREO_SWEEP("Spatial Audio Sweep", "Frequency Test 20Hz-20kHz", 220f)
}

enum class CallAudioType(val label: String, val description: String) {
    PHONE_CONVERSATION("Simulated Call", "Phone conversation voice speech"),
    CALL_RINGTONE("Incoming Ringtone", "Phone calling tone & alert"),
    MIC_MONITOR("Live Mic Monitor", "Speak into mic to hear in right ear"),
    DIAL_BEEP("Dialing Tones", "DTMF phone keypad simulation")
}
