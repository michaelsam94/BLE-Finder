package com.michael.blefinder.playstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michael.blefinder.domain.model.DeviceType
import com.michael.blefinder.presentation.ui.detail.LogItemCompactRow
import com.michael.blefinder.presentation.ui.detail.StatCard
import com.michael.blefinder.presentation.ui.history.LogItemRow
import com.michael.blefinder.presentation.ui.radar.RadarAnimation
import com.michael.blefinder.presentation.ui.scan.DeviceCardItem
import com.michael.blefinder.presentation.ui.scan.SortButton
import com.michael.blefinder.presentation.ui.scan.SortOrder
import com.michael.blefinder.ui.theme.MyApplicationTheme

enum class PlayStoreScene {
    Dashboard,
    Filters,
    Radar,
    Detail
}

@Composable
fun PlayStoreScreenshotFrame(scene: PlayStoreScene) {
    MyApplicationTheme(dynamicColor = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (scene) {
                PlayStoreScene.Dashboard -> DashboardScene()
                PlayStoreScene.Filters -> FiltersScene()
                PlayStoreScene.Radar -> RadarScene()
                PlayStoreScene.Detail -> DetailScene()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScene() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Bluetooth Radar", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            StatusCard()
            DeviceList(devices = PlayStoreFixtures.devices)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersScene() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Refine Signals", fontWeight = FontWeight.Bold) },
                actions = {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Focus on wearables nearby", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DeviceType.values().forEach { type ->
                            FilterChip(
                                selected = type == DeviceType.WATCH,
                                onClick = {},
                                label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SortButton(Icons.Default.BluetoothSearching, "Signal", true) {}
                        SortButton(Icons.Default.History, "Seen", false) {}
                    }
                }
            }
            DeviceList(devices = PlayStoreFixtures.devices.filter { it.deviceType == DeviceType.WATCH || it.deviceType == DeviceType.EARBUD })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RadarScene() {
    val device = PlayStoreFixtures.trackedDevice
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device.name.orEmpty(), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1B2A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1B2A))
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B))) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF4CAF50), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Radar, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Live proximity lock", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(device.address, color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                RadarAnimation(
                    smoothedRssi = device.smoothedRssi,
                    signalColor = Color(0xFFF44336),
                    animationsEnabled = false
                )
            }
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF1B263B))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Metric("Distance", "0.6 m", Color.White)
                    Metric("Signal", "-48 dBm", Color(0xFF4CAF50))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScene() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Device Detail", fontWeight = FontWeight.Bold) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(46.dp))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Pixel Buds Pro", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("C8:2B:96:44:1A:0F", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Average RSSI", "-52.6 dBm", Modifier.weight(1f))
                StatCard("Total Sightings", "128", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Min Signal", "-84 dBm", Modifier.weight(1f))
                StatCard("Max Signal", "-43 dBm", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Signal Sighting Logs", fontWeight = FontWeight.Bold)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 8.dp)) {
                items(PlayStoreFixtures.logs) { log ->
                    LogItemCompactRow(log)
                }
            }
        }
    }
}

@Composable
private fun StatusCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Scanning nearby BLE signals", fontWeight = FontWeight.Bold)
                Text("4 devices found in range", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.BluetoothSearching, contentDescription = null, modifier = Modifier.size(36.dp))
        }
    }
}

@Composable
private fun DeviceList(devices: List<com.michael.blefinder.domain.model.BleDevice>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(devices, key = { it.address }) { device ->
            DeviceCardItem(device = device, onNavigateToRadar = {}, onToggleFavorite = {})
        }
    }
}

@Composable
private fun Metric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
        Text(value, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PlayStoreFeatureGraphic() {
    MyApplicationTheme(dynamicColor = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF12343B), Color(0xFF6650A4), Color(0xFF0D1B2A))
                    )
                )
                .padding(horizontal = 64.dp, vertical = 36.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth(0.48f),
                verticalArrangement = Arrangement.Center
            ) {
                Text("BLE Finder", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Track nearby Bluetooth signals with radar-style distance cues.",
                    color = Color(0xFFE8EAED),
                    fontSize = 24.sp,
                    lineHeight = 30.sp
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(260.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Nearby signals", fontWeight = FontWeight.Black, fontSize = 22.sp)
                    FeatureDeviceRow("Pixel Buds Pro", "-48 dBm", "0.6 m", Color(0xFF4CAF50))
                    FeatureDeviceRow("Galaxy Watch", "-63 dBm", "1.9 m", Color(0xFFFFB300))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0D1B2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        RadarAnimation(
                            smoothedRssi = -52f,
                            signalColor = Color(0xFF4CAF50),
                            animationsEnabled = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureDeviceRow(name: String, signal: String, distance: String, signalColor: Color) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Headset, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(signal, color = signalColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Text(distance, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}
