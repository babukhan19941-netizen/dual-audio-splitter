package com.example.model

enum class DeviceType {
    HEADPHONES,
    EARBUDS,
    SPEAKER,
    CAR_AUDIO,
    HEADSET,
    PHONE,
    GENERIC
}

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

data class BluetoothDeviceItem(
    val id: String,
    val name: String,
    val address: String,
    val deviceType: DeviceType = DeviceType.HEADPHONES,
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val batteryPercent: Int? = null,
    val isA2dpSupported: Boolean = true,
    val isHfpSupported: Boolean = true
)
