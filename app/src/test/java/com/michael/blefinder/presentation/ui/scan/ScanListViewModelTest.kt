package com.michael.blefinder.presentation.ui.scan

import com.michael.blefinder.domain.model.BleDevice
import com.michael.blefinder.domain.model.DeviceType
import com.michael.blefinder.domain.model.ScanState
import com.michael.blefinder.domain.repository.ScanLogRepository
import com.michael.blefinder.domain.repository.ScannerRepository
import com.michael.blefinder.domain.repository.TrackedDeviceRepository
import com.michael.blefinder.domain.usecase.StartBulkScanUseCase
import com.michael.blefinder.domain.usecase.StopScanUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ScanListViewModelTest {

    @Test
    fun keepsKnownNameWhenLaterSightingHasNoName() = runBlocking {
        val scannerRepository = FakeScannerRepository(
            testDevice(name = "Pixel Buds", rssi = -58),
            testDevice(name = null, rssi = -61)
        )
        val scanLogRepository = RecordingScanLogRepository()
        val viewModel = ScanListViewModel(
            startBulkScanUseCase = StartBulkScanUseCase(scannerRepository),
            stopScanUseCase = StopScanUseCase(scannerRepository),
            trackedDeviceRepository = FakeTrackedDeviceRepository(),
            scanLogRepository = scanLogRepository,
            scannerRepository = scannerRepository
        )

        viewModel.startScan()
        waitUntil {
            scanLogRepository.loggedDevices.size == 2 &&
                viewModel.uiState.value.devices.containsKey(TEST_ADDRESS)
        }

        assertEquals("Pixel Buds", viewModel.uiState.value.devices.getValue(TEST_ADDRESS).name)
        assertEquals("Pixel Buds", scanLogRepository.loggedDevices.last().name)
    }

    private fun testDevice(name: String?, rssi: Int): BleDevice {
        return BleDevice(
            address = TEST_ADDRESS,
            name = name,
            rssi = rssi,
            smoothedRssi = rssi.toFloat(),
            estimatedDistance = 1.5f,
            lastSeen = 1L,
            isConnectable = true,
            deviceType = DeviceType.UNKNOWN
        )
    }

    private companion object {
        const val TEST_ADDRESS = "AA:BB:CC:DD:EE:FF"
    }
}

private fun waitUntil(predicate: () -> Boolean) {
    val deadline = System.currentTimeMillis() + 1_000L
    while (!predicate() && System.currentTimeMillis() < deadline) {
        Thread.sleep(10L)
    }
}

private class FakeScannerRepository(
    private vararg val results: BleDevice
) : ScannerRepository {
    override val scanState = MutableStateFlow<ScanState>(ScanState.Idle)

    override fun startScan(): Flow<BleDevice> = flowOf(*results)

    override fun stopScan() = Unit
}

private class RecordingScanLogRepository : ScanLogRepository {
    val loggedDevices = mutableListOf<BleDevice>()

    override suspend fun logSighting(device: BleDevice) {
        loggedDevices += device
    }

    override fun getLogsForDevice(address: String): Flow<List<BleDevice>> = emptyFlow()

    override fun getRecentLogs(since: Long): Flow<List<BleDevice>> = emptyFlow()

    override suspend fun purgeOldLogs(before: Long) = Unit
}

private class FakeTrackedDeviceRepository : TrackedDeviceRepository {
    override suspend fun upsertTrackedDevice(device: BleDevice) = Unit

    override fun getAllTrackedDevices(): Flow<List<BleDevice>> = emptyFlow()

    override fun getFavoriteDevices(): Flow<List<BleDevice>> = emptyFlow()

    override suspend fun toggleFavorite(address: String) = Unit

    override suspend fun deleteDevice(address: String) = Unit
}
