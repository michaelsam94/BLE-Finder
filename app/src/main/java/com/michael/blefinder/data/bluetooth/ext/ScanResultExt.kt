package com.michael.blefinder.data.bluetooth.ext

import android.bluetooth.le.ScanResult
import com.michael.blefinder.domain.model.BleDevice
import com.michael.blefinder.domain.model.DeviceType
import kotlin.math.pow

fun ScanResult.toBleDevice(): BleDevice {
    val name = this.scanRecord?.deviceName ?: this.device.name
    val rssi = this.rssi
    val address = this.device.address
    val timestamp = System.currentTimeMillis()

    val deviceType = guessDeviceType(name)
    val txPower = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val tx = this.scanRecord?.txPowerLevel ?: -59
        if (tx == Int.MIN_VALUE) -59 else tx
    } else {
        -59
    }

    val estimatedDistance = calculateDistance(rssi, txPower)

    val isConnectable = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        this.isConnectable
    } else {
        true
    }

    return BleDevice(
        address = address,
        name = name,
        rssi = rssi,
        smoothedRssi = rssi.toFloat(),
        estimatedDistance = estimatedDistance,
        lastSeen = timestamp,
        isConnectable = isConnectable,
        deviceType = deviceType
    )
}

private fun guessDeviceType(name: String?): DeviceType {
    if (name == null) return DeviceType.UNKNOWN
    val lower = name.lowercase()
    return when {
        lower.contains("bud") || lower.contains("pod") || lower.contains("ear") || 
        lower.contains("headphone") || lower.contains("audio") || lower.contains("sound") || 
        lower.contains("headset") || lower.contains("speaker") -> DeviceType.EARBUD
        
        lower.contains("watch") || lower.contains("clock") || lower.contains("gear") || 
        lower.contains("wear") -> DeviceType.WATCH
        
        lower.contains("fit") || lower.contains("band") || lower.contains("track") || 
        lower.contains("heart") || lower.contains("sport") || lower.contains("run") -> DeviceType.FITNESS
        
        lower.contains("pencil") || lower.contains("pen") || lower.contains("stylus") || 
        lower.contains("draw") -> DeviceType.STYLUS
        
        else -> DeviceType.UNKNOWN
    }
}

private fun calculateDistance(rssi: Int, txPower: Int): Float {
    if (rssi == 0) return -1f
    val ratio = rssi.toDouble() / txPower
    return if (ratio < 1.0) {
        ratio.pow(10).toFloat()
    } else {
        (0.89976 * ratio.pow(7.7095) + 0.111).toFloat()
    }
}
