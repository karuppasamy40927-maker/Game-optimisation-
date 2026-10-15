package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ai.AiOptimizationResult
import com.example.data.model.InstalledGame
import com.example.engine.FpsMetrics
import com.example.engine.SystemStats
import com.example.ui.components.BatteryMonitorScreen
import com.example.ui.components.FpsGovernorScreen
import com.example.ui.components.FpsStabilityCanvas
import com.example.ui.components.GameLauncherScreen
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

@Composable
fun MainDashboardScreen(
    viewModel: GameBoosterViewModel,
    onRequestAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val systemStats by viewModel.systemStats.collectAsState()
    val batteryHealth by viewModel.batteryHealth.collectAsState()
    val fpsMetrics by viewModel.fpsMetrics.collectAsState()
    val games by viewModel.installedGames.collectAsState()
    val selectedGame by viewModel.selectedGame.collectAsState()
    val isBoosting by viewModel.isBoosting.collectAsState()
    val boostDialog by viewModel.boostDialog.collectAsState()
    val launchState by viewModel.launchState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()
    val isAiAnalyzing by viewModel.isAiAnalyzing.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var showUserMenu by remember { mutableStateOf(false) }

    // BackHandler: Navigate back to Booster dashboard if on secondary tab
    BackHandler(enabled = currentTab != BoosterNavTab.DASHBOARD) {
        viewModel.selectTab(BoosterNavTab.DASHBOARD)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(CyberBlack)) {
        val isWideScreen = maxWidth >= 720.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive NavigationRail for tablets and unfolded screens
            if (isWideScreen) {
                NavigationRail(
                    containerColor = CyberDarkSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "Logo",
                        tint = NeonCyan,
                        modifier = Modifier.size(32.dp).padding(4.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    BoosterNavTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = getTabIcon(tab),
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = CyberBlack,
                                selectedTextColor = NeonCyan,
                                indicatorColor = NeonCyan,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_rail_${tab.name}")
                        )
                    }
                }
            }

            // Main Content Area
            Scaffold(
                containerColor = CyberBlack,
                topBar = {
                    DashboardTopBar(
                        currentUserEmail = currentUser?.email,
                        isCloudSynced = currentUser != null,
                        onUserClick = {
                            if (currentUser == null) onRequestAuth() else showUserMenu = true
                        }
                    )
                },
                bottomBar = {
                    // Bottom NavigationBar for compact screens
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = CyberDarkSurface,
                            contentColor = TextPrimary,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            BoosterNavTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = getTabIcon(tab),
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CyberBlack,
                                        selectedTextColor = NeonCyan,
                                        indicatorColor = NeonCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    ),
                                    modifier = Modifier.testTag("nav_bar_${tab.name}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        // Toast status notification banner
                        AnimatedVisibility(visible = statusMessage != null, enter = fadeIn(), exit = fadeOut()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .background(PerformanceGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .border(1.dp, PerformanceGreen, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = statusMessage ?: "",
                                    color = PerformanceGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Tab Switching
                        when (currentTab) {
                            BoosterNavTab.DASHBOARD -> {
                                DashboardHomeContent(
                                    stats = systemStats,
                                    fpsMetrics = fpsMetrics,
                                    selectedGame = selectedGame,
                                    isBoosting = isBoosting,
                                    onBoost = { viewModel.performOneTapBoost() },
                                    onLaunchGame = { viewModel.launchGameWithOptimization(selectedGame) },
                                    onSelectTab = { viewModel.selectTab(it) }
                                )
                            }
                            BoosterNavTab.LAUNCHER -> {
                                GameLauncherScreen(
                                    games = games,
                                    launchState = launchState,
                                    onLaunchGame = { viewModel.launchGameWithOptimization(it) },
                                    onUpdateGameSettings = { game, fps, profile ->
                                        viewModel.selectGame(game)
                                        viewModel.setTargetFps(fps)
                                        viewModel.setPowerProfile(profile)
                                    }
                                )
                            }
                            BoosterNavTab.TELEMETRY -> {
                                FpsGovernorScreen(
                                    fpsMetrics = fpsMetrics,
                                    pingHistory = systemStats.pingHistory,
                                    currentPing = systemStats.pingMs,
                                    onSetTargetFps = { viewModel.setTargetFps(it) },
                                    onToggleGovernor = { viewModel.toggleFpsGovernor(it) }
                                )
                            }
                            BoosterNavTab.BATTERY -> {
                                BatteryMonitorScreen(
                                    battery = batteryHealth,
                                    onOptimizeBackground = { viewModel.optimizeBatteryBackgroundTasks() }
                                )
                            }
                            BoosterNavTab.AI_DOCTOR -> {
                                AiDoctorScreen(
                                    aiResult = aiResult,
                                    isAnalyzing = isAiAnalyzing,
                                    activeGame = selectedGame.appName,
                                    onRunAnalysis = { viewModel.runAiAnalysis() },
                                    onApplyAiProfile = { result ->
                                        viewModel.setTargetFps(result.recommendedFps)
                                        viewModel.setPowerProfile(result.recommendedPowerProfile)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Boost Result Dialog
    if (boostDialog.isOpen) {
        BoostResultModal(
            dialogState = boostDialog,
            onDismiss = { viewModel.dismissBoostDialog() }
        )
    }

    // User Profile Dialog
    val user = currentUser
    if (showUserMenu && user != null) {
        UserSessionDialog(
            user = user,
            onDismiss = { showUserMenu = false },
            onSignOut = {
                viewModel.signOut()
                showUserMenu = false
            }
        )
    }
}

private fun getTabIcon(tab: BoosterNavTab) = when (tab) {
    BoosterNavTab.DASHBOARD -> Icons.Default.FlashOn
    BoosterNavTab.LAUNCHER -> Icons.Default.SportsEsports
    BoosterNavTab.TELEMETRY -> Icons.Default.Speed
    BoosterNavTab.BATTERY -> Icons.Default.BatteryChargingFull
    BoosterNavTab.AI_DOCTOR -> Icons.Default.AutoAwesome
}

@Composable
fun DashboardTopBar(
    currentUserEmail: String?,
    isCloudSynced: Boolean,
    onUserClick: () -> Unit
) {
    Surface(
        color = CyberDarkSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                    bottom = 12.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "App Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "GAME BOOSTER",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "ULTRA KERNEL ENGINE",
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // User / Sync Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CyberCardSurface)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(20.dp))
                    .clickable { onUserClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("auth_chip_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCloudSynced) Icons.Default.CloudDone else Icons.Default.AccountCircle,
                        contentDescription = "User",
                        tint = if (isCloudSynced) PerformanceGreen else NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCloudSynced) (currentUserEmail?.substringBefore("@") ?: "Synced") else "Sign In",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHomeContent(
    stats: SystemStats,
    fpsMetrics: FpsMetrics,
    selectedGame: InstalledGame,
    isBoosting: Boolean,
    onBoost: () -> Unit,
    onLaunchGame: () -> Unit,
    onSelectTab: (BoosterNavTab) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ultra Boost Circular Hero Component
        UltraBoostHeroCard(
            isBoosting = isBoosting,
            ramUsedPercent = stats.ramUsedPercent,
            ramAvailableMb = stats.ramAvailableMb,
            onBoost = onBoost
        )

        // Hardware Telemetry Gauges (RAM, CPU, Battery, Ping)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryGaugeCard(
                label = "RAM USAGE",
                value = "${stats.ramUsedPercent}%",
                subtext = "${stats.ramUsedMb}MB",
                color = if (stats.ramUsedPercent > 80) DangerRed else NeonCyan,
                modifier = Modifier.weight(1f)
            )
            TelemetryGaugeCard(
                label = "CPU LOAD",
                value = "${stats.cpuLoadPercent}%",
                subtext = "Octa-Core",
                color = if (stats.cpuLoadPercent > 80) WarningAmber else TurboViolet,
                modifier = Modifier.weight(1f)
            )
            TelemetryGaugeCard(
                label = "BATTERY",
                value = "${stats.batteryPercent}%",
                subtext = String.format("%.1f°C", stats.batteryTempCelsius),
                color = PerformanceGreen,
                modifier = Modifier.weight(1f)
            )
            TelemetryGaugeCard(
                label = "PING",
                value = "${stats.pingMs}",
                subtext = "ms (Fast)",
                color = NeonCyan,
                modifier = Modifier.weight(1f)
            )
        }

        // Active Game Banner with Quick Boost & Launch
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(NeonCyan.copy(alpha = 0.15f), TurboViolet.copy(alpha = 0.15f))
                        )
                    )
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACTIVE GAME TARGET",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = selectedGame.appName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target: ${selectedGame.customFpsTarget} FPS • ${selectedGame.customPowerProfile}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onLaunchGame,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CyberBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quick_launch_active_game_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LAUNCH", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // FPS Stability Live View
        FpsStabilityCanvas(
            history = fpsMetrics.frameHistory,
            targetFps = fpsMetrics.targetFps,
            currentFps = fpsMetrics.currentFps,
            stabilityScore = fpsMetrics.stabilityScorePercent,
            modifier = Modifier.clickable { onSelectTab(BoosterNavTab.TELEMETRY) }
        )

        // Quick Shortcut Cards (Launcher & Battery Health)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberCardBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectTab(BoosterNavTab.LAUNCHER) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Game Launcher",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Open installed games with auto RAM purge",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, CyberCardBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectTab(BoosterNavTab.BATTERY) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = PerformanceGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Battery Health",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Current draw & task optimizer",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun UltraBoostHeroCard(
    isBoosting: Boolean,
    ramUsedPercent: Int,
    ramAvailableMb: Long,
    onBoost: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
        border = BorderStroke(1.dp, CyberCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SYSTEM ENGINE READY",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${ramAvailableMb} MB RAM Available",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Big 1-Tap Boost Circle Button
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(if (isBoosting) pulseScale else 1f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.35f),
                                TurboViolet.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, NeonCyan, CircleShape)
                    .clickable(enabled = !isBoosting) { onBoost() }
                    .testTag("ultra_boost_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isBoosting) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Boost",
                            tint = NeonCyan,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBoosting) "OPTIMIZING" else "ULTRA BOOST",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tap to purge background RAM cache & maximize FPS",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TelemetryGaugeCard(
    label: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
        border = BorderStroke(1.dp, CyberCardBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtext,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun AiDoctorScreen(
    aiResult: AiOptimizationResult?,
    isAnalyzing: Boolean,
    activeGame: String,
    onRunAnalysis: () -> Unit,
    onApplyAiProfile: (AiOptimizationResult) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
            border = BorderStroke(1.dp, TurboViolet.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = TurboViolet,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "GEMINI 3.8 FLASH AI DOCTOR",
                        color = TurboViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Intelligent Kernel Diagnostics",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "AI analyzes hardware telemetry, frame jitter, and battery drain for $activeGame.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onRunAnalysis,
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = TurboViolet, contentColor = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("run_ai_doctor_button")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(color = TextPrimary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Diagnosing Kernel...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RUN AI PERFORMANCE SCAN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        aiResult?.let { result ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                border = BorderStroke(1.dp, PerformanceGreen.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.title,
                            color = PerformanceGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .background(PerformanceGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Savings: ${result.powerSavingProjection}",
                                color = PerformanceGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = result.summary,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(CyberDarkSurface, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("REC. FPS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("${result.recommendedFps} FPS", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(CyberDarkSurface, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("THERMAL RISK", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(result.thermalRisk, color = PerformanceGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onApplyAiProfile(result) },
                        colors = ButtonDefaults.buttonColors(containerColor = PerformanceGreen, contentColor = CyberBlack),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("APPLY AI RECOMMENDATION", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
fun BoostResultModal(
    dialogState: BoostDialogState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CyberDarkSurface,
            border = BorderStroke(2.dp, NeonCyan),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(PerformanceGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PerformanceGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ULTRA BOOST COMPLETED",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "+${dialogState.ramFreedMb} MB RAM Purged",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(CyberCardSurface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LATENCY REDUCED", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("-${dialogState.latencyReducedMs} ms", color = NeonCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(CyberCardSurface, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FRAME RATE LOCK", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${dialogState.fpsTarget} FPS", color = PerformanceGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CyberBlack),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("DONE", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun UserSessionDialog(
    user: com.google.firebase.auth.FirebaseUser,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CyberCardSurface,
            border = BorderStroke(1.dp, CyberCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "SIGNED IN AS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.displayName ?: user.email ?: "Gamer",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email ?: "",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PerformanceGreen.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = PerformanceGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cloud Firestore profiles synchronized",
                            color = PerformanceGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = TextPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sign Out")
                    }
                }
            }
        }
    }
}
