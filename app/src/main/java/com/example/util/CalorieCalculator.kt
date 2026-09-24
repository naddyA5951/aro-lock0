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
        weightKg: Float = 70f
    ): Int {
        if (durationSeconds <= 0 || weightKg <= 0f) return 0

        val durationMinutes = durationSeconds / 60.0
        val distanceKm = distanceMeters / 1000.0
        val durationHours = durationSeconds / 3600.0

        // Calculate average speed in km/h
        val avgSpeedKmh = if (durationHours > 0) distanceKm / durationHours else 0.0

        // Base MET based on speed
        var met = when {
            avgSpeedKmh < 2.5 -> 3.5
            avgSpeedKmh < 4.0 -> 5.0
            avgSpeedKmh < 5.5 -> 6.5
            avgSpeedKmh < 7.0 -> 7.8
            else -> 9.0
        }

        // Add MET boost for elevation climbing (e.g. +0.05 MET per 10m climbed per hour)
        if (durationHours > 0 && elevationGainMeters > 0) {
            val elevationPerHour = elevationGainMeters / durationHours
            val climbingBonus = (elevationPerHour / 100.0) * 0.8
            met += climbingBonus.coerceIn(0.0, 4.0)
        }

        val totalKcal = (met * 3.5 * weightKg / 200.0) * durationMinutes
        return max(0, totalKcal.toInt())
    }
}
