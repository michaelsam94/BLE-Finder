package com.michael.blefinder.data.repository

import com.michael.blefinder.data.local.dao.TrackedDeviceDao
import com.michael.blefinder.data.local.entity.TrackedDeviceEntity
import com.michael.blefinder.domain.model.BleDevice
import com.michael.blefinder.domain.model.DeviceType
import com.michael.blefinder.domain.repository.TrackedDeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TrackedDeviceRepositoryImpl(
    private val trackedDeviceDao: TrackedDeviceDao
) : TrackedDeviceRepository {

    override suspend fun upsertTrackedDevice(device: BleDevice) = withContext(Dispatchers.IO) {
        val existing = trackedDeviceDao.getDeviceByAddress(device.address)
        val newSightingCount = (existing?.totalSightings ?: 0) + 1
        val isFav = existing?.isFavorite ?: false

        val entity = TrackedDeviceEntity(
            address = device.address,
            name = device.name ?: existing?.name,
            lastRssi = device.rssi,
            lastSeen = System.currentTimeMillis(),
            isFavorite = isFav,
            totalSightings = newSightingCount,
            deviceType = device.deviceType.name
        )
        trackedDeviceDao.upsert(entity)
    }

    override fun getAllTrackedDevices(): Flow<List<BleDevice>> {
        return trackedDeviceDao.getAllDevices().map { entities ->
            entities.map { it.toBleDevice() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getFavoriteDevices(): Flow<List<BleDevice>> {
        return trackedDeviceDao.getFavoriteDevices().map { entities ->
            entities.map { it.toBleDevice() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun toggleFavorite(address: String) = withContext(Dispatchers.IO) {
        val device = trackedDeviceDao.getDeviceByAddress(address) ?: return@withContext
        trackedDeviceDao.setFavorite(address, !device.isFavorite)
    }

    override suspend fun deleteDevice(address: String) = withContext(Dispatchers.IO) {
        trackedDeviceDao.deleteDevice(address)
    }

    private fun TrackedDeviceEntity.toBleDevice(): BleDevice {
        val type = try {
            DeviceType.valueOf(this.deviceType)
        } catch (e: Exception) {
            DeviceType.UNKNOWN
        }

        return BleDevice(
            address = this.address,
            name = this.name,
            rssi = this.lastRssi,
            smoothedRssi = this.lastRssi.toFloat(),
            estimatedDistance = -1f,
            lastSeen = this.lastSeen,
            isConnectable = true,
            deviceType = type
        )
    }
}
