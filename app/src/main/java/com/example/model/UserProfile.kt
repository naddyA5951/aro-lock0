package com.example.model

data class UserProfile(
    val id: String = "user_adventurer",
    val name: String = "Trail Pioneer",
    val email: String = "trekker@arolock.app",
    val photoUri: String? = null,
    val authProvider: String = "Guest", // "Google", "Email", "Guest"
    val isLoggedIn: Boolean = false,
    val age: Int = 28,
    val weightKg: Float = 70f,
    val gender: String = "Other", // Male, Female, Other
    val useMetric: Boolean = true, // true = km/m/kmh, false = mi/ft/mph
    val isDarkMode: Boolean = true,
    val gpsHighAccuracy: Boolean = true,
    val elevationSensitivityMeters: Float = 3.0f,
    val memberSince: String = "September 2026"
)
