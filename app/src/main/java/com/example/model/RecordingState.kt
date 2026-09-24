package com.example.model

data class RecordingState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val points: List<GpsPoint> = emptyList(),
    val waypoints: List<TrekWaypoint> = emptyList(),
    val referenceTrailPoints: List<GpsPoint> = emptyList(),
    val activeTrailId: String? = null,
    val activeTrailName: String? = null,
    val isSimulating: Boolean = false,
    val currentDistanceMeters: Double = 0.0,
    val currentSpeedMps: Float = 0f,
    val avgSpeedMps: Float = 0f,
    val maxSpeedMps: Float = 0f,
    val currentAltitude: Double = 0.0,
    val elevationGainMeters: Double = 0.0,
    val elevationLossMeters: Double = 0.0,
    val minAltitude: Double = 0.0,
    val maxAltitude: Double = 0.0,
    val estimatedCalories: Int = 0,
    val elapsedTimeSeconds: Long = 0,
    val startTimeMillis: Long = 0L,
    val currentTrekId: Long? = null,
    val lastKnownLocation: GpsPoint? = null,
    val attachedMediaPaths: List<String> = emptyList()
)
