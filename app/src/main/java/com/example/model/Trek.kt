package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "treks")
data class Trek(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val startTimeMillis: Long,
    val endTimeMillis: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val distanceMeters: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val elevationLossMeters: Double = 0.0,
    val minAltitude: Double = 0.0,
    val maxAltitude: Double = 0.0,
    val currentAltitude: Double = 0.0,
    val avgSpeedMps: Double = 0.0,
    val maxSpeedMps: Double = 0.0,
    val estimatedCalories: Int = 0,
    val difficulty: String = "Moderate", // Easy, Moderate, Hard, Extreme
    val pointsJson: String = "[]",      // Serialized List<GpsPoint>
    val waypointsJson: String = "[]",   // Serialized List<TrekWaypoint>
    val mediaPathsJson: String = "[]",  // Serialized List<String> of local photo/video URIs
    val isFinished: Boolean = true
)

data class TrekStatistics(
    val totalTreks: Int = 0,
    val totalDistanceMeters: Double = 0.0,
    val totalElevationGainMeters: Double = 0.0,
    val totalDurationSeconds: Long = 0,
    val totalCalories: Int = 0
)
