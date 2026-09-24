package com.example.model

data class UserProfile(
    val name: String = "Adventurer",
    val age: Int = 28,
    val weightKg: Float = 70f,
    val gender: String = "Other", // Male, Female, Other
    val useMetric: Boolean = true, // true = km/m/kmh, false = mi/ft/mph
    val isDarkMode: Boolean = true,
    val gpsHighAccuracy: Boolean = true,
    val elevationSensitivityMeters: Float = 3.0f
)
