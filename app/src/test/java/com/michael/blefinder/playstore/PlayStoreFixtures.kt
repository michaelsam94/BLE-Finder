package com.michael.blefinder.playstore

import com.michael.blefinder.domain.model.BleDevice
import com.michael.blefinder.domain.model.DeviceType

object PlayStoreFixtures {
    private const val NOW = 1_779_820_800_000L

    val devices = listOf(
        BleDevice(
            address = "C8:2B:96:44:1A:0F",
            name = "Pixel Buds Pro",
            rssi = -48,
            smoothedRssi = -50f,
            estimatedDistance = 0.6f,
            lastSeen = NOW,
            isConnectable = true,
            deviceType = DeviceType.EARBUD
        ),
        BleDevice(
            address = "D1:7A:2C:80:3E:14",
            name = "Galaxy Watch",
            rssi = -63,
            smoothedRssi = -64f,
            estimatedDistance = 1.9f,
            lastSeen = NOW - 18_000L,
            isConnectable = true,
            deviceType = DeviceType.WATCH
        ),
        BleDevice(
            address = "9F:55:A0:20:BC:71",
            name = "Fitness Band",
            rssi = -76,
            smoothedRssi = -75f,
            estimatedDistance = 5.1f,
            lastSeen = NOW - 54_000L,
            isConnectable = false,
            deviceType = DeviceType.FITNESS
        ),
        BleDevice(
            address = "72:4D:12:E9:08:AA",
            name = "Smart Stylus",
            rssi = -84,
            smoothedRssi = -82f,
            estimatedDistance = 9.4f,
            lastSeen = NOW - 86_000L,
            isConnectable = true,
            deviceType = DeviceType.STYLUS
        )
    )

    val trackedDevice = devices.first()
    val logs = devices + devices.first().copy(rssi = -55, estimatedDistance = 1.1f, lastSeen = NOW - 132_000L)
}
