package com.example.data

import com.example.model.GpsPoint
import com.example.model.Trail
import com.example.model.TrekWaypoint
import com.example.model.WaypointType

object TrailsCatalog {

    val allTrails: List<Trail> by lazy {
        listOf(
            montBlancTrail,
            yosemiteMistTrail,
            everestBaseCampSection,
            zionAngelsLanding,
            eigerTrailSwitzerland,
            incaTrailMachuPicchu,
            mountFujiTrail
        )
    }

    fun getTrailById(id: String): Trail? {
        return allTrails.firstOrNull { it.id == id }
    }

    // 1. Tour du Mont Blanc: Chamonix to Refuge du Lac Blanc (France)
    private val montBlancTrail: Trail by lazy {
        val baseLat = 45.9237
        val baseLng = 6.8694
        val rawPoints = listOf(
            Triple(45.9237, 6.8694, 1035.0),
            Triple(45.9265, 6.8740, 1140.0),
            Triple(45.9310, 6.8795, 1290.0),
            Triple(45.9372, 6.8850, 1480.0),
            Triple(45.9420, 6.8890, 1680.0),
            Triple(45.9485, 6.8920, 1870.0),
            Triple(45.9540, 6.8965, 2050.0),
            Triple(45.9592, 6.8995, 2180.0),
            Triple(45.9640, 6.8970, 2270.0),
            Triple(45.9680, 6.8935, 2352.0)
        )
        val points = interpolatePoints(rawPoints)
        Trail(
            id = "trail_mont_blanc_lac_blanc",
            name = "Tour du Mont Blanc: Lac Blanc Trail",
            region = "Haute-Savoie, Chamonix",
            country = "France",
            difficulty = "Hard",
            distanceMeters = 8400.0,
            elevationGainMeters = 1317.0,
            estimatedDurationMinutes = 260,
            trailType = "Out-and-Back",
            description = "Iconic alpine trek offering panoramic vistas of the Mont Blanc massif, majestic glaciers, and the crystalline glacial waters of Lac Blanc.",
            highlights = listOf(
                "Aiguille du Midi panorama",
                "Lac Blanc alpine refuge (2,352m)",
                "Alpine ibex wildlife spotting",
                "Ladder climbs on rock faces"
            ),
            recommendedSeason = "June – September",
            points = points,
            waypoints = listOf(
                TrekWaypoint(title = "Les Praz Trailhead", type = WaypointType.TRAILHEAD, latitude = 45.9237, longitude = 6.8694, altitude = 1035.0, note = "Parking and gear prep"),
                TrekWaypoint(title = "Chalet des Chéserys", type = WaypointType.REST_STOP, latitude = 45.9485, longitude = 6.8920, altitude = 1870.0, note = "Water and scenic terrace"),
                TrekWaypoint(title = "Chéserys Lakes Viewpoint", type = WaypointType.VIEWPOINT, latitude = 45.9592, longitude = 6.8995, altitude = 2180.0, note = "Mont Blanc mirror reflection"),
                TrekWaypoint(title = "Refuge du Lac Blanc", type = WaypointType.SUMMIT, latitude = 45.9680, longitude = 6.8935, altitude = 2352.0, note = "Summit hut & glacial lake")
            )
        )
    }

    // 2. Yosemite Mist Trail to Nevada Falls (USA)
    private val yosemiteMistTrail: Trail by lazy {
        val rawPoints = listOf(
            Triple(37.7345, -119.5663, 1225.0),
            Triple(37.7320, -119.5605, 1280.0),
            Triple(37.7285, -119.5540, 1370.0),
            Triple(37.7260, -119.5480, 1490.0), // Vernal Fall Footbridge & Top
            Triple(37.7245, -119.5420, 1610.0), // Silver Apron
            Triple(37.7230, -119.5365, 1780.0),
            Triple(37.7210, -119.5320, 1820.0), // Top of Nevada Fall
            Triple(37.7235, -119.5370, 1720.0),
            Triple(37.7270, -119.5460, 1530.0),
            Triple(37.7340, -119.5660, 1228.0)
        )
        val points = interpolatePoints(rawPoints)
        Trail(
            id = "trail_yosemite_mist_trail",
            name = "Mist Trail to Vernal & Nevada Falls",
            region = "Yosemite National Park, California",
            country = "USA",
            difficulty = "Moderate",
            distanceMeters = 8700.0,
            elevationGainMeters = 610.0,
            estimatedDurationMinutes = 210,
            trailType = "Loop",
            description = "World-famous canyon trek featuring 600 stone steps carved beside thundering Vernal Fall and the panoramic heights of Nevada Fall in Yosemite Valley.",
            highlights = listOf(
                "317-foot Vernal Fall spray zone",
                "Emerald Pool and Silver Apron",
                "594-foot Nevada Fall summit",
                "Granite cliffs of Liberty Cap & Half Dome"
            ),
            recommendedSeason = "April – October",
            points = points,
            waypoints = listOf(
                TrekWaypoint(title = "Happy Isles Trailhead", type = WaypointType.TRAILHEAD, latitude = 37.7345, longitude = -119.5663, altitude = 1225.0, note = "Shuttle stop #16"),
                TrekWaypoint(title = "Vernal Fall Footbridge", type = WaypointType.WATER_SOURCE, latitude = 37.7285, longitude = -119.5540, altitude = 1370.0, note = "Water filling station"),
                TrekWaypoint(title = "Top of Vernal Fall", type = WaypointType.VIEWPOINT, latitude = 37.7260, longitude = -119.5480, altitude = 1490.0, note = "Rock guardrail vista"),
                TrekWaypoint(title = "Nevada Fall Summit", type = WaypointType.SUMMIT, latitude = 37.7210, longitude = -119.5320, altitude = 1820.0, note = "Peak overlook of Liberty Cap")
            )
        )
    }

    // 3. Everest Base Camp: Namche Bazaar to Tengboche (Nepal)
    private val everestBaseCampSection: Trail by lazy {
        val rawPoints = listOf(
            Triple(27.8069, 86.7140, 3440.0),
            Triple(27.8130, 86.7210, 3560.0),
            Triple(27.8200, 86.7290, 3620.0), // Sanasa
            Triple(27.8280, 86.7360, 3480.0),
            Triple(27.8340, 86.7420, 3250.0), // Phunki Thenga river crossing
            Triple(27.8380, 86.7490, 3490.0),
            Triple(27.8420, 86.7560, 3710.0),
            Triple(27.8465, 86.7640, 3867.0)  // Tengboche Monastery
        )
        val points = interpolatePoints(rawPoints)
        Trail(
            id = "trail_everest_namche_tengboche",
            name = "Everest Trail: Namche to Tengboche",
            region = "Khumbu, Sagarmatha National Park",
            country = "Nepal",
            difficulty = "Expert",
            distanceMeters = 9800.0,
            elevationGainMeters = 890.0,
            estimatedDurationMinutes = 300,
            trailType = "Point-to-Point",
            description = "High-altitude Himalayan trek across suspension bridges and rhododendron forests, framed by Ama Dablam, Lhotse, and Mount Everest.",
            highlights = listOf(
                "Direct views of Mt. Everest & Ama Dablam",
                "Historic Tengboche Buddhist Monastery (3,867m)",
                "Dudh Koshi suspension bridge",
                "Himalayan Tahr & musk deer sightings"
            ),
            recommendedSeason = "March – May & Sept – Nov",
            points = points,
            waypoints = listOf(
                TrekWaypoint(title = "Namche Bazaar Gate", type = WaypointType.TRAILHEAD, latitude = 27.8069, longitude = 86.7140, altitude = 3440.0, note = "Sherpa capital hub"),
                TrekWaypoint(title = "Everest Viewpoint Ridge", type = WaypointType.VIEWPOINT, latitude = 27.8200, longitude = 86.7290, altitude = 3620.0, note = "First panoramic glimpse of Everest"),
                TrekWaypoint(title = "Phunki Thenga Bridge", type = WaypointType.WATER_SOURCE, latitude = 27.8340, longitude = 86.7420, altitude = 3250.0, note = "Glacial river suspension crossing"),
                TrekWaypoint(title = "Tengboche Monastery", type = WaypointType.SUMMIT, latitude = 27.8465, longitude = 86.7640, altitude = 3867.0, note = "High spiritual sanctuary")
            )
        )
    }

    // 4. Zion Angels Landing (Utah, USA)
    private val zionAngelsLanding: Trail by lazy {
        val rawPoints = listOf(
            Triple(37.2592, -112.9515, 1310.0),
            Triple(37.2625, -112.9490, 1370.0),
            Triple(37.2660, -112.9470, 1490.0), // Refrigerator Canyon
            Triple(37.2690, -112.9460, 1610.0), // Walter's Wiggles 21 switchbacks
            Triple(37.2715, -112.9475, 1660.0), // Scout Lookout
            Triple(37.2735, -112.9460, 1720.0), // The Spine & Chains
            Triple(37.2755, -112.9470, 1765.0)  // Angels Landing Summit
        )
        val points = interpolatePoints(rawPoints)
        Trail(
            id = "trail_zion_angels_landing",
            name = "Angels Landing Knife-Edge Trail",
            region = "Zion National Park, Utah",
            country = "USA",
            difficulty = "Expert",
            distanceMeters = 8200.0,
            elevationGainMeters = 455.0,
            estimatedDurationMinutes = 190,
            trailType = "Out-and-Back",
            description = "Thrilling red-rock ridge hike navigating Walter's Wiggles and a sheer sandstone spine with chain handrails overlooking Zion Canyon 1,500 feet below.",
            highlights = listOf(
                "Walter's Wiggles 21 stone switchbacks",
                "Sheer knife-edge sandstone ridge",
                "360° Zion Canyon amphitheater panorama",
                "Chain-assisted aerial scramble"
            ),
            recommendedSeason = "March – May & Sept – November",
            points = points,
            waypoints = listOf(
                TrekWaypoint(title = "The Grotto Trailhead", type = WaypointType.TRAILHEAD, latitude = 37.2592, longitude = -112.9515, altitude = 1310.0, note = "Zion Canyon Scenic Drive"),
                TrekWaypoint(title = "Refrigerator Canyon", type = WaypointType.REST_STOP, latitude = 37.2660, longitude = -112.9470, altitude = 1490.0, note = "Cool shaded canyon break"),
                TrekWaypoint(title = "Scout Lookout", type = WaypointType.VIEWPOINT, latitude = 37.2715, longitude = -112.9475, altitude = 1660.0, note = "Permit checkpoint & cliff view"),
                TrekWaypoint(title = "Angels Landing Summit", type = WaypointType.SUMMIT, latitude = 37.2755, longitude = -112.9470, altitude = 1765.0, note = "Spectacular canyon perch")
            )
        )
    }

    // 5. Eiger Trail & North Face (Switzerland)
    private val eigerTrailSwitzerland: Trail by lazy {
        val rawPoints = listOf(
            Triple(46.5845, 7.9620, 2320.0), // Eigergletscher station
            Triple(46.5820, 7.9710, 2260.0),
            Triple(46.5805, 7.9820, 2170.0), // Below North Face
            Triple(46.5790, 7.9940, 2040.0),
            Triple(46.5810, 8.0060, 1890.0),
            Triple(46.5850, 8.0180, 1750.0),
            Triple(46.5910, 8.0260, 1616.0)  // Alpiglen
        )
        val points = interpolatePoints(rawPoints)
        Trail(
            id = "trail_eiger_north_face",
            name = "Eiger Trail: Direct Below the North Wall",
            region = "Bernese Oberland, Grindelwald",
            country = "Switzerland",
            difficulty = "Moderate",
            distanceMeters = 6100.0,
            elevationGainMeters = 120.0,
            estimatedDurationMinutes = 140,
            trailType = "Point-to-Point",
            description = "Walk directly beneath the legendary 1,800-meter Eiger Nordwand, observing climber routes, cascading meltwater falls, and Grindelwald valley below.",
            highlights = listOf(
                "Towering 1,800m Eiger North Face wall",
                "Grindelwald glacier valley overlook",
                "Mönch and Jungfrau alpine backdrop",
                "Alpine flowers and mountain streams"
            ),
            recommendedSeason = "Late June – October",
            points = points,
            waypoints = listOf(
                TrekWaypoint(title = "Eigergletscher Station", type = WaypointType.TRAILHEAD, latitude = 46.5845, longitude = 7.9620, altitude = 2320.0, note = "Railway departure point"),
                TrekWaypoint(title = "Climber's Memorial Plaque", type = WaypointType.VIEWPOINT, latitude = 46.5805, longitude = 7.9820, altitude = 2170.0, note = "Directly under the sheer wall"),
                TrekWaypoint(title = "Wart Spring", type = WaypointType.WATER_SOURCE, latitude = 46.5810, longitude = 8.0060, altitude = 1890.0, note = "Clear glacial brook"),
                TrekWaypoint(title = "Alpiglen Station", type = WaypointType.REST_STOP, latitude = 46.5910, longitude = 8.0260, altitude = 1616.0, note = "Traditional Swiss mountain inn")
            )
        )
    }

    // 6. Inca Trail to Sun Gate (Peru)
    private val incaTrailMachuPicchu: Trail by lazy {
        val rawPoints = listOf(
            Triple(-13.2420, -72.5310, 2680.0), // Wiñay Wayna
            Triple(-13.2360, -72.5350, 2710.0),
            Triple(-13.2280, -72.5400, 2730.0), // Intipunku (Sun Gate)
            Triple(-13.2210, -72.5430, 2620.0),
            Triple(-13.2160, -72.5460, 2510.0),
            Triple(-13.2110, -72.5490, 2430.0)  // Machu Picchu citadel
        )
        val points = interpolatePoints(rawPoints)
        Trail(
            id = "trail_inca_sun_gate",
            name = "Inca Trail: Wiñay Wayna to Sun Gate",
            region = "Cusco, Sacred Valley",
            country = "Peru",
            difficulty = "Moderate",
            distanceMeters = 6400.0,
            elevationGainMeters = 340.0,
            estimatedDurationMinutes = 160,
            trailType = "Point-to-Point",
            description = "Ancient Incan stone highway traversing sub-tropical cloud forests culminating in the iconic aerial entrance through Intipunku overlooking Machu Picchu.",
            highlights = listOf(
                "Wiñay Wayna agricultural terraces",
                "Intipunku (Sun Gate) entrance",
                "Cloud forest orchids & birdlife",
                "Classic Machu Picchu citadel overlook"
            ),
            recommendedSeason = "May – September",
            points = points,
            waypoints = listOf(
                TrekWaypoint(title = "Wiñay Wayna Ruins", type = WaypointType.TRAILHEAD, latitude = -13.2420, longitude = -72.5310, altitude = 2680.0, note = "Incan stone terraces"),
                TrekWaypoint(title = "The 'Gringo Killer' Stone Steps", type = WaypointType.HAZARD, latitude = -13.2360, longitude = -72.5350, altitude = 2710.0, note = "Steep final hand-carved staircase"),
                TrekWaypoint(title = "Intipunku (Sun Gate)", type = WaypointType.VIEWPOINT, latitude = -13.2280, longitude = -72.5400, altitude = 2730.0, note = "First panoramic sighting of citadel"),
                TrekWaypoint(title = "Machu Picchu Upper Overlook", type = WaypointType.SUMMIT, latitude = -13.2110, longitude = -72.5490, altitude = 2430.0, note = "Final historical viewpoint")
            )
        )
    }

    // 7. Mount Fuji Yoshida Trail (Japan)
    private val mountFujiTrail: Trail by lazy {
        val rawPoints = listOf(
            Triple(35.3900, 138.7320, 2305.0), // Fuji Subaru 5th Station
            Triple(35.3830, 138.7335, 2390.0), // 6th station
            Triple(35.3760, 138.7340, 2700.0), // 7th station
            Triple(35.3690, 138.7345, 3100.0), // 8th station
            Triple(35.3640, 138.7330, 3400.0), // 8.5 station
            Triple(35.3606, 138.7275, 3776.0)  // Kengamine Peak (Fuji Summit)
        )
        val points = interpolatePoints(rawPoints)
        Trail(
            id = "trail_mount_fuji_yoshida",
            name = "Mount Fuji: Yoshida Summit Trail",
            region = "Yamanashi Prefecture",
            country = "Japan",
            difficulty = "Hard",
            distanceMeters = 7500.0,
            elevationGainMeters = 1471.0,
            estimatedDurationMinutes = 320,
            trailType = "Out-and-Back",
            description = "Sacred volcanic pilgrimage winding through volcanic lava fields and mountain huts up to Japan's highest volcano crater summit.",
            highlights = listOf(
                "3,776m Kengamine crater summit",
                "Goraiko (sunrise above the clouds)",
                "Historic torii shrine gates on summit",
                "Volcanic caldera crater rim walk"
            ),
            recommendedSeason = "July 1 – September 10",
            points = points,
            waypoints = listOf(
                TrekWaypoint(title = "Fuji Subaru 5th Station", type = WaypointType.TRAILHEAD, latitude = 35.3900, longitude = 138.7320, altitude = 2305.0, note = "Equipment rental & acclimatization"),
                TrekWaypoint(title = "7th Station Rock Band", type = WaypointType.REST_STOP, latitude = 35.3760, longitude = 138.7340, altitude = 2700.0, note = "First mountain huts & tea"),
                TrekWaypoint(title = "Original 8th Station", type = WaypointType.CAMPSITE, latitude = 35.3690, longitude = 138.7345, altitude = 3100.0, note = "Night rest for sunrise summit"),
                TrekWaypoint(title = "Kengamine Peak (Mt Fuji Summit)", type = WaypointType.SUMMIT, latitude = 35.3606, longitude = 138.7275, altitude = 3776.0, note = "Highest point in Japan")
            )
        )
    }

    private fun interpolatePoints(raw: List<Triple<Double, Double, Double>>): List<GpsPoint> {
        val result = mutableListOf<GpsPoint>()
        var timestamp = System.currentTimeMillis() - (raw.size * 300_000L)
        for (i in 0 until raw.size - 1) {
            val (lat1, lng1, alt1) = raw[i]
            val (lat2, lng2, alt2) = raw[i + 1]
            val steps = 5
            for (s in 0 until steps) {
                val fraction = s.toDouble() / steps
                val lat = lat1 + (lat2 - lat1) * fraction
                val lng = lng1 + (lng2 - lng1) * fraction
                val alt = alt1 + (alt2 - alt1) * fraction
                result.add(
                    GpsPoint(
                        latitude = lat,
                        longitude = lng,
                        altitude = alt,
                        speed = 1.3f,
                        accuracy = 3.5f,
                        timestamp = timestamp
                    )
                )
                timestamp += 60_000L
            }
        }
        val (lastLat, lastLng, lastAlt) = raw.last()
        result.add(
            GpsPoint(
                latitude = lastLat,
                longitude = lastLng,
                altitude = lastAlt,
                speed = 1.2f,
                accuracy = 3.0f,
                timestamp = timestamp
            )
        )
        return result
    }
}
