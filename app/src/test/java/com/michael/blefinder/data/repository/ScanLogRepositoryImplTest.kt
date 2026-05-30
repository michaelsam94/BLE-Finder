package com.michael.blefinder.data.repository

import com.michael.blefinder.data.local.dao.ScanLogDao
import com.michael.blefinder.data.local.entity.ScanLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanLogRepositoryImplTest {

    @Test
    fun recentLogsUseKnownNameForUnnamedRowsWithSameAddress() = runTest {
        val dao = FakeScanLogDao(
            recentLogs = listOf(
                scanLog(id = 2, name = null, timestamp = 2L),
                scanLog(id = 1, name = "Pixel Buds", timestamp = 1L)
            )
        )
        val repository = ScanLogRepositoryImpl(dao)

        val logs = repository.getRecentLogs(since = 0L).first()

        assertEquals("Pixel Buds", logs.first().name)
    }

    private fun scanLog(id: Long, name: String?, timestamp: Long): ScanLogEntity {
        return ScanLogEntity(
            id = id,
            deviceAddress = TEST_ADDRESS,
            deviceName = name,
            rssi = -60,
            estimatedDistanceMeters = 1.25f,
            timestamp = timestamp,
            sessionId = "test"
        )
    }

    private companion object {
        const val TEST_ADDRESS = "AA:BB:CC:DD:EE:FF"
    }
}

private class FakeScanLogDao(
    private val recentLogs: List<ScanLogEntity> = emptyList(),
    private val deviceLogs: List<ScanLogEntity> = emptyList()
) : ScanLogDao {
    override suspend fun insertLog(log: ScanLogEntity) = Unit

    override fun getLogsForDevice(address: String): Flow<List<ScanLogEntity>> = flowOf(deviceLogs)

    override fun getRecentLogs(since: Long): Flow<List<ScanLogEntity>> = flowOf(recentLogs)

    override suspend fun purgeOldLogs(before: Long) = Unit
}
