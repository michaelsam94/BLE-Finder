package com.michael.blefinder.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.michael.blefinder.data.local.entity.TrackedDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedDeviceDao {
    @Upsert
    suspend fun upsert(device: TrackedDeviceEntity)

    @Query("SELECT * FROM tracked_devices ORDER BY lastSeen DESC")
    fun getAllDevices(): Flow<List<TrackedDeviceEntity>>

    @Query("SELECT * FROM tracked_devices WHERE isFavorite = 1")
    fun getFavoriteDevices(): Flow<List<TrackedDeviceEntity>>

    @Query("UPDATE tracked_devices SET isFavorite = :fav WHERE address = :address")
    suspend fun setFavorite(address: String, fav: Boolean)

    @Query("SELECT * FROM tracked_devices WHERE address = :address")
    suspend fun getDeviceByAddress(address: String): TrackedDeviceEntity?

    @Query("DELETE FROM tracked_devices WHERE address = :address")
    suspend fun deleteDevice(address: String)
}
