package com.example.util

import android.location.Location
import com.example.model.GpsPoint
import kotlin.math.abs

object GpsFilter {

    private const val MAX_ACCURACY_THRESHOLD_METERS = 40.0f
    private const val MAX_SPEED_TREKKING_MPS = 35.0f // ~126 km/h (covers trail running, downhill biking, rejects teleport)
    private const val MIN_DISTANCE_DELTA_METERS = 1.5 // filters micro jitter when stationary

    /**
     * Calculates distance between two coordinates in meters.
     */
    fun calculateDistance(p1: GpsPoint, p2: GpsPoint): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0].toDouble()
    }

    /**
     * Determines whether a new GPS point is valid to append to the route.
     */
    fun isValidPoint(lastPoint: GpsPoint?, newPoint: GpsPoint): Boolean {
        // Discard poor accuracy points
        if (newPoint.accuracy > MAX_ACCURACY_THRESHOLD_METERS && newPoint.accuracy > 0f) {
            return false
        }

        if (lastPoint == null) return true

        val distance = calculateDistance(lastPoint, newPoint)
        val timeDeltaSeconds = (newPoint.timestamp - lastPoint.timestamp) / 1000.0

        if (timeDeltaSeconds <= 0.0) return false

        // Check if movement is too small (standing still GPS jitter)
        if (distance < MIN_DISTANCE_DELTA_METERS) {
            return false
        }

        // Calculate speed between points to reject GPS teleport spikes
        val calculatedSpeed = distance / timeDeltaSeconds
        if (calculatedSpeed > MAX_SPEED_TREKKING_MPS) {
            return false
        }

        return true
    }

    /**
     * Smooths elevation changes using minimum threshold to eliminate GPS vertical noise.
     * Returns Triple(newSmoothedAltitude, elevationGainDelta, elevationLossDelta).
     */
    fun processElevation(
        lastAltitude: Double,
        newRawAltitude: Double,
        thresholdMeters: Float = 3.0f
    ): Triple<Double, Double, Double> {
        if (newRawAltitude == 0.0) {
            return Triple(lastAltitude, 0.0, 0.0)
        }

        if (lastAltitude == 0.0) {
            return Triple(newRawAltitude, 0.0, 0.0)
        }

        // Low-pass filter (exponential moving average: 70% current, 30% new)
        val smoothed = (lastAltitude * 0.7) + (newRawAltitude * 0.3)
        val delta = smoothed - lastAltitude

        return if (abs(delta) >= thresholdMeters) {
            if (delta > 0) {
                Triple(smoothed, delta, 0.0)
            } else {
                Triple(smoothed, 0.0, abs(delta))
            }
        } else {
            Triple(smoothed, 0.0, 0.0)
        }
    }
}
