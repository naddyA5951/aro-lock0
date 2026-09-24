package com.example.util

import kotlin.math.max

object CalorieCalculator {

    /**
     * Estimates calories burned during a trek using Metabolic Equivalent of Task (MET).
     *
     * General walking/hiking MET values:
     * - Flat hiking at 4.0 km/h: ~5.3 MET
     * - Hiking with elevation gain: 6.5 - 8.5 MET
     * - Formula: Calories = (MET * 3.5 * weightKg / 200) * (durationMinutes)
     */
    fun estimateCalories(
        durationSeconds: Long,
        distanceMeters: Double,
        elevationGainMeters: Double,
        weightKg: Float = 70f,
        stepCount: Int = 0
    ): Int {
        if (durationSeconds <= 0 || weightKg <= 0f) return 0

        val durationMinutes = durationSeconds / 60.0
        val distanceKm = distanceMeters / 1000.0
        val durationHours = durationSeconds / 3600.0

        // Calculate average speed in km/h
        val avgSpeedKmh = if (durationHours > 0) distanceKm / durationHours else 0.0

        // Base MET based on speed
        var met = when {
            avgSpeedKmh < 2.0 -> 3.0
            avgSpeedKmh < 4.0 -> 4.5
            avgSpeedKmh < 5.5 -> 6.0
            avgSpeedKmh < 7.0 -> 7.5
            else -> 9.0
        }

        // Add MET boost for elevation climbing (e.g. +0.05 MET per 10m climbed per hour)
        if (durationHours > 0 && elevationGainMeters > 0) {
            val elevationPerHour = elevationGainMeters / durationHours
            val climbingBonus = (elevationPerHour / 100.0) * 0.8
            met += climbingBonus.coerceIn(0.0, 4.0)
        }

        var totalKcal = (met * 3.5 * weightKg / 200.0) * durationMinutes

        // If step count is available, ensure minimum realistic burn (~0.04 kcal per step for 70kg hiker)
        if (stepCount > 0) {
            val stepBasedKcal = stepCount * 0.045 * (weightKg / 70.0)
            totalKcal = max(totalKcal, stepBasedKcal)
        }

        return max(0, totalKcal.toInt())
    }
}
