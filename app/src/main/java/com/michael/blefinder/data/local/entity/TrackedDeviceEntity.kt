package com.michael.blefinder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_devices")
data class TrackedDeviceEntity(
    @PrimaryKey val address: String,
    val name: String?,
    val lastRssi: Int,
    val lastSeen: Long,
    val isFavorite: Boolean,
    val totalSightings: Int,
    val deviceType: String // DeviceType enum name
)
