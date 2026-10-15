package com.example.data.model

data class GameProfile(
    val id: String = "",
    val gameName: String = "",
    val targetFps: Int = 60,
    val resolutionScale: Int = 100,
    val powerProfile: String = "Beast Performance",
    val dnsOptimizer: Boolean = true,
    val touchPollingRateHz: Int = 240,
    val updatedAt: Long = System.currentTimeMillis()
)
