package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.AuthScreen
import com.example.ui.GameBoosterViewModel
import com.example.ui.MainDashboardScreen
import com.example.ui.components.InGameOverlayScreen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.GameBoosterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameBoosterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GameBoosterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBlack
                ) {
                    var showAuthScreen by remember { mutableStateOf(false) }
                    val activeGame by viewModel.activeRunningGame.collectAsState()
                    val fpsMetrics by viewModel.fpsMetrics.collectAsState()
                    val batteryHealth by viewModel.batteryHealth.collectAsState()
                    val systemStats by viewModel.systemStats.collectAsState()

                    Crossfade(
                        targetState = Triple(showAuthScreen, activeGame != null, activeGame),
                        label = "screenTransition"
                    ) { (isAuth, isGameRunning, game) ->
                        when {
                            isAuth -> {
                                AuthScreen(
                                    onSignedIn = { user ->
                                        viewModel.handleUserSignedIn(user)
                                        showAuthScreen = false
                                    },
                                    onSkipGuest = {
                                        showAuthScreen = false
                                    }
                                )
                            }
                            isGameRunning && game != null -> {
                                InGameOverlayScreen(
                                    game = game,
                                    fpsMetrics = fpsMetrics,
                                    battery = batteryHealth,
                                    systemStats = systemStats,
                                    onSetTargetFps = { viewModel.setTargetFps(it) },
                                    onToggleGovernor = { viewModel.toggleFpsGovernor(it) },
                                    onSetPowerProfile = { viewModel.setPowerProfile(it) },
                                    onPurgeRam = { viewModel.purgeRamImmediate() },
                                    onExitGame = { viewModel.exitGameOverlay() }
                                )
                            }
                            else -> {
                                MainDashboardScreen(
                                    viewModel = viewModel,
                                    onRequestAuth = { showAuthScreen = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
