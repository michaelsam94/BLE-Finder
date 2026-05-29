package com.example.domain.repository

import com.example.domain.model.BleDevice
import com.example.domain.model.ScanState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ScannerRepository {
    val scanState: StateFlow<ScanState>
    fun startScan(): Flow<BleDevice>
    fun stopScan()
}
