package com.example.model

/**
 * Represents a single GPS coordinate recorded during a trek.
 */
data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,      // in meters
    val speed: Float = 0f,           // in meters/second
    val accuracy: Float = 0f,        // in meters
    val timestamp: Long = System.currentTimeMillis()
)
