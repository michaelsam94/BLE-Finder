package com.michael.blefinder.presentation.ui.radar

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.michael.blefinder.domain.model.BleDevice
import com.michael.blefinder.domain.model.ScanState
import com.michael.blefinder.domain.repository.AudioFeedbackRepository
import com.michael.blefinder.domain.repository.ScanLogRepository
import com.michael.blefinder.domain.repository.ScannerRepository
import com.michael.blefinder.domain.repository.TrackedDeviceRepository
import com.michael.blefinder.domain.usecase.CalculateDistanceUseCase
import com.michael.blefinder.domain.usecase.SmoothRssiUseCase
import com.michael.blefinder.domain.usecase.TrackSingleDeviceUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RadarScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun stopsAudioPingWhenRadarScreenLeavesComposition() {
        val audioRepository = FakeAudioFeedbackRepository()
        val viewModel = RadarViewModel(
            address = "AA:BB:CC:DD:EE:FF",
            trackSingleDeviceUseCase = TrackSingleDeviceUseCase(FakeScannerRepository()),
            smoothRssiUseCase = SmoothRssiUseCase(),
            calculateDistanceUseCase = CalculateDistanceUseCase(),
            audioFeedbackRepository = audioRepository,
            scanLogRepository = FakeScanLogRepository(),
            trackedDeviceRepository = FakeTrackedDeviceRepository()
        )
        val showRadar = mutableStateOf(true)

        composeRule.setContent {
            if (showRadar.value) {
                RadarScreen(
                    viewModel = viewModel,
                    onNavigateBack = {},
                    animationsEnabled = false
                )
            }
        }

        composeRule.onNodeWithContentDescription("Toggle Audio Feedback").performClick()
        composeRule.runOnIdle {
            showRadar.value = false
        }

        composeRule.waitForIdle()
        assertEquals(1, audioRepository.stopPingCalls)
    }
}

private class FakeAudioFeedbackRepository : AudioFeedbackRepository {
    private var enabled = false
    var stopPingCalls = 0

    override suspend fun startProximityPing(rssiFlow: Flow<Float>) = Unit

    override fun stopPing() {
        stopPingCalls += 1
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun isEnabled(): Boolean = enabled
}

private class FakeScannerRepository : ScannerRepository {
    override val scanState = MutableStateFlow<ScanState>(ScanState.Idle)

    override fun startScan(): Flow<BleDevice> = emptyFlow()

    override fun stopScan() = Unit
}

private class FakeScanLogRepository : ScanLogRepository {
    override suspend fun logSighting(device: BleDevice) = Unit

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
