package com.example.domain.usecase

import kotlin.math.pow

class CalculateDistanceUseCase {
    operator fun invoke(rssi: Int, txPower: Int = -59): Float {
        if (rssi == 0) return -1f
        val ratio = rssi.toDouble() / txPower
        return if (ratio < 1.0) {
            ratio.pow(10).toFloat()
        } else {
            (0.89976 * ratio.pow(7.7095) + 0.111).toFloat()
        }
    }
}
