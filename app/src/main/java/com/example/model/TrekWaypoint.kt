package com.example.model

enum class WaypointType(val label: String, val iconSymbol: String) {
    SUMMIT("Summit / Peak", "🏔️"),
    WATER_SOURCE("Water Point", "💧"),
    CAMPSITE("Campsite / Shelter", "⛺"),
    VIEWPOINT("Scenic Viewpoint", "👁️"),
    HAZARD("Hazard / Obstacle", "⚠️"),
    TRAILHEAD("Trailhead / Start", "🚩"),
    REST_STOP("Rest Stop", "☕"),
    PHOTO_POINT("Photo Landmark", "📸")
}

data class TrekWaypoint(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val type: WaypointType = WaypointType.VIEWPOINT,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
