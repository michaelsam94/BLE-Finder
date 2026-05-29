package com.example.presentation.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BleDevice
import com.example.domain.model.DeviceType
import com.example.domain.model.ScanState
import com.example.domain.repository.ScanLogRepository
import com.example.domain.repository.ScannerRepository
import com.example.domain.repository.TrackedDeviceRepository
import com.example.domain.usecase.StartBulkScanUseCase
import com.example.domain.usecase.StopScanUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortOrder {
    BY_SIGNAL, BY_NAME, BY_LAST_SEEN
}

data class ScanListUiState(
    val devices: Map<String, BleDevice> = emptyMap(),
    val isScanning: Boolean = false,
    val sortOrder: SortOrder = SortOrder.BY_SIGNAL,
    val filterType: DeviceType? = null,
    val error: String? = null
)

class ScanListViewModel(
    private val startBulkScanUseCase: StartBulkScanUseCase,
    private val stopScanUseCase: StopScanUseCase,
    private val trackedDeviceRepository: TrackedDeviceRepository,
    private val scanLogRepository: ScanLogRepository,
    private val scannerRepository: ScannerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanListUiState())
    val uiState: StateFlow<ScanListUiState> = _uiState.asStateFlow()

    val scanState: StateFlow<ScanState> = scannerRepository.scanState

    private var scanJob: Job? = null

    fun startScan() {
        _uiState.update { it.copy(isScanning = true, error = null) }
        scanJob?.cancel()
        scanJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                startBulkScanUseCase().collect { device ->
                    _uiState.update { state ->
                        val updated = state.devices.toMutableMap().apply {
                            put(device.address, device)
                        }
                        state.copy(devices = updated)
                    }
                    trackedDeviceRepository.upsertTrackedDevice(device)
                    scanLogRepository.logSighting(device)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false, error = e.localizedMessage) }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        stopScanUseCase()
        _uiState.update { it.copy(isScanning = false) }
    }

    fun toggleFavorite(address: String) {
        viewModelScope.launch(Dispatchers.IO) {
            trackedDeviceRepository.toggleFavorite(address)
        }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun setFilterType(type: DeviceType?) {
        _uiState.update { it.copy(filterType = type) }
    }

    fun clearDevices() {
        _uiState.update { it.copy(devices = emptyMap()) }
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}
