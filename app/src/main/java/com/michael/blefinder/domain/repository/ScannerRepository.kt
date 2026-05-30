package com.michael.blefinder.domain.repository

import com.michael.blefinder.domain.model.BleDevice
import com.michael.blefinder.domain.model.ScanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ScannerRepository {
    val scanState: StateFlow<ScanState>
    fun startScan(): Flow<BleDevice>
    fun stopScan()
}
