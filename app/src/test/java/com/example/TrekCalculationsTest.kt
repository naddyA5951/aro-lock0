package com.example

import com.example.model.GpsPoint
import com.example.model.Trek
import com.example.util.CalorieCalculator
import com.example.util.GpsFilter
import com.example.util.GpsPointJsonHelper
import com.example.util.GpxExporter
import com.example.util.UnitFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TrekCalculationsTest {

    @Test
    fun testGpsDistanceCalculation() {
        val p1 = GpsPoint(45.9765, 7.7491, 1600.0, 0f, 5f, 1000L)
        val p2 = GpsPoint(45.9855, 7.7491, 1800.0, 1.2f, 5f, 2000L)

        val distance = GpsFilter.calculateDistance(p1, p2)
        assertTrue("Distance should be approximately 1000 meters, got $distance", distance in 800.0..1200.0)
    }

    @Test
    fun testGpsFilterRejectsPoorAccuracy() {
        val lastPoint = GpsPoint(45.9765, 7.7491, 1600.0, 0f, 5f, 1000L)
        val badAccuracyPoint = GpsPoint(45.9768, 7.7494, 1610.0, 1f, 80f, 2000L)

        val isValid = GpsFilter.isValidPoint(lastPoint, badAccuracyPoint)
        assertFalse("Points with accuracy > 40m should be rejected", isValid)
    }

    @Test
    fun testGpsFilterRejectsTeleportSpikes() {
        val lastPoint = GpsPoint(45.9765, 7.7491, 1600.0, 0f, 5f, 1000L)
        val teleportPoint = GpsPoint(46.0200, 7.7491, 1600.0, 0f, 5f, 2000L)

        val isValid = GpsFilter.isValidPoint(lastPoint, teleportPoint)
        assertFalse("Impossible speed spikes should be rejected", isValid)
    }

    @Test
    fun testElevationNoiseSmoothing() {
        val (smoothed1, gain1, loss1) = GpsFilter.processElevation(1500.0, 1501.0, 3.0f)
        assertEquals(0.0, gain1, 0.01)
        assertEquals(0.0, loss1, 0.01)

        val (smoothed2, gain2, loss2) = GpsFilter.processElevation(1500.0, 1520.0, 3.0f)
        assertTrue("Gain should register for significant climb", gain2 > 0.0)
    }

    @Test
    fun testCalorieCalculator() {
        val calories = CalorieCalculator.estimateCalories(
            durationSeconds = 7200L,
            distanceMeters = 8000.0,
            elevationGainMeters = 300.0,
            weightKg = 75f
        )

        assertTrue("Calories burned should be reasonable for 2h hike (500-1400 kcal), got $calories", calories in 500..1400)
    }

    @Test
    fun testUnitFormatting() {
        assertEquals("01:30:15", UnitFormatter.formatDuration(5415L))
        assertEquals("1h 30m", UnitFormatter.formatDurationHuman(5415L))

        assertEquals("5.25 km", UnitFormatter.formatDistance(5250.0, useMetric = true))
        assertEquals("450 m", UnitFormatter.formatDistance(450.0, useMetric = true))

        val imperial = UnitFormatter.formatDistance(5250.0, useMetric = false)
        assertTrue("Should contain mi", imperial.contains("mi"))

        assertEquals("+450 m", UnitFormatter.formatElevation(450.0, useMetric = true))
    }

    @Test
    fun testGpsPointJsonHelper() {
        val points = listOf(
            GpsPoint(45.1, 7.2, 1200.0, 1.4f, 4.5f, 100000L),
            GpsPoint(45.2, 7.3, 1250.0, 1.6f, 3.8f, 105000L)
        )
        val json = GpsPointJsonHelper.pointsToJson(points)
        val restored = GpsPointJsonHelper.jsonToPoints(json)

        assertEquals(2, restored.size)
        assertEquals(45.1, restored[0].latitude, 0.0001)
        assertEquals(7.3, restored[1].longitude, 0.0001)
        assertEquals(1250.0, restored[1].altitude, 0.1)
    }

    @Test
    fun testGpxExporter() {
        val points = listOf(
            GpsPoint(46.0, 8.0, 1500.0, 1.2f, 4f, 1700000000000L),
            GpsPoint(46.01, 8.01, 1580.0, 1.3f, 4f, 1700000060000L)
        )
        val trek = Trek(
            id = 1,
            name = "Mount Blanc Trail",
            startTimeMillis = 1700000000000L,
            pointsJson = GpsPointJsonHelper.pointsToJson(points)
        )

        val gpx = GpxExporter.generateGpxString(trek)
        assertTrue("GPX must contain standard header", gpx.contains("<gpx version=\"1.1\""))
        assertTrue("GPX must contain track name", gpx.contains("<name>Mount Blanc Trail</name>"))
        assertTrue("GPX must contain trkpt tags", gpx.contains("<trkpt lat=\"46.0\" lon=\"8.0\">"))
        assertTrue("GPX must contain elevation", gpx.contains("<ele>1500.0</ele>"))
    }
}
