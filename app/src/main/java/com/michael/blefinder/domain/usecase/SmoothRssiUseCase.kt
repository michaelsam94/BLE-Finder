package com.michael.blefinder.domain.usecase

class SmoothRssiUseCase {
    operator fun invoke(rssi: Int, previousSmoothed: Float?, alpha: Float = 0.3f): Float {
        if (previousSmoothed == null) return rssi.toFloat()
        return alpha * rssi + (1f - alpha) * previousSmoothed
    }
}
