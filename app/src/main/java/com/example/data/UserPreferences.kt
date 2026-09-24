package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "arolock_user_prefs",
        Context.MODE_PRIVATE
    )

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString("user_name", "Trail Pioneer") ?: "Trail Pioneer",
            age = prefs.getInt("user_age", 28),
            weightKg = prefs.getFloat("user_weight", 70f),
            gender = prefs.getString("user_gender", "Other") ?: "Other",
            useMetric = prefs.getBoolean("use_metric", true),
            isDarkMode = prefs.getBoolean("dark_mode", true),
            gpsHighAccuracy = prefs.getBoolean("gps_high_accuracy", true),
            elevationSensitivityMeters = prefs.getFloat("elevation_sensitivity", 3.0f)
        )
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString("user_name", profile.name)
            putInt("user_age", profile.age)
            putFloat("user_weight", profile.weightKg)
            putString("user_gender", profile.gender)
            putBoolean("use_metric", profile.useMetric)
            putBoolean("dark_mode", profile.isDarkMode)
            putBoolean("gps_high_accuracy", profile.gpsHighAccuracy)
            putFloat("elevation_sensitivity", profile.elevationSensitivityMeters)
            apply()
        }
        _userProfile.value = profile
    }

    fun setUnitSystem(useMetric: Boolean) {
        val current = _userProfile.value
        saveProfile(current.copy(useMetric = useMetric))
    }

    fun setDarkMode(isDark: Boolean) {
        val current = _userProfile.value
        saveProfile(current.copy(isDarkMode = isDark))
    }
}
