package com.example.presentation.navigation

object AppDestinations {
    const val SCAN_LIST = "scan_list"
    const val RADAR = "radar/{address}"
    const val HISTORY = "history"
    const val DETAIL = "detail/{address}"
    const val SETTINGS = "settings"
    
    fun createRadarRoute(address: String): String = "radar/$address"
    fun createDetailRoute(address: String): String = "detail/$address"
}
