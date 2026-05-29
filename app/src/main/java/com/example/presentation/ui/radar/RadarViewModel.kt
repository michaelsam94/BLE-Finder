package com.example.presentation.ui.radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BleDevice
import com.example.domain.repository.AudioFeedbackRepository
import com.example.domain.repository.ScanLogRepository
import com.example.domain.repository.TrackedDeviceRepository
import com.example.domain.usecase.CalculateDistanceUseCase
import com.example.domain.usecase.SmoothRssiUseCase
import com.example.domain.usecase.TrackSingleDeviceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RadarUiState(
    val device: BleDevice? = null,
    val smoothedRssi: Float = -80f,
    val distance: Float = -1f,
    val rssiHistory: List<Int> = emptyList(),
    val audioPingEnabled: Boolean = false
)

class RadarViewModel(
    val address: String,
    private val trackSingleDeviceUseCase: TrackSingleDeviceUseCase,
    private val smoothRssiUseCase: SmoothRssiUseCase,
    private val calculateDistanceUseCase: CalculateDistanceUseCase,
    private val audioFeedbackRepository: AudioFeedbackRepository,
    private val scanLogRepository: ScanLogRepository,
    private val trackedDeviceRepository: TrackedDeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadarUiState())
    val uiState: StateFlow<RadarUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null
    private val rssiList = mutableListOf<Int>()
    private var lastSmoothed: Float? = null

    init {
        _uiState.update { it.copy(audioPingEnabled = audioFeedbackRepository.isEnabled()) }
        startTracking()
    }

    private fun startTracking() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch(Dispatchers.IO) {
            trackSingleDeviceUseCase(address)
                .conflate()
                .collect { rawDevice ->
                    val smoothed = smoothRssiUseCase(rawDevice.rssi, lastSmoothed, alpha = 0.3f)
                    lastSmoothed = smoothed
                    
                    val distance = calculateDistanceUseCase(smoothed.toInt(), -59)

                    rssiList.add(rawDevice.rssi)
                    if (rssiList.size > 20) {
                        rssiList.removeAt(0)
                    }

                    val updatedDevice = rawDevice.copy(
                        smoothedRssi = smoothed,
                        estimatedDistance = distance
                    )

                    _uiState.update {
                        it.copy(
                            device = updatedDevice,
                            smoothedRssi = smoothed,
                            distance = distance,
                            rssiHistory = rssiList.toList()
                        )
                    }

                    trackedDeviceRepository.upsertTrackedDevice(updatedDevice)
                    scanLogRepository.logSighting(updatedDevice)
                }
        }
    }

    fun toggleAudioPing() {
        val nextEnabled = !uiState.value.audioPingEnabled
        audioFeedbackRepository.setEnabled(nextEnabled)
        _uiState.update { it.copy(audioPingEnabled = nextEnabled) }

        viewModelScope.launch(Dispatchers.IO) {
            if (nextEnabled) {
                audioFeedbackRepository.startProximityPing(
                    uiState.map { it.smoothedRssi }
                )
            } else {
                audioFeedbackRepository.stopPing()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            audioFeedbackRepository.stopPing()
        }
    }
}
