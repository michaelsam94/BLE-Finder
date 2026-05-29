package com.example.domain.usecase

import com.example.domain.repository.ScannerRepository

class StopScanUseCase(private val scannerRepository: ScannerRepository) {
    operator fun invoke() {
        scannerRepository.stopScan()
    }
}
