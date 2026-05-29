package com.example.presentation.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BleDevice
import com.example.domain.repository.ScanLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LogHistoryUiState(
    val logs: List<BleDevice> = emptyList(),
    val filterAddress: String? = null
)

class LogHistoryViewModel(
    private val scanLogRepository: ScanLogRepository
) : ViewModel() {

    private val _filterAddress = MutableStateFlow<String?>(null)

    private val recentLogsFlow = scanLogRepository.getRecentLogs(
        System.currentTimeMillis() - 12 * 60 * 60 * 1000L
    )

    val uiState: StateFlow<LogHistoryUiState> = combine(
        recentLogsFlow,
        _filterAddress
    ) { logs, filter ->
        val filteredLogs = if (filter == null) {
            logs
        } else {
            logs.filter { it.address == filter }
        }
        LogHistoryUiState(logs = filteredLogs, filterAddress = filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LogHistoryUiState()
    )

    fun filterByDevice(address: String?) {
        _filterAddress.value = address
    }

    fun purgeLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            val sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            scanLogRepository.purgeOldLogs(sevenDaysAgo)
        }
    }
}
