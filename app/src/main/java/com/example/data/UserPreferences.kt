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
        val savedEmail = prefs.getString("user_email", "") ?: ""
        val savedLoggedIn = prefs.getBoolean("is_logged_in", false) && savedEmail.isNotBlank()
        return UserProfile(
            id = prefs.getString("user_id", "user_${System.currentTimeMillis()}") ?: "user_default",
            name = prefs.getString("user_name", "") ?: "",
            email = savedEmail,
            isEmailVerified = prefs.getBoolean("is_email_verified", false),
            photoUri = prefs.getString("user_photo_uri", null),
            authProvider = prefs.getString("auth_provider", "Email") ?: "Email",
            isLoggedIn = savedLoggedIn,
            preferredMapStyle = prefs.getString("preferred_map_style", "TOPO") ?: "TOPO",
            age = prefs.getInt("user_age", 28),
            weightKg = prefs.getFloat("user_weight", 70f),
            gender = prefs.getString("user_gender", "Other") ?: "Other",
            useMetric = prefs.getBoolean("use_metric", true),
            isDarkMode = prefs.getBoolean("dark_mode", true),
            gpsHighAccuracy = prefs.getBoolean("gps_high_accuracy", true),
            elevationSensitivityMeters = prefs.getFloat("elevation_sensitivity", 3.0f),
            memberSince = prefs.getString("member_since", "September 2026") ?: "September 2026"
        )
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit().apply {
            putString("user_id", profile.id)
            putString("user_name", profile.name)
            putString("user_email", profile.email)
            putBoolean("is_email_verified", profile.isEmailVerified)
            putString("user_photo_uri", profile.photoUri)
            putString("auth_provider", profile.authProvider)
            putBoolean("is_logged_in", profile.isLoggedIn && profile.email.isNotBlank())
            putString("preferred_map_style", profile.preferredMapStyle)
            putInt("user_age", profile.age)
            putFloat("user_weight", profile.weightKg)
            putString("user_gender", profile.gender)
            putBoolean("use_metric", profile.useMetric)
            putBoolean("dark_mode", profile.isDarkMode)
            putBoolean("gps_high_accuracy", profile.gpsHighAccuracy)
            putFloat("elevation_sensitivity", profile.elevationSensitivityMeters)
            putString("member_since", profile.memberSince)
            apply()
        }
        _userProfile.value = profile
    }

    fun registerAccount(name: String, email: String, password: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        val accountKey = "pwd_$cleanEmail"
        val nameKey = "name_$cleanEmail"
        prefs.edit().apply {
            putString(accountKey, password)
            putString(nameKey, name)
            apply()
        }
        verifyEmailAndLogin(name, cleanEmail, "Email")
        return true
    }

    fun loginWithPassword(email: String, password: String): Pair<Boolean, String> {
        val cleanEmail = email.trim().lowercase()
        val savedPwd = prefs.getString("pwd_$cleanEmail", null)
        val savedName = prefs.getString("name_$cleanEmail", null) ?: cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }

        return if (savedPwd == null) {
            registerAccount(savedName, cleanEmail, password)
            Pair(true, "Account created & authenticated successfully")
        } else if (savedPwd == password) {
            verifyEmailAndLogin(savedName, cleanEmail, "Email")
            Pair(true, "Logged in successfully")
        } else {
            Pair(false, "Incorrect password for this email address")
        }
    }

    fun verifyEmailAndLogin(displayName: String, email: String, authProvider: String = "Email") {
        val current = _userProfile.value
        val name = displayName.ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }
        saveProfile(
            current.copy(
                name = name,
                email = email.trim(),
                isEmailVerified = true,
                authProvider = authProvider,
                isLoggedIn = true
            )
        )
    }

    fun loginWithGoogle(displayName: String, email: String, photoUri: String? = null) {
        val current = _userProfile.value
        saveProfile(
            current.copy(
                name = displayName.ifEmpty { "Explorer" },
                email = email.trim(),
                isEmailVerified = true,
                photoUri = photoUri,
                authProvider = "Google",
                isLoggedIn = true
            )
        )
    }

    fun loginWithEmail(displayName: String, email: String) {
        verifyEmailAndLogin(displayName, email, "Email")
    }

    fun setPreferredMapStyle(styleKey: String) {
        val current = _userProfile.value
        saveProfile(current.copy(preferredMapStyle = styleKey))
    }

    fun logout() {
        val current = _userProfile.value
        saveProfile(
            current.copy(
                isLoggedIn = false,
                isEmailVerified = false,
                email = "",
                name = "",
                authProvider = "Email"
            )
        )
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
