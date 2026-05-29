package com.example.presentation.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BleDevice
import com.example.domain.repository.ScanLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DeviceDetailUiState(
    val address: String = "",
    val name: String? = null,
    val logs: List<BleDevice> = emptyList(),
    val averageRssi: Float = 0f,
    val maxRssi: Int = -100,
    val minRssi: Int = -20,
    val totalSightings: Int = 0
)

class DeviceDetailViewModel(
    val address: String,
    private val scanLogRepository: ScanLogRepository
) : ViewModel() {

    val uiState: StateFlow<DeviceDetailUiState> = scanLogRepository.getLogsForDevice(address)
        .map { logs ->
            if (logs.isEmpty()) {
                DeviceDetailUiState(address = address)
            } else {
                val name = logs.firstOrNull { it.name != null }?.name
                val rssis = logs.map { it.rssi }
                val avgRssi = rssis.average().toFloat()
                val minRssi = rssis.minOrNull() ?: -100
                val maxRssi = rssis.maxOrNull() ?: -20
                
                DeviceDetailUiState(
                    address = address,
                    name = name,
                    logs = logs,
                    averageRssi = avgRssi,
                    minRssi = minRssi,
                    maxRssi = maxRssi,
                    totalSightings = logs.size
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DeviceDetailUiState(address = address)
        )
}
