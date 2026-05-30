package com.michael.blefinder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.michael.blefinder.data.local.entity.ScanLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ScanLogEntity)

    @Query("SELECT * FROM scan_log WHERE deviceAddress = :address ORDER BY timestamp DESC LIMIT 100")
    fun getLogsForDevice(address: String): Flow<List<ScanLogEntity>>

    @Query("SELECT * FROM scan_log WHERE timestamp > :since ORDER BY timestamp DESC")
    fun getRecentLogs(since: Long): Flow<List<ScanLogEntity>>

    @Query("DELETE FROM scan_log WHERE timestamp < :before")
    suspend fun purgeOldLogs(before: Long)
}
