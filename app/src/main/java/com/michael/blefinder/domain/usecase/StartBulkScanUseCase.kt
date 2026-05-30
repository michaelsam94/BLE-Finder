package com.michael.blefinder.domain.usecase

import com.michael.blefinder.domain.model.BleDevice
import com.michael.blefinder.domain.repository.ScannerRepository
import kotlinx.coroutines.flow.Flow

class StartBulkScanUseCase(private val scannerRepository: ScannerRepository) {
    operator fun invoke(): Flow<BleDevice> {
        return scannerRepository.startScan()
    }
}
