package com.michael.blefinder.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.michael.blefinder.presentation.ui.detail.DeviceDetailScreen
import com.michael.blefinder.presentation.ui.detail.DeviceDetailViewModel
import com.michael.blefinder.presentation.ui.history.LogHistoryScreen
import com.michael.blefinder.presentation.ui.history.LogHistoryViewModel
import com.michael.blefinder.presentation.ui.radar.RadarScreen
import com.michael.blefinder.presentation.ui.radar.RadarViewModel
import com.michael.blefinder.presentation.ui.scan.ScanListScreen
import com.michael.blefinder.presentation.ui.scan.ScanListViewModel
import com.michael.blefinder.presentation.ui.settings.SettingsScreen
import com.michael.blefinder.presentation.ui.settings.SettingsViewModel
import com.michael.blefinder.presentation.viewmodel.ViewModelFactory

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = currentDestination == AppDestinations.SCAN_LIST ||
            currentDestination == AppDestinations.HISTORY ||
            currentDestination == AppDestinations.SETTINGS

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination == AppDestinations.SCAN_LIST,
                        onClick = {
                            if (currentDestination != AppDestinations.SCAN_LIST) {
                                navController.navigate(AppDestinations.SCAN_LIST) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Radar, contentDescription = "Radar") },
                        label = { Text("Radar") }
                    )
                    NavigationBarItem(
                        selected = currentDestination == AppDestinations.HISTORY,
                        onClick = {
                            if (currentDestination != AppDestinations.HISTORY) {
                                navController.navigate(AppDestinations.HISTORY) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "History") },
                        label = { Text("History") }
                    )
                    NavigationBarItem(
                        selected = currentDestination == AppDestinations.SETTINGS,
                        onClick = {
                            if (currentDestination != AppDestinations.SETTINGS) {
                                navController.navigate(AppDestinations.SETTINGS) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.SCAN_LIST,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(AppDestinations.SCAN_LIST) {
                val scanListViewModel: ScanListViewModel = viewModel(
                    factory = ViewModelFactory(context)
                )
                ScanListScreen(
                    viewModel = scanListViewModel,
                    onNavigateToRadar = { address ->
                        navController.navigate(AppDestinations.createRadarRoute(address))
                    }
                )
            }

            composable(
                route = AppDestinations.RADAR,
                arguments = listOf(navArgument("address") { type = NavType.StringType })
            ) { backStackEntry ->
                val address = backStackEntry.arguments?.getString("address") ?: ""
                val radarViewModel: RadarViewModel = viewModel(
                    factory = ViewModelFactory(context, key = address)
                )
                RadarScreen(
                    viewModel = radarViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AppDestinations.HISTORY) {
                val logHistoryViewModel: LogHistoryViewModel = viewModel(
                    factory = ViewModelFactory(context)
                )
                LogHistoryScreen(
                    viewModel = logHistoryViewModel
                )
            }

            composable(
                route = AppDestinations.DETAIL,
                arguments = listOf(navArgument("address") { type = NavType.StringType })
            ) { backStackEntry ->
                val address = backStackEntry.arguments?.getString("address") ?: ""
                val detailViewModel: DeviceDetailViewModel = viewModel(
                    factory = ViewModelFactory(context, key = address)
                )
                DeviceDetailScreen(
                    viewModel = detailViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AppDestinations.SETTINGS) {
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = ViewModelFactory(context)
                )
                SettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
