package com.example.data.model

data class BatteryHealthInfo(
    val healthLabel: String = "Good",
    val healthColor: Long = 0xFF00E676,
    val levelPercent: Int = 85,
    val isCharging: Boolean = false,
    val temperatureCelsius: Float = 34.2f,
    val voltageMv: Int = 4120,
    val currentDrawMa: Int = 480, // Real or calibrated current draw
    val powerDrawWatts: Float = 1.98f, // Calculated voltage * current
    val estimatedRemainingGamingMinutes: Int = 215, // Calculated based on battery capacity & current draw
    val backgroundServicesCount: Int = 14,
    val backgroundRamWasteMb: Int = 540,
    val optimizationSuggested: Boolean = true,
    val extendedMinutesPossible: Int = 45
)
