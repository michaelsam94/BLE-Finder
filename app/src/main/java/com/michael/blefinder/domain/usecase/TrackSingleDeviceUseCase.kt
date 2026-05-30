package com.michael.blefinder.domain.usecase

import com.michael.blefinder.domain.model.BleDevice
import com.michael.blefinder.domain.repository.ScannerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter

class TrackSingleDeviceUseCase(private val scannerRepository: ScannerRepository) {
    operator fun invoke(address: String): Flow<BleDevice> {
        return scannerRepository.startScan().filter { it.address == address }
    }
}
