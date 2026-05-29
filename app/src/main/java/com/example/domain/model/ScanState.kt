package com.example.domain.model

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Error(val message: String) : ScanState()
}
