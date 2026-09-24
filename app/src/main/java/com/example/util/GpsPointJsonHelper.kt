package com.example.util

import com.example.model.GpsPoint
import org.json.JSONArray
import org.json.JSONObject

object GpsPointJsonHelper {

    fun pointsToJson(points: List<GpsPoint>): String {
        val array = JSONArray()
        for (p in points) {
            val obj = JSONObject()
            obj.put("lat", p.latitude)
            obj.put("lng", p.longitude)
            obj.put("alt", p.altitude)
            obj.put("spd", p.speed.toDouble())
            obj.put("acc", p.accuracy.toDouble())
            obj.put("ts", p.timestamp)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToPoints(json: String?): List<GpsPoint> {
        if (json.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<GpsPoint>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GpsPoint(
                        latitude = obj.getDouble("lat"),
                        longitude = obj.getDouble("lng"),
                        altitude = obj.optDouble("alt", 0.0),
                        speed = obj.optDouble("spd", 0.0).toFloat(),
                        accuracy = obj.optDouble("acc", 0.0).toFloat(),
                        timestamp = obj.optLong("ts", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {
            // Return what was successfully parsed
        }
        return list
    }

    fun stringListToJson(items: List<String>): String {
        val array = JSONArray()
        for (item in items) {
            array.put(item)
        }
        return array.toString()
    }

    fun jsonToStringList(json: String?): List<String> {
        if (json.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {
            // Ignore error
        }
        return list
    }

    fun waypointsToJson(waypoints: List<com.example.model.TrekWaypoint>): String {
        val array = JSONArray()
        for (wp in waypoints) {
            val obj = JSONObject()
            obj.put("id", wp.id)
            obj.put("title", wp.title)
            obj.put("type", wp.type.name)
            obj.put("lat", wp.latitude)
            obj.put("lng", wp.longitude)
            obj.put("alt", wp.altitude)
            obj.put("ts", wp.timestamp)
            obj.put("note", wp.note)
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToWaypoints(json: String?): List<com.example.model.TrekWaypoint> {
        if (json.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<com.example.model.TrekWaypoint>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val typeStr = obj.optString("type", "VIEWPOINT")
                val type = try {
                    com.example.model.WaypointType.valueOf(typeStr)
                } catch (_: Exception) {
                    com.example.model.WaypointType.VIEWPOINT
                }
                list.add(
                    com.example.model.TrekWaypoint(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        title = obj.optString("title", "Waypoint"),
                        type = type,
                        latitude = obj.getDouble("lat"),
                        longitude = obj.getDouble("lng"),
                        altitude = obj.optDouble("alt", 0.0),
                        timestamp = obj.optLong("ts", System.currentTimeMillis()),
                        note = obj.optString("note", "")
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return list
    }
}
