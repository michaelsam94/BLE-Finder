package com.example.presentation.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.domain.repository.AudioFeedbackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ScanMode {
    LOW_POWER, BALANCED, LOW_LATENCY
}

data class SettingsUiState(
    val scanMode: ScanMode = ScanMode.LOW_LATENCY,
    val scanIntervalMs: Long = 2000L,
    val audioPingEnabled: Boolean = true,
    val logRetentionDays: Int = 7
)

class SettingsViewModel(
    context: Context,
    private val audioFeedbackRepository: AudioFeedbackRepository
) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("ble_settings", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val modeStr = sharedPrefs.getString("scan_mode", ScanMode.LOW_LATENCY.name) ?: ScanMode.LOW_LATENCY.name
        val mode = try { ScanMode.valueOf(modeStr) } catch (e: Exception) { ScanMode.LOW_LATENCY }
        val interval = sharedPrefs.getLong("scan_interval", 2000L)
        val audio = sharedPrefs.getBoolean("audio_enabled", true)
        val retention = sharedPrefs.getInt("log_retention", 7)

        _uiState.update {
            SettingsUiState(
                scanMode = mode,
                scanIntervalMs = interval,
                audioPingEnabled = audio,
                logRetentionDays = retention
            )
        }
        audioFeedbackRepository.setEnabled(audio)
    }

    fun setScanMode(mode: ScanMode) {
        sharedPrefs.edit().putString("scan_mode", mode.name).apply()
        _uiState.update { it.copy(scanMode = mode) }
    }

    fun setScanInterval(intervalMs: Long) {
        sharedPrefs.edit().putLong("scan_interval", intervalMs).apply()
        _uiState.update { it.copy(scanIntervalMs = intervalMs) }
    }

    fun toggleAudioPing(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("audio_enabled", enabled).apply()
        audioFeedbackRepository.setEnabled(enabled)
        _uiState.update { it.copy(audioPingEnabled = enabled) }
    }

    fun setLogRetentionDays(days: Int) {
        sharedPrefs.edit().putInt("log_retention", days).apply()
        _uiState.update { it.copy(logRetentionDays = days) }
    }
}
