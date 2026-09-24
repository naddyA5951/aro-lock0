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
        trekDao.getTotalDurationFlow(),
        trekDao.getTotalCaloriesFlow()
    ) { count, distance, elevation, duration, calories ->
        TrekStatistics(
            totalTreks = count,
            totalDistanceMeters = distance ?: 0.0,
            totalElevationGainMeters = elevation ?: 0.0,
            totalDurationSeconds = duration ?: 0L,
            totalCalories = calories ?: 0
        )
    }

    suspend fun getTrekById(id: Long): Trek? = trekDao.getTrekById(id)

    fun getTrekByIdFlow(id: Long): Flow<Trek?> = trekDao.getTrekByIdFlow(id)

    suspend fun saveTrek(trek: Trek): Long = trekDao.insertTrek(trek)

    suspend fun updateTrek(trek: Trek) = trekDao.updateTrek(trek)

    suspend fun deleteTrek(id: Long) = trekDao.deleteTrekById(id)

    suspend fun deleteAllTreks() = trekDao.deleteAllTreks()

    fun updateProfile(profile: UserProfile) = userPreferences.saveProfile(profile)

    fun setUnitSystem(useMetric: Boolean) = userPreferences.setUnitSystem(useMetric)

    fun setDarkMode(isDark: Boolean) = userPreferences.setDarkMode(isDark)
}
