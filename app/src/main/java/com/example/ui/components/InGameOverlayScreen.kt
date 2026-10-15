package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BatteryHealthInfo
import com.example.data.model.InstalledGame
import com.example.engine.FpsMetrics
import com.example.engine.SystemStats
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TurboViolet
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay

@Composable
fun InGameOverlayScreen(
    game: InstalledGame,
    fpsMetrics: FpsMetrics,
    battery: BatteryHealthInfo,
    systemStats: SystemStats,
    onSetTargetFps: (Int) -> Unit,
    onToggleGovernor: (Boolean) -> Unit,
    onSetPowerProfile: (String) -> Unit,
    onPurgeRam: () -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    // BackHandler: If sidebar is open, close sidebar first; otherwise exit game overlay
    var isSidebarOpen by remember { mutableStateOf(true) }
    var dndEnabled by remember { mutableStateOf(true) }
    var touchBoostEnabled by remember { mutableStateOf(true) }
    var sessionSeconds by remember { mutableIntStateOf(0) }
    var ramPurgedBanner by remember { mutableStateOf<String?>(null) }

    // Session timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            sessionSeconds++
        }
    }

    BackHandler {
        if (isSidebarOpen) {
            isSidebarOpen = false
        } else {
            onExitGame()
        }
    }

    val minutes = sessionSeconds / 60
    val seconds = sessionSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // Simulated Game Viewport
        SimulatedGameViewport(
            game = game,
            sessionTime = formattedTime,
            fps = fpsMetrics.currentFps,
            targetFps = fpsMetrics.targetFps,
            temp = battery.temperatureCelsius,
            onOpenSidebar = { isSidebarOpen = true },
            onExit = onExitGame
        )

        // Collapsed Floating Edge Handle on the right side
        if (!isSidebarOpen) {
            FloatingSidebarHandle(
                fps = fpsMetrics.currentFps,
                temp = battery.temperatureCelsius,
                profile = game.customPowerProfile,
                onClick = { isSidebarOpen = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
            )
        }

        // Full In-Game Gaming Side Bar (Slide-in from right)
        AnimatedVisibility(
            visible = isSidebarOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            GamingSideBar(
                game = game,
                sessionTime = formattedTime,
                fpsMetrics = fpsMetrics,
                battery = battery,
                systemStats = systemStats,
                dndEnabled = dndEnabled,
                touchBoostEnabled = touchBoostEnabled,
                ramPurgedBanner = ramPurgedBanner,
                onCloseSidebar = { isSidebarOpen = false },
                onSetTargetFps = onSetTargetFps,
                onToggleGovernor = onToggleGovernor,
                onSetPowerProfile = onSetPowerProfile,
                onToggleDnd = { dndEnabled = !dndEnabled },
                onToggleTouchBoost = { touchBoostEnabled = !touchBoostEnabled },
                onPurgeRam = {
                    onPurgeRam()
                    ramPurgedBanner = "RAM Purged! Background caches cleared"
                },
                onExitGame = onExitGame
            )
        }
    }
}

@Composable
fun SimulatedGameViewport(
    game: InstalledGame,
    sessionTime: String,
    fps: Int,
    targetFps: Int,
    temp: Float,
    onOpenSidebar: () -> Unit,
    onExit: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gameVisuals")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF141E30),
                        CyberBlack
                    )
                )
            )
    ) {
        // High-tech Cyber Gaming Arena Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Grid lines
            val gridStep = 40.dp.toPx()
            for (x in 0..(width / gridStep).toInt()) {
                val posX = x * gridStep
                drawLine(
                    color = NeonCyan.copy(alpha = 0.05f),
                    start = Offset(posX, 0f),
                    end = Offset(posX, height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(height / gridStep).toInt()) {
                val posY = y * gridStep
                drawLine(
                    color = TurboViolet.copy(alpha = 0.05f),
                    start = Offset(0f, posY),
                    end = Offset(width, posY),
                    strokeWidth = 1f
                )
            }

            // Central gaming radar crosshair
            drawCircle(
                color = NeonCyan.copy(alpha = 0.12f * pulse),
                radius = 120.dp.toPx(),
                center = Offset(width / 2f, height / 2f)
            )
            drawCircle(
                color = TurboViolet.copy(alpha = 0.18f),
                radius = 70.dp.toPx(),
                center = Offset(width / 2f, height / 2f)
            )
        }

        // Top Game Overlay Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberCardSurface, CircleShape)
                        .border(1.dp, CyberCardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit Game",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = game.appName,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PerformanceGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE SESSION • $sessionTime",
                            color = PerformanceGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Quick In-Game Telemetry Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .background(CyberCardSurface, RoundedCornerShape(10.dp))
                        .border(1.dp, PerformanceGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$fps FPS",
                        color = PerformanceGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(CyberCardSurface, RoundedCornerShape(10.dp))
                        .border(1.dp, if (temp >= 40f) DangerRed else NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = String.format("%.1f°C", temp),
                        color = if (temp >= 40f) DangerRed else NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Center Game Atmosphere
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.25f), TurboViolet.copy(alpha = 0.35f))),
                        CircleShape
                    )
                    .border(2.dp, NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "${game.appName} Running",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Rendering in 1080p HDR • Target ${targetFps} FPS Locked",
                color = TextSecondary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenSidebar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = CyberBlack
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("open_side_bar_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OPEN GAMING SIDE BAR",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun FloatingSidebarHandle(
    fps: Int,
    temp: Float,
    profile: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(CyberCardSurface, CyberDarkSurface)
                )
            )
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.verticalGradient(listOf(NeonCyan, TurboViolet))
                ),
                RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 12.dp)
            .testTag("floating_sidebar_handle")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Expand Side Bar",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "$fps",
                color = PerformanceGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "FPS",
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(1.dp)
                    .background(CyberCardBorder)
            )
            Text(
                text = String.format("%.0f°", temp),
                color = if (temp >= 40f) DangerRed else TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GamingSideBar(
    game: InstalledGame,
    sessionTime: String,
    fpsMetrics: FpsMetrics,
    battery: BatteryHealthInfo,
    systemStats: SystemStats,
    dndEnabled: Boolean,
    touchBoostEnabled: Boolean,
    ramPurgedBanner: String?,
    onCloseSidebar: () -> Unit,
    onSetTargetFps: (Int) -> Unit,
    onToggleGovernor: (Boolean) -> Unit,
    onSetPowerProfile: (String) -> Unit,
    onToggleDnd: () -> Unit,
    onToggleTouchBoost: () -> Unit,
    onPurgeRam: () -> Unit,
    onExitGame: () -> Unit
) {
    val scrollState = rememberScrollState()

    Surface(
        color = CyberDarkSurface,
        border = BorderStroke(1.dp, CyberCardBorder),
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(min = 300.dp, max = 340.dp)
            .testTag("in_game_side_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Side Bar Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(NeonCyan.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "GAME TURBO HUD",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = game.appName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onCloseSidebar,
                    modifier = Modifier.testTag("close_sidebar_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Collapse Side Bar",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Notification / RAM purge status banner
            ramPurgedBanner?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PerformanceGreen.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .border(1.dp, PerformanceGreen, RoundedCornerShape(10.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = message,
                        color = PerformanceGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ================= 1. FPS INDICATOR =================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = PerformanceGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FPS INDICATOR",
                                color = PerformanceGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Governor Lock indicator switch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (fpsMetrics.isGovernorLocked) "LOCKED" else "FREE",
                                color = if (fpsMetrics.isGovernorLocked) PerformanceGreen else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = fpsMetrics.isGovernorLocked,
                                onCheckedChange = onToggleGovernor,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberBlack,
                                    checkedTrackColor = PerformanceGreen,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = CyberDarkSurface
                                ),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${fpsMetrics.currentFps}",
                                color = PerformanceGreen,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = " / ${fpsMetrics.targetFps} TARGET",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                            )
                        }

                        Text(
                            text = "${fpsMetrics.stabilityScorePercent}% Steady",
                            color = PerformanceGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = String.format("Frame Time: %.1f ms • Jitter: ±%.1f ms", fpsMetrics.frameTimeMs, fpsMetrics.frameTimeVarianceMs),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target FPS Selector Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(60, 90, 120).forEach { target ->
                            val isSelected = fpsMetrics.targetFps == target
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PerformanceGreen else CyberDarkSurface)
                                    .border(1.dp, if (isSelected) PerformanceGreen else CyberCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSetTargetFps(target) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$target FPS",
                                    color = if (isSelected) CyberBlack else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ================= 2. TEMPERATURE INDICATOR =================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = if (battery.temperatureCelsius >= 40f) DangerRed else NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TEMPERATURE",
                                color = if (battery.temperatureCelsius >= 40f) DangerRed else NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (battery.temperatureCelsius >= 40f) DangerRed.copy(alpha = 0.15f) else PerformanceGreen.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (battery.temperatureCelsius >= 40f) "Throttle Risk" else "Optimal Zone",
                                color = if (battery.temperatureCelsius >= 40f) DangerRed else PerformanceGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = String.format("%.1f°C", battery.temperatureCelsius),
                            color = if (battery.temperatureCelsius >= 40f) DangerRed else TextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Text(
                            text = String.format("%.2fW • %dmA", battery.powerDrawWatts, battery.currentDrawMa),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (fpsMetrics.adaptiveThermalGovernorActive) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🛡️ Dynamic scaling (${fpsMetrics.dynamicResolutionScale}%) active to prevent stutter",
                            color = DangerRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ================= 3. PERFORMANCE INDICATOR =================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = TurboViolet,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PERFORMANCE ENGINE",
                                color = TurboViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = game.customPowerProfile,
                            color = TurboViolet,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // CPU load indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CPU LOAD", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${systemStats.cpuLoadPercent}%", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { (systemStats.cpuLoadPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .padding(vertical = 2.dp),
                        color = if (systemStats.cpuLoadPercent > 80) DangerRed else TurboViolet,
                        trackColor = CyberDarkSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // RAM usage indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("RAM USAGE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${systemStats.ramUsedMb} MB / ${systemStats.ramTotalMb} MB", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { (systemStats.ramUsedPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .padding(vertical = 2.dp),
                        color = NeonCyan,
                        trackColor = CyberDarkSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1-Tap RAM Purge in side bar
                    Button(
                        onClick = onPurgeRam,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CyberBlack),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("in_game_ram_purge_button")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PURGE BACKGROUND RAM", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Power Profile Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Beast", "Extreme", "Balanced").forEach { profName ->
                            val fullName = when (profName) {
                                "Beast" -> "Beast Performance"
                                "Extreme" -> "Extreme Performance"
                                else -> "Balanced Mode"
                            }
                            val isSelected = game.customPowerProfile.startsWith(profName)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) TurboViolet else CyberDarkSurface)
                                    .border(1.dp, if (isSelected) TurboViolet else CyberCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSetPowerProfile(fullName) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profName,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // ================= 4. GAMING UTILITIES =================
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // DND Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DoNotDisturb,
                                contentDescription = null,
                                tint = if (dndEnabled) PerformanceGreen else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Block Notifications",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = dndEnabled,
                            onCheckedChange = { onToggleDnd() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberBlack,
                                checkedTrackColor = PerformanceGreen,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CyberDarkSurface
                            ),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Touch Response Boost Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = if (touchBoostEnabled) NeonCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "360Hz Touch Sampling",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = touchBoostEnabled,
                            onCheckedChange = { onToggleTouchBoost() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberBlack,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = CyberDarkSurface
                            ),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // ================= 5. EXIT GAME BUTTON =================
            Button(
                onClick = onExitGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCardSurface,
                    contentColor = DangerRed
                ),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("exit_game_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXIT GAME MODE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
