package com.michael.blefinder.di

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.room.Room
import com.michael.blefinder.data.audio.AudioFeedbackImpl
import com.michael.blefinder.data.bluetooth.BleScanner
import com.michael.blefinder.data.local.AppDatabase
import com.michael.blefinder.data.repository.ScanLogRepositoryImpl
import com.michael.blefinder.data.repository.TrackedDeviceRepositoryImpl
import com.michael.blefinder.domain.repository.AudioFeedbackRepository
import com.michael.blefinder.domain.repository.ScanLogRepository
import com.michael.blefinder.domain.repository.ScannerRepository
import com.michael.blefinder.domain.repository.TrackedDeviceRepository
import com.michael.blefinder.domain.usecase.*

interface AppContainer {
    val database: AppDatabase
    val scannerRepository: ScannerRepository
    val scanLogRepository: ScanLogRepository
    val trackedDeviceRepository: TrackedDeviceRepository
    val audioFeedbackRepository: AudioFeedbackRepository

    val startBulkScanUseCase: StartBulkScanUseCase
    val stopScanUseCase: StopScanUseCase
    val trackSingleDeviceUseCase: TrackSingleDeviceUseCase
    val calculateDistanceUseCase: CalculateDistanceUseCase
    val smoothRssiUseCase: SmoothRssiUseCase
    val purgeOldLogsUseCase: PurgeOldLogsUseCase
}

class AppContainerImpl(private val appContext: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        Room.databaseBuilder(appContext, AppDatabase::class.java, "ble_finder.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    private val bluetoothManager: BluetoothManager? by lazy {
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    override val scannerRepository: ScannerRepository by lazy {
        BleScanner(appContext, bluetoothManager)
    }

    override val scanLogRepository: ScanLogRepository by lazy {
        ScanLogRepositoryImpl(database.scanLogDao())
    }

    override val trackedDeviceRepository: TrackedDeviceRepository by lazy {
        TrackedDeviceRepositoryImpl(database.trackedDeviceDao())
    }

    override val audioFeedbackRepository: AudioFeedbackRepository by lazy {
        AudioFeedbackImpl()
    }

    override val startBulkScanUseCase: StartBulkScanUseCase by lazy {
        StartBulkScanUseCase(scannerRepository)
    }

    override val stopScanUseCase: StopScanUseCase by lazy {
        StopScanUseCase(scannerRepository)
    }

    override val trackSingleDeviceUseCase: TrackSingleDeviceUseCase by lazy {
        TrackSingleDeviceUseCase(scannerRepository)
    }

    override val calculateDistanceUseCase: CalculateDistanceUseCase by lazy {
        CalculateDistanceUseCase()
    }

    override val smoothRssiUseCase: SmoothRssiUseCase by lazy {
        SmoothRssiUseCase()
    }

    override val purgeOldLogsUseCase: PurgeOldLogsUseCase by lazy {
        PurgeOldLogsUseCase(scanLogRepository)
    }
}
