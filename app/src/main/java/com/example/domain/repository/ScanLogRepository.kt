package com.example.domain.repository

import com.example.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface ScanLogRepository {
    suspend fun logSighting(device: BleDevice)
    fun getLogsForDevice(address: String): Flow<List<BleDevice>>
    fun getRecentLogs(since: Long): Flow<List<BleDevice>>
    suspend fun purgeOldLogs(before: Long)
}
