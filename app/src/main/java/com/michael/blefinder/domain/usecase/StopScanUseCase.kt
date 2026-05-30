package com.michael.blefinder.domain.usecase

import com.michael.blefinder.domain.repository.ScannerRepository

class StopScanUseCase(private val scannerRepository: ScannerRepository) {
    operator fun invoke() {
        scannerRepository.stopScan()
    }
}
