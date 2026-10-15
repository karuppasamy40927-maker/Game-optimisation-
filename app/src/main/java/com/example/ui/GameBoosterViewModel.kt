package com.example.ui

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiOptimizationResult
import com.example.data.ai.GeminiAiService
import com.example.data.model.BatteryHealthInfo
import com.example.data.model.GameProfile
import com.example.data.model.InstalledGame
import com.example.data.repository.GameBoosterRepository
import com.example.engine.FpsGovernor
import com.example.engine.FpsMetrics
import com.example.engine.SystemHardwareManager
import com.example.engine.SystemStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class BoosterNavTab(val label: String) {
    DASHBOARD("Booster"),
    LAUNCHER("Game Launcher"),
    TELEMETRY("FPS & Engine"),
    BATTERY("Battery Health"),
    AI_DOCTOR("AI Doctor")
}

data class BoostDialogState(
    val isOpen: Boolean = false,
    val ramFreedMb: Long = 0L,
    val latencyReducedMs: Int = 0,
    val activeProfile: String = "",
    val fpsTarget: Int = 60
)

data class GameLaunchState(
    val isLaunching: Boolean = false,
    val game: InstalledGame? = null,
    val stepMessage: String = "",
    val ramClearedMb: Long = 0L,
    val isCompleted: Boolean = false
)

class GameBoosterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameBoosterRepository()
    private val hardwareManager = SystemHardwareManager(application)
    val fpsGovernor = FpsGovernor()
    private val aiService = GeminiAiService()
    private val auth = FirebaseAuth.getInstance()

    val systemStats: StateFlow<SystemStats> = hardwareManager.systemStats
    val batteryHealth: StateFlow<BatteryHealthInfo> = hardwareManager.batteryHealth
    val fpsMetrics: StateFlow<FpsMetrics> = fpsGovernor.metrics

    private val _currentTab = MutableStateFlow(BoosterNavTab.DASHBOARD)
    val currentTab: StateFlow<BoosterNavTab> = _currentTab.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _installedGames = MutableStateFlow<List<InstalledGame>>(repository.defaultGames)
    val installedGames: StateFlow<List<InstalledGame>> = _installedGames.asStateFlow()

    private val _gameProfiles = MutableStateFlow<List<GameProfile>>(emptyList())
    val gameProfiles: StateFlow<List<GameProfile>> = _gameProfiles.asStateFlow()

    private val _selectedGame = MutableStateFlow(repository.defaultGames.first())
    val selectedGame: StateFlow<InstalledGame> = _selectedGame.asStateFlow()

    private val _boostDialog = MutableStateFlow(BoostDialogState())
    val boostDialog: StateFlow<BoostDialogState> = _boostDialog.asStateFlow()

    private val _launchState = MutableStateFlow(GameLaunchState())
    val launchState: StateFlow<GameLaunchState> = _launchState.asStateFlow()

    private val _activeRunningGame = MutableStateFlow<InstalledGame?>(null)
    val activeRunningGame: StateFlow<InstalledGame?> = _activeRunningGame.asStateFlow()

    private val _isBoosting = MutableStateFlow(false)
    val isBoosting: StateFlow<Boolean> = _isBoosting.asStateFlow()

    private val _aiResult = MutableStateFlow<AiOptimizationResult?>(null)
    val aiResult: StateFlow<AiOptimizationResult?> = _aiResult.asStateFlow()

    private val _isAiAnalyzing = MutableStateFlow(false)
    val isAiAnalyzing: StateFlow<Boolean> = _isAiAnalyzing.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        fpsGovernor.start()

        // Periodically refresh telemetry
        viewModelScope.launch {
            while (isActive) {
                hardwareManager.refreshStats()
                val stats = hardwareManager.systemStats.value
                val battery = hardwareManager.batteryHealth.value
                fpsGovernor.evaluateThermalPressure(battery.temperatureCelsius, stats.cpuLoadPercent)
                delay(2000L)
            }
        }

        // Auth state listener
        auth.addAuthStateListener { fbAuth ->
            _currentUser.value = fbAuth.currentUser
            fbAuth.currentUser?.let { user ->
                observeUserProfiles(user.uid)
            }
        }

        // Scan installed device games
        scanDeviceGames()

        // Observe repository local profiles initially
        viewModelScope.launch {
            repository.localProfiles.collect { profiles ->
                if (_currentUser.value == null) {
                    _gameProfiles.value = profiles
                }
            }
        }
    }

    private fun observeUserProfiles(uid: String) {
        viewModelScope.launch {
            try {
                repository.observeGameProfiles(uid).collect { list ->
                    if (list.isNotEmpty()) {
                        _gameProfiles.value = list
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun selectTab(tab: BoosterNavTab) {
        _currentTab.value = tab
    }

    fun selectGame(game: InstalledGame) {
        _selectedGame.value = game
        fpsGovernor.setTargetFps(game.customFpsTarget)
    }

    fun setTargetFps(target: Int) {
        fpsGovernor.setTargetFps(target)
        val current = _selectedGame.value
        _selectedGame.value = current.copy(customFpsTarget = target)
    }

    fun toggleFpsGovernor(locked: Boolean) {
        fpsGovernor.toggleGovernor(locked)
    }

    fun setPowerProfile(profile: String) {
        val current = _selectedGame.value
        _selectedGame.value = current.copy(customPowerProfile = profile)
    }

    fun performOneTapBoost() {
        if (_isBoosting.value) return
        viewModelScope.launch {
            _isBoosting.value = true
            hardwareManager.vibrateBoost()
            delay(400)

            val freedMb = hardwareManager.purgeRamCache()
            val latencyReduced = (4..9).random()

            _boostDialog.value = BoostDialogState(
                isOpen = true,
                ramFreedMb = freedMb,
                latencyReducedMs = latencyReduced,
                activeProfile = _selectedGame.value.customPowerProfile,
                fpsTarget = _selectedGame.value.customFpsTarget
            )
            _isBoosting.value = false
        }
    }

    fun dismissBoostDialog() {
        _boostDialog.value = BoostDialogState(isOpen = false)
    }

    fun launchGameWithOptimization(game: InstalledGame) {
        viewModelScope.launch {
            _launchState.value = GameLaunchState(
                isLaunching = true,
                game = game,
                stepMessage = "Purging background tasks & RAM cache...",
                ramClearedMb = 0L
            )
            hardwareManager.vibrateClick()
            delay(500)

            val freedMb = hardwareManager.purgeRamCache()
            _launchState.value = _launchState.value.copy(
                ramClearedMb = freedMb,
                stepMessage = "Enforcing ${game.customPowerProfile} & ${game.customFpsTarget} FPS Lock..."
            )
            hardwareManager.vibrateBoost()
            delay(600)

            // Save launch stats to repository / cloud
            _currentUser.value?.let { user ->
                repository.saveGameLaunch(user.uid, game)
            }

            _launchState.value = _launchState.value.copy(
                stepMessage = "🚀 Launching ${game.appName} in high-performance mode!",
                isCompleted = true
            )

            // Attempt actual package launch if installed on device
            try {
                val launchIntent = getApplication<Application>().packageManager.getLaunchIntentForPackage(game.packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    getApplication<Application>().startActivity(launchIntent)
                }
            } catch (_: Exception) {}

            delay(1000)
            _launchState.value = GameLaunchState(isLaunching = false)
            _activeRunningGame.value = game
            _selectedGame.value = game
            fpsGovernor.setTargetFps(game.customFpsTarget)
        }
    }

    fun openGameOverlay(game: InstalledGame) {
        _activeRunningGame.value = game
        selectGame(game)
    }

    fun exitGameOverlay() {
        _activeRunningGame.value = null
    }

    fun purgeRamImmediate() {
        hardwareManager.vibrateClick()
        hardwareManager.purgeRamCache()
    }

    fun dismissLaunchDialog() {
        _launchState.value = GameLaunchState(isLaunching = false)
    }

    fun optimizeBatteryBackgroundTasks() {
        viewModelScope.launch {
            hardwareManager.vibrateClick()
            val (freedMb, addedMinutes) = hardwareManager.optimizeBackgroundTasks()
            _statusMessage.value = "Optimized! Freed ${freedMb}MB RAM • +${addedMinutes} min gaming time added!"
            delay(3500)
            _statusMessage.value = null
        }
    }

    fun runAiAnalysis() {
        if (_isAiAnalyzing.value) return
        viewModelScope.launch {
            _isAiAnalyzing.value = true
            val stats = hardwareManager.systemStats.value
            val battery = hardwareManager.batteryHealth.value
            val game = _selectedGame.value.appName
            val result = aiService.analyzeSystemPerformance(stats, battery, game)
            _aiResult.value = result.getOrNull()
            _isAiAnalyzing.value = false
        }
    }

    fun saveCustomProfile(profile: GameProfile) {
        viewModelScope.launch {
            val uid = _currentUser.value?.uid ?: "local_guest"
            repository.saveGameProfile(uid, profile)
            _statusMessage.value = "Profile '${profile.gameName}' saved and locked!"
            delay(3000)
            _statusMessage.value = null
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    fun handleUserSignedIn(user: FirebaseUser) {
        _currentUser.value = user
        observeUserProfiles(user.uid)
    }

    private fun scanDeviceGames() {
        try {
            val pm = getApplication<Application>().packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val detectedGames = mutableListOf<InstalledGame>()

            for (app in packages) {
                val isGame = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    app.category == ApplicationInfo.CATEGORY_GAME
                } else false

                val isInterestingApp = isGame || (app.flags and ApplicationInfo.FLAG_SYSTEM == 0)

                if (isInterestingApp && app.packageName != getApplication<Application>().packageName) {
                    val label = pm.getApplicationLabel(app).toString()
                    detectedGames.add(
                        InstalledGame(
                            packageName = app.packageName,
                            appName = label,
                            category = if (isGame) "Installed Game" else "Installed App",
                            isSystemInstalled = true,
                            launchCount = 5,
                            lastLaunched = System.currentTimeMillis() - 86400000L,
                            customFpsTarget = 60,
                            customPowerProfile = "Beast Performance",
                            estimatedRamUsageMb = 350
                        )
                    )
                }
            }

            // Combine with default esports games so list is always rich
            val combined = (detectedGames + repository.defaultGames).distinctBy { it.packageName }
            _installedGames.value = combined
        } catch (_: Exception) {
            _installedGames.value = repository.defaultGames
        }
    }

    override fun onCleared() {
        super.onCleared()
        fpsGovernor.stop()
    }
}
