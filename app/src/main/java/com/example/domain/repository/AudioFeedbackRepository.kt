package com.example.domain.repository

import kotlinx.coroutines.flow.Flow

interface AudioFeedbackRepository {
    suspend fun startProximityPing(rssiFlow: Flow<Float>)
    suspend fun stopPing()
    fun setEnabled(enabled: Boolean)
    fun isEnabled(): Boolean
}
