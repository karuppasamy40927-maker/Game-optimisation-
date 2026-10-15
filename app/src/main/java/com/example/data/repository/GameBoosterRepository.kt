package com.example.data.repository

import com.example.data.model.GameProfile
import com.example.data.model.InstalledGame
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GameBoosterRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Default baseline popular games for Game Launcher (essential on Android emulators where user hasn't downloaded third-party games yet)
    val defaultGames = listOf(
        InstalledGame(
            packageName = "com.tencent.ig",
            appName = "PUBG Mobile",
            category = "Esports Battle Royale",
            launchCount = 42,
            lastLaunched = System.currentTimeMillis() - 3600000L,
            customFpsTarget = 90,
            customPowerProfile = "Beast Performance",
            estimatedRamUsageMb = 820
        ),
        InstalledGame(
            packageName = "com.miHoYo.GenshinImpact",
            appName = "Genshin Impact",
            category = "Open World RPG",
            launchCount = 28,
            lastLaunched = System.currentTimeMillis() - 7200000L,
            customFpsTarget = 60,
            customPowerProfile = "Extreme Performance",
            estimatedRamUsageMb = 1150
        ),
        InstalledGame(
            packageName = "com.activision.callofduty.shooter",
            appName = "Call of Duty: Mobile",
            category = "Competitive FPS",
            launchCount = 65,
            lastLaunched = System.currentTimeMillis() - 14400000L,
            customFpsTarget = 120,
            customPowerProfile = "Beast Performance",
            estimatedRamUsageMb = 750
        ),
        InstalledGame(
            packageName = "com.mobile.legends",
            appName = "Mobile Legends: Bang Bang",
            category = "Esports MOBA",
            launchCount = 51,
            lastLaunched = System.currentTimeMillis() - 86400000L,
            customFpsTarget = 90,
            customPowerProfile = "Balanced Mode",
            estimatedRamUsageMb = 480
        ),
        InstalledGame(
            packageName = "com.riotgames.league.wildrift",
            appName = "League of Legends: Wild Rift",
            category = "Esports MOBA",
            launchCount = 19,
            lastLaunched = System.currentTimeMillis() - 172800000L,
            customFpsTarget = 120,
            customPowerProfile = "Beast Performance",
            estimatedRamUsageMb = 620
        )
    )

    private val _localGames = MutableStateFlow(defaultGames)
    val localGames: Flow<List<InstalledGame>> = _localGames.asStateFlow()

    private val _localProfiles = MutableStateFlow(
        listOf(
            GameProfile(
                id = "pubg_beast",
                gameName = "PUBG Mobile",
                targetFps = 90,
                resolutionScale = 100,
                powerProfile = "Beast Performance",
                dnsOptimizer = true,
                touchPollingRateHz = 240
            ),
            GameProfile(
                id = "genshin_opt",
                gameName = "Genshin Impact",
                targetFps = 60,
                resolutionScale = 85,
                powerProfile = "Extreme Performance",
                dnsOptimizer = true,
                touchPollingRateHz = 180
            ),
            GameProfile(
                id = "codm_ultra",
                gameName = "Call of Duty: Mobile",
                targetFps = 120,
                resolutionScale = 100,
                powerProfile = "Beast Performance",
                dnsOptimizer = true,
                touchPollingRateHz = 360
            )
        )
    )
    val localProfiles: Flow<List<GameProfile>> = _localProfiles.asStateFlow()

    suspend fun saveGameProfile(userId: String, profile: GameProfile) {
        val profileId = if (profile.id.isNotBlank()) profile.id else "prof_${System.currentTimeMillis()}"
        val data = mapOf(
            "gameName" to profile.gameName,
            "targetFps" to profile.targetFps,
            "resolutionScale" to profile.resolutionScale,
            "powerProfile" to profile.powerProfile,
            "dnsOptimizer" to profile.dnsOptimizer,
            "touchPollingRateHz" to profile.touchPollingRateHz,
            "updatedAt" to System.currentTimeMillis()
        )

        // Save to Firestore if authenticated
        try {
            firestore.collection("users")
                .document(userId)
                .collection("profiles")
                .document(profileId)
                .set(data)
                .await()
        } catch (_: Exception) {}

        // Keep local updated
        val current = _localProfiles.value.toMutableList()
        val index = current.indexOfFirst { it.id == profileId || it.gameName.equals(profile.gameName, ignoreCase = true) }
        val updated = profile.copy(id = profileId)
        if (index >= 0) {
            current[index] = updated
        } else {
            current.add(updated)
        }
        _localProfiles.value = current
    }

    suspend fun saveGameLaunch(userId: String, game: InstalledGame) {
        val updatedGame = game.copy(
            launchCount = game.launchCount + 1,
            lastLaunched = System.currentTimeMillis()
        )

        try {
            val docId = game.packageName.replace(".", "_")
            firestore.collection("users")
                .document(userId)
                .collection("games")
                .document(docId)
                .set(
                    mapOf(
                        "packageName" to updatedGame.packageName,
                        "appName" to updatedGame.appName,
                        "launchCount" to updatedGame.launchCount,
                        "lastLaunched" to updatedGame.lastLaunched,
                        "customFpsTarget" to updatedGame.customFpsTarget,
                        "customPowerProfile" to updatedGame.customPowerProfile
                    )
                )
                .await()
        } catch (_: Exception) {}

        val current = _localGames.value.toMutableList()
        val idx = current.indexOfFirst { it.packageName == game.packageName }
        if (idx >= 0) {
            current[idx] = updatedGame
        } else {
            current.add(0, updatedGame)
        }
        _localGames.value = current
    }

    fun observeGameProfiles(userId: String): Flow<List<GameProfile>> = callbackFlow {
        var listener: ListenerRegistration? = null
        try {
            val query = firestore.collection("users")
                .document(userId)
                .collection("profiles")

            listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        GameProfile(
                            id = doc.id,
                            gameName = data["gameName"] as? String ?: "",
                            targetFps = (data["targetFps"] as? Number)?.toInt() ?: 60,
                            resolutionScale = (data["resolutionScale"] as? Number)?.toInt() ?: 100,
                            powerProfile = data["powerProfile"] as? String ?: "Beast Performance",
                            dnsOptimizer = data["dnsOptimizer"] as? Boolean ?: true,
                            touchPollingRateHz = (data["touchPollingRateHz"] as? Number)?.toInt() ?: 240,
                            updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L
                        )
                    }
                    trySend(list)
                } else {
                    trySend(_localProfiles.value)
                }
            }
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            listener?.remove()
        }
    }
}
