package com.example.model

data class Trail(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val difficulty: String, // Easy, Moderate, Hard, Expert
    val distanceMeters: Double,
    val elevationGainMeters: Double,
    val estimatedDurationMinutes: Int,
    val trailType: String, // Loop, Point-to-Point, Out-and-Back
    val description: String,
    val highlights: List<String>,
    val recommendedSeason: String,
    val points: List<GpsPoint>,
    val waypoints: List<TrekWaypoint> = emptyList()
)
