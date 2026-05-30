package com.michael.blefinder.presentation.ui.radar

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarScreen(
    viewModel: RadarViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    animationsEnabled: Boolean = true
) {
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.stopAudioPing()
        }
    }
    
    val animatedDistance by animateFloatAsState(
        targetValue = if (uiState.distance > 0) uiState.distance else 15f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "distance"
    )

    val animatedRssi by animateFloatAsState(
        targetValue = uiState.smoothedRssi,
        animationSpec = tween(500),
        label = "rssi"
    )

    val signalColor = when {
        uiState.smoothedRssi >= -60f -> Color(0xFFF44336) // Close - Red
        uiState.smoothedRssi >= -80f -> Color(0xFFFFC107) // Medium - Amber
        else -> Color(0xFF1E88E5) // Far - Blue
    }

    val proximityText = when {
        uiState.smoothedRssi >= -60f -> "HOT (Very Close)"
        uiState.smoothedRssi >= -80f -> "WARM (In Range)"
        else -> "COLD (Far / Out of View)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.device?.name ?: "Tracking Device", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1B2A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAudioPing() },
                shape = CircleShape,
                containerColor = if (uiState.audioPingEnabled) Color(0xFF4CAF50) else Color(0xFFF44336)
            ) {
                Icon(
                    imageVector = if (uiState.audioPingEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    contentDescription = "Toggle Audio Feedback",
                    tint = Color.White
                )
            }
        },
        modifier = modifier
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
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.device?.name ?: "Unnamed BLE Peripheral",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "MAC: ${viewModel.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
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
                    smoothedRssi = animatedRssi,
                    signalColor = signalColor,
                    animationsEnabled = animationsEnabled
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = proximityText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = signalColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Estimated Distance", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = String.format("%.1f m", animatedDistance),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color.Gray)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Signal Strength", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = "${uiState.device?.rssi ?: 0} dBm",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.audioPingEnabled) "Audio Ping Active (Pitch dynamic to RSSI)" else "Audio feedback disabled. Enable via FAB.",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.audioPingEnabled) Color.Green else Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun RadarAnimation(
    smoothedRssi: Float,
    signalColor: Color,
    animationsEnabled: Boolean = true
) {
    val pulseDuration = when {
        smoothedRssi >= -60f -> 400
        smoothedRssi >= -80f -> 1000
        else -> 2000
    }

    val sweepAngle: Float
    val pulseScale: Float
    if (animationsEnabled) {
        val infiniteSweep = rememberInfiniteTransition(label = "sweep")
        sweepAngle = infiniteSweep.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseDuration * 2, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "sweep"
        ).value

        val infinitePulse = rememberInfiniteTransition(label = "pulse")
        pulseScale = infinitePulse.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseDuration, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulse"
        ).value
    } else {
        sweepAngle = 310f
        pulseScale = 0.72f
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerOffset = this.center
        val maxRadius = (this.size.minDimension / 2f) * 0.9f

        val ring1Radius = maxRadius * 0.33f
        val ring2Radius = maxRadius * 0.66f
        val ring3Radius = maxRadius

        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = ring1Radius,
            center = centerOffset,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = ring2Radius,
            center = centerOffset,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.05f),
            radius = ring3Radius,
            center = centerOffset,
            style = Stroke(width = 2.dp.toPx())
        )

        drawCircle(
            color = signalColor.copy(alpha = 0.15f * (1f - pulseScale)),
            radius = maxRadius * pulseScale,
            center = centerOffset
        )

        val rad = Math.toRadians(sweepAngle.toDouble())
        val targetX = centerOffset.x + maxRadius * cos(rad).toFloat()
        val targetY = centerOffset.y + maxRadius * sin(rad).toFloat()

        drawLine(
            color = signalColor.copy(alpha = 0.3f),
            start = centerOffset,
            end = androidx.compose.ui.geometry.Offset(targetX, targetY),
            strokeWidth = 3.dp.toPx()
        )

        drawCircle(
            color = signalColor,
            radius = 16.dp.toPx(),
            center = centerOffset
        )
        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = centerOffset
        )
    }
}
