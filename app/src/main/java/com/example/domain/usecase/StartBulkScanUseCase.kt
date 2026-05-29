package com.example.domain.usecase

import com.example.domain.model.BleDevice
import com.example.domain.repository.ScannerRepository
import kotlinx.coroutines.flow.Flow

class StartBulkScanUseCase(private val scannerRepository: ScannerRepository) {
    operator fun invoke(): Flow<BleDevice> {
        return scannerRepository.startScan()
    }
}
