package com.example.presentation.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BleDevice
import com.example.domain.model.DeviceType
import com.example.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanListScreen(
    viewModel: ScanListViewModel,
    onNavigateToRadar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    var hasPermissions by remember {
        mutableStateOf(
            requiredPermissions.all {
                context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermissions = grants.values.all { it }
        if (hasPermissions) {
            viewModel.startScan()
        }
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            viewModel.startScan()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Bluetooth Radar",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.clearDevices() }) {
                        Icon(Icons.Default.DeleteSweep, "Clear discovered")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (hasPermissions) {
                        if (uiState.isScanning) {
                            viewModel.stopScan()
                        } else {
                            viewModel.startScan()
                        }
                    } else {
                        launcher.launch(requiredPermissions)
                    }
                },
                icon = {
                    if (uiState.isScanning) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.BluetoothSearching, null)
                    }
                },
                text = {
                    Text(if (uiState.isScanning) "Stop Scanning" else "Scan Devices")
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isScanning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            PermissionAndBluetoothChecker(
                hasPermissions = hasPermissions,
                onRequestPermissions = { launcher.launch(requiredPermissions) }
            )

            FilterSection(
                selectedType = uiState.filterType,
                onSelectType = { viewModel.setFilterType(it) }
            )

            SortSection(
                selectedOrder = uiState.sortOrder,
                onSelectOrder = { viewModel.setSortOrder(it) }
            )

            val filteredSortedDevices = remember(uiState.devices, uiState.sortOrder, uiState.filterType) {
                uiState.devices.values
                    .filter { device ->
                        uiState.filterType == null || device.deviceType == uiState.filterType
                    }
                    .sortedWith { d1, d2 ->
                        when (uiState.sortOrder) {
                            SortOrder.BY_SIGNAL -> d2.rssi.compareTo(d1.rssi)
                            SortOrder.BY_NAME -> (d1.name ?: "").compareTo(d2.name ?: "")
                            SortOrder.BY_LAST_SEEN -> d2.lastSeen.compareTo(d1.lastSeen)
                        }
                    }
            }

            if (filteredSortedDevices.isEmpty()) {
                EmptyScanState(
                    isScanning = uiState.isScanning,
                    hasPermissions = hasPermissions,
                    onStartScan = {
                        if (hasPermissions) viewModel.startScan() else launcher.launch(requiredPermissions)
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredSortedDevices, key = { it.address }) { device ->
                        DeviceCardItem(
                            device = device,
                            onNavigateToRadar = onNavigateToRadar,
                            onToggleFavorite = { viewModel.toggleFavorite(device.address) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionAndBluetoothChecker(
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit
) {
    if (!hasPermissions) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.ScreenPadding),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Location & Scan Permissions Required",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Android requires Location & Bluetooth Scan permissions to discover nearby BLE signals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSection(
    selectedType: DeviceType?,
    onSelectType: (DeviceType?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Filter by Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedType == null,
                onClick = { onSelectType(null) },
                label = { Text("All") }
            )
            DeviceType.values().forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onSelectType(type) },
                    label = { Text(type.name) }
                )
            }
        }
    }
}

@Composable
fun SortSection(
    selectedOrder: SortOrder,
    onSelectOrder: (SortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Discovered Devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            SortButton(
                icon = Icons.Default.SignalCellularAlt,
                label = "Signal",
                selected = selectedOrder == SortOrder.BY_SIGNAL,
                onClick = { onSelectOrder(SortOrder.BY_SIGNAL) }
            )
            SortButton(
                icon = Icons.Default.SortByAlpha,
                label = "Name",
                selected = selectedOrder == SortOrder.BY_NAME,
                onClick = { onSelectOrder(SortOrder.BY_NAME) }
            )
            SortButton(
                icon = Icons.Default.AccessTime,
                label = "Seen",
                selected = selectedOrder == SortOrder.BY_LAST_SEEN,
                onClick = { onSelectOrder(SortOrder.BY_LAST_SEEN) }
            )
        }
    }
}

@Composable
fun SortButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DeviceCardItem(
    device: BleDevice,
    onNavigateToRadar: (String) -> Unit,
    onToggleFavorite: () -> Unit
) {
    val signalColor = when {
        device.rssi >= -60 -> Color(0xFF4CAF50)
        device.rssi >= -80 -> Color(0xFFFFB300)
        else -> Color(0xFFF44336)
    }

    val icon = when (device.deviceType) {
        DeviceType.EARBUD -> Icons.Default.Headset
        DeviceType.WATCH -> Icons.Default.Watch
        DeviceType.FITNESS -> Icons.Default.DirectionsRun
        DeviceType.STYLUS -> Icons.Default.Edit
        DeviceType.UNKNOWN -> Icons.Default.Bluetooth
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToRadar(device.address) },
        shape = RoundedCornerShape(Dimens.CornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unnamed Device",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(signalColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${device.rssi} dBm",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    if (device.estimatedDistance > 0) {
                        Text(
                            text = String.format("~%.1f meters", device.estimatedDistance),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Button(
                    onClick = { onNavigateToRadar(device.address) },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Radar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyScanState(
    isScanning: Boolean,
    hasPermissions: Boolean,
    onStartScan: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bluetooth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isScanning) "Searching for signals..." else "Scan Bluetooth Radar",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isScanning) "Keep scanning, signals can take a few seconds to register." else "Radar tracking maps and computes signal distances around you",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (!isScanning) {
                Button(onClick = onStartScan) {
                    Text("Start Scan")
                }
            }
        }
    }
}
