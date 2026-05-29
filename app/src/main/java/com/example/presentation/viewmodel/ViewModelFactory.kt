package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.BleFinderApp
import com.example.presentation.ui.detail.DeviceDetailViewModel
import com.example.presentation.ui.history.LogHistoryViewModel
import com.example.presentation.ui.radar.RadarViewModel
import com.example.presentation.ui.scan.ScanListViewModel
import com.example.presentation.ui.settings.SettingsViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val context: Context,
    private val key: String? = null
) : ViewModelProvider.Factory {

    private val app = context.applicationContext as BleFinderApp
    private val container = app.appContainer

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ScanListViewModel::class.java) -> {
                ScanListViewModel(
                    container.startBulkScanUseCase,
                    container.stopScanUseCase,
                    container.trackedDeviceRepository,
                    container.scanLogRepository,
                    container.scannerRepository
                ) as T
            }
            modelClass.isAssignableFrom(RadarViewModel::class.java) -> {
                val address = checkNotNull(key) { "Address key is required for RadarViewModel" }
                RadarViewModel(
                    address,
                    container.trackSingleDeviceUseCase,
                    container.smoothRssiUseCase,
                    container.calculateDistanceUseCase,
                    container.audioFeedbackRepository,
                    container.scanLogRepository,
                    container.trackedDeviceRepository
                ) as T
            }
            modelClass.isAssignableFrom(LogHistoryViewModel::class.java) -> {
                LogHistoryViewModel(container.scanLogRepository) as T
            }
            modelClass.isAssignableFrom(DeviceDetailViewModel::class.java) -> {
                val address = checkNotNull(key) { "Address key is required for DeviceDetailViewModel" }
                DeviceDetailViewModel(
                    address,
                    container.scanLogRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    context,
                    container.audioFeedbackRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
