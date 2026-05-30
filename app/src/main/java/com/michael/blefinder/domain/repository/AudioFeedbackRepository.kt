package com.michael.blefinder.domain.repository

import kotlinx.coroutines.flow.Flow

interface AudioFeedbackRepository {
    suspend fun startProximityPing(rssiFlow: Flow<Float>)
    fun stopPing()
    fun setEnabled(enabled: Boolean)
    fun isEnabled(): Boolean
}
