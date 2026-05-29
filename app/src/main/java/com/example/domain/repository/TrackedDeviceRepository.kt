package com.example.domain.repository

import com.example.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface TrackedDeviceRepository {
    suspend fun upsertTrackedDevice(device: BleDevice)
    fun getAllTrackedDevices(): Flow<List<BleDevice>>
    fun getFavoriteDevices(): Flow<List<BleDevice>>
    suspend fun toggleFavorite(address: String)
    suspend fun deleteDevice(address: String)
}
