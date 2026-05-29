package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_log")
data class ScanLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceAddress: String,
    val deviceName: String?,
    val rssi: Int,
    val estimatedDistanceMeters: Float,
    val timestamp: Long,
    val sessionId: String
)
