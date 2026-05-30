package com.michael.blefinder.domain.usecase

import com.michael.blefinder.domain.repository.ScanLogRepository

class PurgeOldLogsUseCase(private val scanLogRepository: ScanLogRepository) {
    suspend operator fun invoke(beforeTimestamp: Long) {
        scanLogRepository.purgeOldLogs(beforeTimestamp)
    }
}
