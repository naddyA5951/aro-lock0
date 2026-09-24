package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UnitFormatter {

    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    }

    fun formatDurationHuman(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        return when {
            hrs > 0 -> "${hrs}h ${mins}m"
            mins > 0 -> "${mins}m"
            else -> "${seconds}s"
        }
    }

    fun formatDistance(meters: Double, useMetric: Boolean = true): String {
        return if (useMetric) {
            if (meters >= 1000) {
                String.format(Locale.getDefault(), "%.2f km", meters / 1000.0)
            } else {
                String.format(Locale.getDefault(), "%d m", meters.toInt())
            }
        } else {
            val miles = meters * 0.000621371
            String.format(Locale.getDefault(), "%.2f mi", miles)
        }
    }

    fun formatElevation(meters: Double, useMetric: Boolean = true): String {
        return if (useMetric) {
            String.format(Locale.getDefault(), "+%d m", meters.toInt())
        } else {
            val feet = meters * 3.28084
            String.format(Locale.getDefault(), "+%d ft", feet.toInt())
        }
    }

    fun formatElevationAltitude(meters: Double, useMetric: Boolean = true): String {
        return if (useMetric) {
            String.format(Locale.getDefault(), "%d m", meters.toInt())
        } else {
            val feet = meters * 3.28084
            String.format(Locale.getDefault(), "%d ft", feet.toInt())
        }
    }

    fun formatSpeed(metersPerSec: Float, useMetric: Boolean = true): String {
        return if (useMetric) {
            val kmh = metersPerSec * 3.6f
            String.format(Locale.getDefault(), "%.1f km/h", kmh)
        } else {
            val mph = metersPerSec * 2.23694f
            String.format(Locale.getDefault(), "%.1f mph", mph)
        }
    }

    fun formatPace(metersPerSec: Float, useMetric: Boolean = true): String {
        if (metersPerSec <= 0.25f) return "--:--"
        val secondsPerMeter = 1.0 / metersPerSec
        val secondsPerUnit = secondsPerMeter * (if (useMetric) 1000.0 else 1609.34)
        val mins = (secondsPerUnit / 60).toInt()
        val secs = (secondsPerUnit % 60).toInt()
        val unit = if (useMetric) "/km" else "/mi"
        return String.format(Locale.getDefault(), "%d:%02d %s", mins, secs, unit)
    }

    fun formatSteps(steps: Int): String {
        return if (steps >= 10000) {
            String.format(Locale.getDefault(), "%,d", steps)
        } else {
            steps.toString()
        }
    }

    fun formatDate(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }

    fun formatDateTime(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }
}
