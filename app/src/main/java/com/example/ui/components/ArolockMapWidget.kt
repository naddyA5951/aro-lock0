package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GpsPoint
import com.example.model.TrekWaypoint
import com.example.model.WaypointType
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.PineDeep
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class MapLayerStyle(val label: String, val subtitle: String) {
    TOPO("Outdoor Topo", "Contours & trails"),
    STREET("OpenStreetMap", "Standard world map"),
    NIGHT("Night Trail", "High-contrast luminescent"),
    SATELLITE("Terrain Relief", "Elevation hillshade")
}

private val sharedTileClient by lazy {
    OkHttpClient.Builder().build()
}

/**
 * Universal interactive route and trail map widget.
 * Features:
 * - Topographic contour simulation + OpenStreetMap tile rendering
 * - Reference guide trail & live recorded GPS trail paths
 * - Waypoint pins (Summits, Water, Campsites, Viewpoints, Hazards)
 * - Layer switcher (Topo, Standard, Night, Terrain)
 * - Recenter on user, Follow mode, Zoom controls, Compass
 * - Works 100% offline with vector contour fallback
 */
@Composable
fun ArolockMapWidget(
    points: List<GpsPoint>,
    currentLocation: GpsPoint?,
    modifier: Modifier = Modifier,
    referencePoints: List<GpsPoint> = emptyList(),
    waypoints: List<TrekWaypoint> = emptyList(),
    isInteractive: Boolean = true,
    showControls: Boolean = true,
    autoCenterOnUser: Boolean = true,
    initialZoom: Float = 14.2f
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mapStyle by remember { mutableStateOf(MapLayerStyle.TOPO) }
    var showLayerMenu by remember { mutableStateOf(false) }
    var selectedWaypoint by remember { mutableStateOf<TrekWaypoint?>(null) }

    val defaultLat = currentLocation?.latitude
        ?: points.lastOrNull()?.latitude
        ?: referencePoints.firstOrNull()?.latitude
        ?: 45.9237 // Chamonix Mont Blanc
    val defaultLng = currentLocation?.longitude
        ?: points.lastOrNull()?.longitude
        ?: referencePoints.firstOrNull()?.longitude
        ?: 6.8694

    var centerLat by remember { mutableDoubleStateOf(defaultLat) }
    var centerLng by remember { mutableDoubleStateOf(defaultLng) }
    var zoomLevel by remember { mutableFloatStateOf(initialZoom) }
    var followUser by remember { mutableStateOf(autoCenterOnUser) }

    // Pulsing animation for live GPS user dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Tile cache in memory
    val tileBitmaps = remember { mutableStateMapOf<String, Bitmap>() }

    // Auto-center on user if follow mode is active
    LaunchedEffect(currentLocation, followUser) {
        if (followUser && currentLocation != null) {
            centerLat = currentLocation.latitude
            centerLng = currentLocation.longitude
        }
    }

    // Auto-fit bounds if displaying a static trek or reference trail
    LaunchedEffect(points, referencePoints) {
        val targetPoints = if (points.size >= 2) points else referencePoints
        if (targetPoints.size >= 2 && currentLocation == null) {
            var minLat = 90.0
            var maxLat = -90.0
            var minLng = 180.0
            var maxLng = -180.0
            for (p in targetPoints) {
                if (p.latitude < minLat) minLat = p.latitude
                if (p.latitude > maxLat) maxLat = p.latitude
                if (p.longitude < minLng) minLng = p.longitude
                if (p.longitude > maxLng) maxLng = p.longitude
            }
            centerLat = (minLat + maxLat) / 2.0
            centerLng = (minLng + maxLng) / 2.0
            followUser = false

            // Auto-scale zoom according to bounding box span
            val dLat = maxLat - minLat
            val dLng = maxLng - minLng
            val maxSpan = max(dLat, dLng)
            zoomLevel = when {
                maxSpan > 1.0 -> 8.5f
                maxSpan > 0.4 -> 10.5f
                maxSpan > 0.15 -> 12.0f
                maxSpan > 0.05 -> 13.5f
                maxSpan > 0.02 -> 14.5f
                else -> 15.5f
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(NightBlack)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("arolock_trail_map_canvas")
                .then(
                    if (isInteractive) {
                        Modifier
                            .pointerInput(Unit) {
                                detectTapGestures { tapOffset ->
                                    val width = size.width.toFloat()
                                    val height = size.height.toFloat()
                                    val centerWorld = latLngToWorldMercator(centerLat, centerLng, zoomLevel)

                                    // Check if tapped near any waypoint (within 28dp)
                                    val tappedWp = waypoints.firstOrNull { wp ->
                                        val wpWorld = latLngToWorldMercator(wp.latitude, wp.longitude, zoomLevel)
                                        val wpScreen = worldMercatorToScreen(wpWorld, centerWorld, width, height)
                                        val dist = sqrt((wpScreen.x - tapOffset.x).pow(2) + (wpScreen.y - tapOffset.y).pow(2))
                                        dist < 40f
                                    }
                                    selectedWaypoint = tappedWp
                                }
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    followUser = false
                                    zoomLevel = (zoomLevel * zoom).coerceIn(3.0f, 18.5f)

                                    val scale = 256.0 * 2.0.pow(zoomLevel.toDouble())
                                    val dLng = -pan.x / scale * 360.0
                                    val rad = centerLat * PI / 180.0
                                    val dLat = (pan.y / scale) * (360.0 * cos(rad))

                                    centerLng = (centerLng + dLng).coerceIn(-180.0, 180.0)
                                    centerLat = (centerLat + dLat).coerceIn(-85.0, 85.0)
                                }
                            }
                    } else Modifier
                )
        ) {
            val width = size.width
            val height = size.height
            val intZoom = zoomLevel.toInt().coerceIn(1, 18)
            val centerWorld = latLngToWorldMercator(centerLat, centerLng, zoomLevel)

            // Background base color based on map style
            val bgColor = when (mapStyle) {
                MapLayerStyle.TOPO -> Color(0xFF0F1E16)
                MapLayerStyle.STREET -> Color(0xFF161F24)
                MapLayerStyle.NIGHT -> Color(0xFF090D0C)
                MapLayerStyle.SATELLITE -> Color(0xFF141917)
            }
            drawRect(color = bgColor)

            // Draw offline topographic contour simulation lines
            drawTopoContourGrid(width, height, centerLat, centerLng, zoomLevel, mapStyle)

            // Render visible OpenStreetMap tiles if not in pure night stealth
            if (mapStyle != MapLayerStyle.NIGHT) {
                val tileSize = 256f * (2.0.pow((zoomLevel - intZoom).toDouble())).toFloat()
                val centerTileX = lon2tile(centerLng, intZoom)
                val centerTileY = lat2tile(centerLat, intZoom)

                val tilesX = (width / tileSize).toInt() + 2
                val tilesY = (height / tileSize).toInt() + 2

                val minTileX = max(0, centerTileX - tilesX / 2)
                val maxTileX = min((1 shl intZoom) - 1, centerTileX + tilesX / 2 + 1)
                val minTileY = max(0, centerTileY - tilesY / 2)
                val maxTileY = min((1 shl intZoom) - 1, centerTileY + tilesY / 2 + 1)

                for (tx in minTileX..maxTileX) {
                    for (ty in minTileY..maxTileY) {
                        val tileKey = "$intZoom/$tx/$ty"
                        val cachedBmp = tileBitmaps[tileKey]

                        val tileNWLat = tile2lat(ty, intZoom)
                        val tileNWLng = tile2lon(tx, intZoom)
                        val tilePos = worldMercatorToScreen(
                            latLngToWorldMercator(tileNWLat, tileNWLng, zoomLevel),
                            centerWorld,
                            width,
                            height
                        )

                        if (cachedBmp != null) {
                            try {
                                drawImage(
                                    image = cachedBmp.asImageBitmap(),
                                    dstOffset = androidx.compose.ui.unit.IntOffset(tilePos.x.toInt(), tilePos.y.toInt()),
                                    dstSize = androidx.compose.ui.unit.IntSize(tileSize.toInt() + 1, tileSize.toInt() + 1)
                                )
                            } catch (_: Exception) {}
                        } else {
                            val tileCacheDir = File(context.cacheDir, "osm_tiles")
                            if (!tileCacheDir.exists()) tileCacheDir.mkdirs()
                            val tileFile = File(tileCacheDir, "${intZoom}_${tx}_$ty.png")

                            coroutineScope.launch(Dispatchers.IO) {
                                val bmp = loadOrDownloadTile(tileFile, intZoom, tx, ty)
                                if (bmp != null) {
                                    withContext(Dispatchers.Main) {
                                        tileBitmaps[tileKey] = bmp
                                    }
                                }
                            }
                        }
                    }
                }

                // Semi-transparent overlay to keep trail polyline readable
                val overlayColor = when (mapStyle) {
                    MapLayerStyle.TOPO -> Color(0x35002414)
                    MapLayerStyle.SATELLITE -> Color(0x50000000)
                    else -> Color(0x25000000)
                }
                drawRect(color = overlayColor)
            }

            // 1. Draw Reference Trail Path (Cyan/Dashed Guide Line)
            if (referencePoints.size >= 2) {
                val refPath = Path()
                var first = true
                for (p in referencePoints) {
                    val scrPt = worldMercatorToScreen(
                        latLngToWorldMercator(p.latitude, p.longitude, zoomLevel),
                        centerWorld, width, height
                    )
                    if (first) {
                        refPath.moveTo(scrPt.x, scrPt.y)
                        first = false
                    } else {
                        refPath.lineTo(scrPt.x, scrPt.y)
                    }
                }
                // Guide Trail Path
                drawPath(
                    path = refPath,
                    color = Color(0xFF00B4D8).copy(alpha = 0.75f),
                    style = Stroke(
                        width = 4.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
                    )
                )
            }

            // 2. Draw Recorded Trail Route Polyline (Vibrant Gradient)
            if (points.size >= 2) {
                val routePath = Path()
                var first = true
                for (p in points) {
                    val scrPt = worldMercatorToScreen(
                        latLngToWorldMercator(p.latitude, p.longitude, zoomLevel),
                        centerWorld, width, height
                    )
                    if (first) {
                        routePath.moveTo(scrPt.x, scrPt.y)
                        first = false
                    } else {
                        routePath.lineTo(scrPt.x, scrPt.y)
                    }
                }

                // Route Outer Glow
                val glowColor = if (mapStyle == MapLayerStyle.NIGHT) AmberGold.copy(alpha = 0.5f) else AmberGold.copy(alpha = 0.35f)
                drawPath(
                    path = routePath,
                    color = glowColor,
                    style = Stroke(
                        width = 12f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Route Main Polyline
                val routeBrush = Brush.linearGradient(
                    colors = listOf(ForestGreen, AmberGold, Terracotta)
                )
                drawPath(
                    path = routePath,
                    brush = routeBrush,
                    style = Stroke(
                        width = 6f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 3. Draw Start & End Markers
            val trackPoints = if (points.isNotEmpty()) points else referencePoints
            if (trackPoints.isNotEmpty()) {
                val start = trackPoints.first()
                val startScreen = worldMercatorToScreen(
                    latLngToWorldMercator(start.latitude, start.longitude, zoomLevel),
                    centerWorld, width, height
                )
                drawCircle(color = NightBlack, radius = 9f, center = startScreen)
                drawCircle(color = SageGreen, radius = 7f, center = startScreen)
                drawCircle(color = Color.White, radius = 2.5f, center = startScreen)
            }

            if (trackPoints.size > 1 && currentLocation == null) {
                val end = trackPoints.last()
                val endScreen = worldMercatorToScreen(
                    latLngToWorldMercator(end.latitude, end.longitude, zoomLevel),
                    centerWorld, width, height
                )
                drawCircle(color = NightBlack, radius = 9f, center = endScreen)
                drawCircle(color = DangerRed, radius = 7f, center = endScreen)
                drawCircle(color = Color.White, radius = 2.5f, center = endScreen)
            }

            // 4. Draw Waypoint Markers
            for (wp in waypoints) {
                val wpScreen = worldMercatorToScreen(
                    latLngToWorldMercator(wp.latitude, wp.longitude, zoomLevel),
                    centerWorld, width, height
                )
                val wpColor = when (wp.type) {
                    WaypointType.SUMMIT -> Color(0xFF9D4EDD)
                    WaypointType.WATER_SOURCE -> Color(0xFF00B4D8)
                    WaypointType.CAMPSITE -> SageGreen
                    WaypointType.VIEWPOINT -> AmberGold
                    WaypointType.HAZARD -> DangerRed
                    WaypointType.TRAILHEAD -> ForestGreen
                    WaypointType.REST_STOP -> Color(0xFF48CAE4)
                    WaypointType.PHOTO_POINT -> Color(0xFFF77F00)
                }

                // Outer pin base
                drawCircle(color = NightBlack, radius = 11f, center = wpScreen)
                drawCircle(color = wpColor, radius = 8.5f, center = wpScreen)
                drawCircle(color = Color.White, radius = 3.5f, center = wpScreen)
            }

            // 5. Draw Live User Location Pin & Accuracy Pulse
            if (currentLocation != null) {
                val userScreen = worldMercatorToScreen(
                    latLngToWorldMercator(currentLocation.latitude, currentLocation.longitude, zoomLevel),
                    centerWorld, width, height
                )

                // Pulse Wave
                drawCircle(
                    color = AmberGold.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = userScreen
                )

                // Accuracy circle
                if (currentLocation.accuracy > 0f) {
                    val accRadiusPx = (currentLocation.accuracy * 2.0.pow(zoomLevel.toDouble()) / 156543.03392).toFloat()
                    drawCircle(
                        color = AmberGold.copy(alpha = 0.15f),
                        radius = accRadiusPx.coerceIn(14f, 110f),
                        center = userScreen
                    )
                }

                // Inner Solid Marker Pin
                drawCircle(color = NightBlack, radius = 10f, center = userScreen)
                drawCircle(color = AmberGold, radius = 7f, center = userScreen)
                drawCircle(color = Color.White, radius = 3f, center = userScreen)
            }
        }

        // Selected Waypoint Callout Tooltip
        selectedWaypoint?.let { wp ->
            Surface(
                color = NightCard.copy(alpha = 0.95f),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 24.dp, end = 24.dp)
                    .clickable { selectedWaypoint = null }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = wp.type.iconSymbol, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = wp.title,
                            color = TextPrimaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${wp.type.label} • ${wp.altitude.toInt()}m${if (wp.note.isNotEmpty()) " • " + wp.note else ""}",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Attribution & Current Layer Badge
        Surface(
            color = NightCard.copy(alpha = 0.82f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Terrain,
                    contentDescription = null,
                    tint = SageGreen,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${mapStyle.label} • OSM",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Map Control Floating Buttons (Recenter, Layer Switcher, Zoom +/-)
        if (showControls && isInteractive) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Layer Switcher Button
                IconButton(
                    onClick = { showLayerMenu = !showLayerMenu },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = NightCard.copy(alpha = 0.9f),
                        contentColor = SageGreen
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("map_layers_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Change Map Layer",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Recenter / Follow User Button
                IconButton(
                    onClick = {
                        followUser = true
                        currentLocation?.let {
                            centerLat = it.latitude
                            centerLng = it.longitude
                        } ?: points.lastOrNull()?.let {
                            centerLat = it.latitude
                            centerLng = it.longitude
                        } ?: referencePoints.firstOrNull()?.let {
                            centerLat = it.latitude
                            centerLng = it.longitude
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (followUser) AmberGold else NightCard.copy(alpha = 0.9f),
                        contentColor = if (followUser) NightBlack else Color.White
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("map_recenter_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter Map",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom In
                IconButton(
                    onClick = { zoomLevel = (zoomLevel + 1.0f).coerceAtMost(18.5f) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = NightCard.copy(alpha = 0.9f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("map_zoom_in_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Zoom Out
                IconButton(
                    onClick = { zoomLevel = (zoomLevel - 1.0f).coerceAtLeast(3.0f) },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = NightCard.copy(alpha = 0.9f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("map_zoom_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Layer Selection Dropdown / Overlay
            AnimatedVisibility(
                visible = showLayerMenu,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 64.dp)
            ) {
                Surface(
                    color = NightCard.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.width(170.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "MAP STYLES",
                            color = TextSecondaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        MapLayerStyle.values().forEach { style ->
                            val isSelected = style == mapStyle
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SageGreen.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        mapStyle = style
                                        showLayerMenu = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = style.label,
                                        color = if (isSelected) SageGreen else TextPrimaryDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = style.subtitle,
                                        color = TextSecondaryDark,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- MAP MATHEMATICS & PROJECTION ----------------

private fun latLngToWorldMercator(lat: Double, lng: Double, zoom: Float): Offset {
    val scale = 256.0 * 2.0.pow(zoom.toDouble())
    val x = ((lng + 180.0) / 360.0) * scale
    val sinLat = sin(lat * PI / 180.0).coerceIn(-0.9999, 0.9999)
    val y = (0.5 - ln((1.0 + sinLat) / (1.0 - sinLat)) / (4.0 * PI)) * scale
    return Offset(x.toFloat(), y.toFloat())
}

private fun worldMercatorToScreen(
    pointWorld: Offset,
    centerWorld: Offset,
    screenWidth: Float,
    screenHeight: Float
): Offset {
    val dx = pointWorld.x - centerWorld.x
    val dy = pointWorld.y - centerWorld.y
    return Offset(screenWidth / 2f + dx, screenHeight / 2f + dy)
}

private fun lon2tile(lon: Double, zoom: Int): Int {
    return floor((lon + 180.0) / 360.0 * (1 shl zoom)).toInt()
}

private fun lat2tile(lat: Double, zoom: Int): Int {
    val rad = lat * PI / 180.0
    return floor((1.0 - ln(tan(rad) + 1.0 / cos(rad)) / PI) / 2.0 * (1 shl zoom)).toInt()
}

private fun tile2lon(x: Int, z: Int): Double {
    return x.toDouble() / (1 shl z) * 360.0 - 180.0
}

private fun tile2lat(y: Int, z: Int): Double {
    val n = PI - 2.0 * PI * y.toDouble() / (1 shl z)
    return 180.0 / PI * kotlin.math.atan(0.5 * (kotlin.math.exp(n) - kotlin.math.exp(-n)))
}

private fun DrawScope.drawTopoContourGrid(
    width: Float,
    height: Float,
    lat: Double,
    lng: Double,
    zoom: Float,
    style: MapLayerStyle
) {
    val gridSpacing = 42f
    val lineColor = when (style) {
        MapLayerStyle.TOPO -> Color(0x2052B788)
        MapLayerStyle.NIGHT -> Color(0x18FFB703)
        MapLayerStyle.SATELLITE -> Color(0x18778DA9)
        MapLayerStyle.STREET -> Color(0x153A86FF)
    }

    var x = 0f
    while (x < width) {
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 1f
        )
        x += gridSpacing
    }
    var y = 0f
    while (y < height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f
        )
        y += gridSpacing
    }

    // Organic wavy contour simulation lines
    val contourColor = lineColor.copy(alpha = lineColor.alpha * 1.5f)
    for (i in 1..4) {
        val path = Path()
        val baseY = height * (i / 5f)
        path.moveTo(0f, baseY)
        var px = 0f
        while (px <= width) {
            val py = baseY + sin((px / 60f) + (lat * 10).toFloat() + i) * 16f
            path.lineTo(px, py)
            px += 20f
        }
        drawPath(
            path = path,
            color = contourColor,
            style = Stroke(width = 1.2f)
        )
    }
}

private fun loadOrDownloadTile(file: File, z: Int, x: Int, y: Int): Bitmap? {
    if (file.exists() && file.length() > 0) {
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    return try {
        val url = "https://tile.openstreetmap.org/$z/$x/$y.png"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "ArolockTrekkingApp/1.0 (Android; Universal-Device)")
            .build()

        sharedTileClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val bytes = response.body?.bytes()
                if (bytes != null) {
                    file.writeBytes(bytes)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else null
            } else null
        }
    } catch (_: Exception) {
        null
    }
}
