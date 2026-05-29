package com.example.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.example.data.bluetooth.ext.toBleDevice
import com.example.domain.model.BleDevice
import com.example.domain.model.ScanState
import com.example.domain.repository.ScannerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class BleScanner(
    private val context: Context,
    private val bluetoothManager: BluetoothManager?
) : ScannerRepository {

    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    override val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private var activeCallback: ScanCallback? = null

    @SuppressLint("MissingPermission")
    override fun startScan(): Flow<BleDevice> = callbackFlow {
        val bluetoothLeScanner = adapter?.bluetoothLeScanner
        if (adapter == null || bluetoothLeScanner == null) {
            _scanState.value = ScanState.Error("Bluetooth not supported")
            close(Exception("Bluetooth not supported"))
            return@callbackFlow
        }

        if (!adapter.isEnabled) {
            _scanState.value = ScanState.Error("Bluetooth is disabled")
            close(Exception("Bluetooth is disabled"))
            return@callbackFlow
        }

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(result.toBleDevice())
            }

            override fun onBatchScanResults(results: List<ScanResult>) {
                results.forEach { result ->
                    trySend(result.toBleDevice())
                }
            }

            override fun onScanFailed(errorCode: Int) {
                _scanState.value = ScanState.Error("Scan failed with code: $errorCode")
            }
        }

        try {
            bluetoothLeScanner.startScan(null, settings, callback)
            activeCallback = callback
            _scanState.value = ScanState.Scanning
        } catch (e: Exception) {
            _scanState.value = ScanState.Error("Scan failed to start: ${e.localizedMessage}")
            close(e)
            return@callbackFlow
        }

        awaitClose {
            try {
                bluetoothLeScanner.stopScan(callback)
            } catch (e: Exception) {
                // ignore
            }
            if (activeCallback == callback) {
                activeCallback = null
            }
            _scanState.value = ScanState.Idle
        }
    }.flowOn(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    override fun stopScan() {
        val scanner = adapter?.bluetoothLeScanner ?: return
        val callback = activeCallback
        if (callback != null) {
            try {
                scanner.stopScan(callback)
            } catch (e: Exception) {
                // ignore
            }
            activeCallback = null
            _scanState.value = ScanState.Idle
        }
    }
}
