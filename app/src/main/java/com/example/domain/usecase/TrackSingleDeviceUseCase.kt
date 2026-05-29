package com.example.domain.usecase

import com.example.domain.model.BleDevice
import com.example.domain.repository.ScannerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

class TrackSingleDeviceUseCase(private val scannerRepository: ScannerRepository) {
    operator fun invoke(address: String): Flow<BleDevice> {
        return scannerRepository.startScan().filter { it.address == address }
    }
}
