package com.example.domain.usecase

import com.example.domain.repository.ScanLogRepository

class PurgeOldLogsUseCase(private val scanLogRepository: ScanLogRepository) {
    suspend operator fun invoke(beforeTimestamp: Long) {
        scanLogRepository.purgeOldLogs(beforeTimestamp)
    }
}
