package com.example.data.model

data class InstalledGame(
    val packageName: String,
    val appName: String,
    val category: String = "Action / Esports",
    val isSystemInstalled: Boolean = true,
    val launchCount: Int = 0,
    val lastLaunched: Long = 0L,
    val customFpsTarget: Int = 60,
    val customPowerProfile: String = "Beast Performance",
    val estimatedRamUsageMb: Int = 450
)
