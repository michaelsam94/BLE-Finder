package com.michael.blefinder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.michael.blefinder.data.local.dao.ScanLogDao
import com.michael.blefinder.data.local.dao.TrackedDeviceDao
import com.michael.blefinder.data.local.entity.ScanLogEntity
import com.michael.blefinder.data.local.entity.TrackedDeviceEntity

@Database(
    entities = [ScanLogEntity::class, TrackedDeviceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanLogDao(): ScanLogDao
    abstract fun trackedDeviceDao(): TrackedDeviceDao
}
