package com.example.data.repository

import com.example.data.local.dao.ScanLogDao
import com.example.data.local.entity.ScanLogEntity
import com.example.domain.model.BleDevice
import com.example.domain.model.DeviceType
import com.example.domain.repository.ScanLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ScanLogRepositoryImpl(
    private val scanLogDao: ScanLogDao
) : ScanLogRepository {

    private val sessionId = UUID.randomUUID().toString()
    private val lastLogTimes = ConcurrentHashMap<String, Long>()

    override suspend fun logSighting(device: BleDevice) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val lastTime = lastLogTimes[device.address] ?: 0L
        if (now - lastTime >= 2000L) {
            val entity = ScanLogEntity(
                deviceAddress = device.address,
                deviceName = device.name,
                rssi = device.rssi,
                estimatedDistanceMeters = device.estimatedDistance,
                timestamp = now,
                sessionId = sessionId
            )
            scanLogDao.insertLog(entity)
            lastLogTimes[device.address] = now
        }
    }

    override fun getLogsForDevice(address: String): Flow<List<BleDevice>> {
        return scanLogDao.getLogsForDevice(address).map { entities ->
            entities.map { it.toBleDevice() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getRecentLogs(since: Long): Flow<List<BleDevice>> {
        return scanLogDao.getRecentLogs(since).map { entities ->
            entities.map { it.toBleDevice() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun purgeOldLogs(before: Long) = withContext(Dispatchers.IO) {
        scanLogDao.purgeOldLogs(before)
    }

    private fun ScanLogEntity.toBleDevice(): BleDevice {
        return BleDevice(
            address = this.deviceAddress,
            name = this.deviceName,
            rssi = this.rssi,
            smoothedRssi = this.rssi.toFloat(),
            estimatedDistance = this.estimatedDistanceMeters,
            lastSeen = this.timestamp,
            isConnectable = true,
            deviceType = DeviceType.UNKNOWN
        )
    }
}
