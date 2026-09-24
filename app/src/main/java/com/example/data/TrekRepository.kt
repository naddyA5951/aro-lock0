package com.example.data

import com.example.model.Trek
import com.example.model.TrekStatistics
import com.example.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class TrekRepository(
    private val trekDao: TrekDao,
    private val userPreferences: UserPreferences
) {

    val allTreks: Flow<List<Trek>> = trekDao.getAllTreks()
    val recentTreks: Flow<List<Trek>> = trekDao.getRecentTreks()
    val userProfile: StateFlow<UserProfile> = userPreferences.userProfile

    val statistics: Flow<TrekStatistics> = combine(
        trekDao.getTreksCountFlow(),
        trekDao.getTotalDistanceFlow(),
        trekDao.getTotalElevationGainFlow(),
        combine(
            trekDao.getTotalDurationFlow(),
            trekDao.getTotalCaloriesFlow(),
            trekDao.getTotalStepsFlow()
        ) { duration, calories, steps -> Triple(duration, calories, steps) }
    ) { count, distance, elevation, subStats ->
        val (duration, calories, steps) = subStats
        TrekStatistics(
            totalTreks = count,
            totalDistanceMeters = distance ?: 0.0,
            totalElevationGainMeters = elevation ?: 0.0,
            totalDurationSeconds = duration ?: 0L,
            totalCalories = calories ?: 0,
            totalSteps = steps ?: 0
        )
    }

    suspend fun getTrekById(id: Long): Trek? = trekDao.getTrekById(id)

    fun getTrekByIdFlow(id: Long): Flow<Trek?> = trekDao.getTrekByIdFlow(id)

    suspend fun saveTrek(trek: Trek): Long = trekDao.insertTrek(trek)

    suspend fun updateTrek(trek: Trek) = trekDao.updateTrek(trek)

    suspend fun deleteTrek(id: Long) = trekDao.deleteTrekById(id)

    suspend fun deleteAllTreks() = trekDao.deleteAllTreks()

    fun updateProfile(profile: UserProfile) = userPreferences.saveProfile(profile)

    fun loginWithGoogle(displayName: String, email: String, photoUri: String? = null) =
        userPreferences.loginWithGoogle(displayName, email, photoUri)

    fun loginWithEmail(displayName: String, email: String) =
        userPreferences.loginWithEmail(displayName, email)

    fun registerAccount(name: String, email: String, password: String): Boolean =
        userPreferences.registerAccount(name, email, password)

    fun loginWithPassword(email: String, password: String): Pair<Boolean, String> =
        userPreferences.loginWithPassword(email, password)

    fun verifyEmailAndLogin(displayName: String, email: String, authProvider: String = "Email") =
        userPreferences.verifyEmailAndLogin(displayName, email, authProvider)

    fun setPreferredMapStyle(styleKey: String) =
        userPreferences.setPreferredMapStyle(styleKey)

    fun logout() = userPreferences.logout()

    fun setUnitSystem(useMetric: Boolean) = userPreferences.setUnitSystem(useMetric)

    fun setDarkMode(isDark: Boolean) = userPreferences.setDarkMode(isDark)
}
