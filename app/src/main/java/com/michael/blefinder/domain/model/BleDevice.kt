package com.michael.blefinder.domain.model

data class BleDevice(
    val address: String,          // MAC address (stable ID)
    val name: String?,            // Advertised name or null
    val rssi: Int,                // Raw dBm value
    val smoothedRssi: Float,      // Filtered RSSI
    val estimatedDistance: Float, // Meters, calculated from RSSI
    val lastSeen: Long,           // System.currentTimeMillis()
    val isConnectable: Boolean,
    val deviceType: DeviceType    // EARBUD, WATCH, FITNESS, STYLUS, UNKNOWN
)

enum class DeviceType {
    EARBUD, WATCH, FITNESS, STYLUS, UNKNOWN
}
