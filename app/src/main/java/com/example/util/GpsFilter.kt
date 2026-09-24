package com.example.util

import android.location.Location
import com.example.model.GpsPoint
import kotlin.math.abs
import kotlin.math.max

object GpsFilter {

    // Maximum accuracy radius in meters before GPS points are deemed too noisy to trust
    const val MAX_ACCURACY_THRESHOLD_METERS = 30.0f

    // Walking speed threshold in m/s (~1.8 km/h). Any GPS speed below this is stationary noise.
    const val MIN_MOVING_SPEED_MPS = 0.5f

    // Maximum realistic trekking/running/biking speed in m/s (~72 km/h) to reject satellite teleportation
    const val MAX_SPEED_TREKKING_MPS = 20.0f

    /**
     * Calculates geodesic distance between two coordinates in meters.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0].toDouble()
    }

    /**
     * Calculates geodesic distance between two GpsPoints in meters.
     */
    fun calculateDistance(p1: GpsPoint, p2: GpsPoint): Double {
        return calculateDistance(p1.latitude, p1.longitude, p2.latitude, p2.longitude)
    }

    /**
     * Determines whether a point is valid for route storage, rejecting poor accuracy and teleport spikes.
     */
    fun isValidPoint(lastPoint: GpsPoint?, newPoint: GpsPoint): Boolean {
        if (newPoint.accuracy > 40.0f && newPoint.accuracy > 0f) {
            return false
        }
        if (lastPoint == null) return true

        val distance = calculateDistance(lastPoint, newPoint)
        val timeDeltaSeconds = (newPoint.timestamp - lastPoint.timestamp) / 1000.0

        if (timeDeltaSeconds <= 0.0) return false

        val calculatedSpeed = distance / timeDeltaSeconds
        if (calculatedSpeed > 35.0f) {
            return false
        }

        return true
    }

    /**
     * Determines whether the user has genuinely moved beyond the GPS uncertainty radius.
     * Prevents the odometer from accumulating fake distance while stationary.
     */
    fun isGenuineMovement(
        lastPoint: GpsPoint?,
        newLocation: Location,
        distDelta: Double,
        timeDeltaSeconds: Double
    ): Boolean {
        if (lastPoint == null) return false

        val accuracy = if (newLocation.hasAccuracy()) newLocation.accuracy else 15f
        if (accuracy > MAX_ACCURACY_THRESHOLD_METERS) {
            return false // Too noisy
        }

        // 1. Hardware Doppler Speed check (most accurate source from GPS chipset)
        if (newLocation.hasSpeed()) {
            if (newLocation.speed < MIN_MOVING_SPEED_MPS) {
                return false // Physically stationary: Doppler radar reports stopped
            }
            // Moving with reported speed >= 0.5 m/s. Ensure distance is at least 2 meters
            return distDelta >= 2.0
        }

        // 2. Position difference check: displacement must comfortably exceed GPS circular error probable (CEP)
        val minRequiredDisplacement = max(6.0, accuracy.toDouble() * 0.8)
        if (distDelta < minRequiredDisplacement) {
            return false // Jitter inside the circle of uncertainty
        }

        // 3. Time elapsed check
        if (timeDeltaSeconds < 1.0) {
            return false
        }

        // 4. Reject teleport spikes
        val calculatedSpeed = distDelta / timeDeltaSeconds
        if (calculatedSpeed > MAX_SPEED_TREKKING_MPS) {
            return false
        }

        return true
    }

    /**
     * Smooths GPS altitude and computes genuine elevation gain/loss only during movement.
     * Eliminates fake altitude drift while sitting or standing still.
     */
    fun processElevation(
        lastAltitude: Double,
        newRawAltitude: Double,
        isMoving: Boolean = true,
        thresholdMeters: Float = 4.0f
    ): Triple<Double, Double, Double> {
        if (newRawAltitude == 0.0) {
            return Triple(lastAltitude, 0.0, 0.0)
        }

        if (lastAltitude == 0.0) {
            return Triple(newRawAltitude, 0.0, 0.0)
        }

        // Heavy exponential moving average filter for vertical stability
        val smoothed = (lastAltitude * 0.85) + (newRawAltitude * 0.15)
        val delta = smoothed - lastAltitude

        // CRITICAL: NEVER accumulate elevation gain if user is stationary!
        if (!isMoving) {
            return Triple(smoothed, 0.0, 0.0)
        }

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

    fun processElevation(
        lastAltitude: Double,
        newRawAltitude: Double,
        thresholdMeters: Float
    ): Triple<Double, Double, Double> {
        return processElevation(lastAltitude, newRawAltitude, isMoving = true, thresholdMeters = thresholdMeters)
    }
}
