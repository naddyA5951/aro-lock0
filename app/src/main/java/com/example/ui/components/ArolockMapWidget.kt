package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.NightCardBorder
import com.example.ui.theme.NightSurface
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

enum class MapLayerStyle(
    val key: String,
    val label: String,
    val subtitle: String,
    val iconEmoji: String,
    val baseColor: Color
) {
    TOPO(
        key = "TOPO",
        label = "Outdoor Topo",
        subtitle = "Contours & mountain relief",
        iconEmoji = "🏔️",
        baseColor = Color(0xFF0F1E16)
    ),
    SATELLITE(
        key = "SATELLITE",
        label = "Satellite Aerial",
        subtitle = "High-res Earth imagery",
        iconEmoji = "🛰️",
        baseColor = Color(0xFF0B1218)
    ),
    TERRAIN(
        key = "TERRAIN",
        label = "Mountain Relief",
        subtitle = "Hillshading & ridge peaks",
        iconEmoji = "🧭",
        baseColor = Color(0xFF14191C)
    ),
    DARK_TACTICAL(
        key = "DARK",
        label = "Dark Tactical",
        subtitle = "Night OLED & luminescent trail",
        iconEmoji = "🌑",
        baseColor = Color(0xFF07090A)
    ),
    STREET(
        key = "STREET",
        label = "OpenStreetMap",
        subtitle = "Worldwide trail roads & landmarks",
        iconEmoji = "🗺️",
        baseColor = Color(0xFF161F24)
    ),
    NEON_TRAIL(
        key = "NEON",
        label = "High-Visibility Neon",
        subtitle = "Glacier & direct sunlight HUD",
        iconEmoji = "⚡",
        baseColor = Color(0xFF0B141C)
    ),
    NATGEO(
        key = "NATGEO",
        label = "National Geographic",
        subtitle = "Classic NatGeo cartography & landforms",
        iconEmoji = "🌎",
        baseColor = Color(0xFF141915)
    ),
    CYCLOSM(
        key = "CYCLOSM",
        label = "Cycling & Hiking Trails",
        subtitle = "Dedicated trail networks & elevation grades",
        iconEmoji = "🚴",
        baseColor = Color(0xFF131B15)
    ),
    WINTER_ALPINE(
        key = "WINTER",
        label = "Winter Alpine Snow",
        subtitle = "High-contrast snow peaks & glacier routes",
        iconEmoji = "❄️",
        baseColor = Color(0xFF1A1F26)
    ),
    USGS_SHADED(
        key = "USGS",
        label = "USGS Shaded Relief",
        subtitle = "Detailed elevation contour shading",
        iconEmoji = "⛰️",
        baseColor = Color(0xFF151815)
    );

    companion object {
        fun fromKey(key: String): MapLayerStyle {
            return values().firstOrNull { it.key.equals(key, ignoreCase = true) } ?: TOPO
        }
    }
}

private val sharedTileClient by lazy {
    OkHttpClient.Builder().build()
}

/**
 * Universal interactive route and trail map widget.
 * Features:
 * - 6 Switchable Map Styles (Outdoor Topo, Satellite Aerial, Mountain Relief, Dark Tactical, OSM, Neon)
 * - Real tile caching & rendering with vector contour fallback
 * - Reference guide trail & live recorded GPS trail paths
 * - Waypoint pins (Summits, Water, Campsites, Viewpoints, Photos, Hazards)
 * - Layer switcher dialog with visual cards
 * - Recenter on user, Follow mode, Zoom controls, Compass
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
    initialZoom: Float = 14.2f,
    initialStyle: MapLayerStyle = MapLayerStyle.TOPO,
    onMapStyleChanged: ((MapLayerStyle) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mapStyle by remember { mutableStateOf(initialStyle) }
    var showStyleChooserDialog by remember { mutableStateOf(false) }
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
            .background(mapStyle.baseColor)
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

                                    val tappedWaypoint = waypoints.firstOrNull { wp ->
                                        val wpWorld = latLngToWorldMercator(wp.latitude, wp.longitude, zoomLevel)
                                        val wpScreen = worldMercatorToScreen(wpWorld, centerWorld, width, height)
                                        val dist = sqrt((tapOffset.x - wpScreen.x).pow(2) + (tapOffset.y - wpScreen.y).pow(2))
                                        dist < 32f
                                    }
                                    selectedWaypoint = tappedWaypoint
                                }
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    followUser = false
                                    zoomLevel = (zoomLevel * zoom).coerceIn(3.0f, 18.5f)

                                    val scale = 256.0 * 2.0.pow(zoomLevel.toDouble())
                                    val dLng = -(pan.x / scale) * 360.0
                                    val latRad = centerLat * PI / 180.0
                                    val dLat = (pan.y / scale) * 360.0 * cos(latRad)

                                    centerLng = (centerLng + dLng).coerceIn(-180.0, 180.0)
                                    centerLat = (centerLat + dLat).coerceIn(-85.0, 85.0)
                                }
                            }
                    } else Modifier
                )
        ) {
            val width = size.width
            val height = size.height
            val intZoom = zoomLevel.toInt().coerceIn(0, 18)
            val centerWorld = latLngToWorldMercator(centerLat, centerLng, zoomLevel)

            // Draw base background color based on map style
            drawRect(color = mapStyle.baseColor)

            // Draw offline topographic contour simulation lines
            drawTopoContourGrid(width, height, centerLat, centerLng, zoomLevel, mapStyle)

            // Render visible tiles for the selected MapLayerStyle
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
                    val tileKey = "${mapStyle.key}_${intZoom}_${tx}_$ty"
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
                        val tileCacheDir = File(context.cacheDir, "tiles_${mapStyle.key.lowercase()}")
                        if (!tileCacheDir.exists()) tileCacheDir.mkdirs()
                        val tileFile = File(tileCacheDir, "${intZoom}_${tx}_$ty.png")

                        coroutineScope.launch(Dispatchers.IO) {
                            val bmp = loadOrDownloadTile(tileFile, mapStyle, intZoom, tx, ty)
                            if (bmp != null) {
                                withContext(Dispatchers.Main) {
                                    tileBitmaps[tileKey] = bmp
                                }
                            }
                        }
                    }
                }
            }

            // Contrast enhancement overlay to keep route polyline bright
            val overlayColor = when (mapStyle) {
                MapLayerStyle.TOPO -> Color(0x25002010)
                MapLayerStyle.SATELLITE -> Color(0x35000000)
                MapLayerStyle.TERRAIN -> Color(0x20151000)
                MapLayerStyle.DARK_TACTICAL -> Color(0x30000000)
                MapLayerStyle.NEON_TRAIL -> Color(0x18001525)
                MapLayerStyle.STREET -> Color(0x20000000)
                MapLayerStyle.NATGEO -> Color(0x15001000)
                MapLayerStyle.CYCLOSM -> Color(0x15001500)
                MapLayerStyle.WINTER_ALPINE -> Color(0x12000515)
                MapLayerStyle.USGS_SHADED -> Color(0x18101510)
            }
            drawRect(color = overlayColor)

            // 1. Draw Reference Guide Trail Path (Cyan/Dashed)
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
                drawPath(
                    path = refPath,
                    color = Color(0xFF00B4D8).copy(alpha = 0.8f),
                    style = Stroke(
                        width = 4.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
                    )
                )
            }

            // 2. Draw Recorded Route Polyline (Vibrant glowing trail)
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
                val glowColor = when (mapStyle) {
                    MapLayerStyle.NEON_TRAIL -> Color(0xFF00F5D4).copy(alpha = 0.5f)
                    MapLayerStyle.DARK_TACTICAL -> AmberGold.copy(alpha = 0.5f)
                    else -> AmberGold.copy(alpha = 0.35f)
                }
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
                val routeBrush = when (mapStyle) {
                    MapLayerStyle.NEON_TRAIL -> Brush.linearGradient(
                        colors = listOf(Color(0xFF00F5D4), Color(0xFF7B2CBF), Color(0xFFFF007F))
                    )
                    else -> Brush.linearGradient(
                        colors = listOf(ForestGreen, AmberGold, Terracotta)
                    )
                }
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
                val startP = trackPoints.first()
                val startScr = worldMercatorToScreen(latLngToWorldMercator(startP.latitude, startP.longitude, zoomLevel), centerWorld, width, height)
                drawCircle(color = Color.White, radius = 9f, center = startScr)
                drawCircle(color = ForestGreen, radius = 7f, center = startScr)

                if (trackPoints.size >= 2) {
                    val endP = trackPoints.last()
                    val endScr = worldMercatorToScreen(latLngToWorldMercator(endP.latitude, endP.longitude, zoomLevel), centerWorld, width, height)
                    drawCircle(color = Color.White, radius = 9f, center = endScr)
                    drawCircle(color = DangerRed, radius = 7f, center = endScr)
                }
            }

            // 4. Draw Waypoints Pins
            for (wp in waypoints) {
                val wpScr = worldMercatorToScreen(latLngToWorldMercator(wp.latitude, wp.longitude, zoomLevel), centerWorld, width, height)
                val pinColor = when (wp.type) {
                    WaypointType.SUMMIT -> AmberGold
                    WaypointType.WATER_SOURCE -> Color(0xFF48CAE4)
                    WaypointType.CAMPSITE -> SageGreen
                    WaypointType.VIEWPOINT -> Color(0xFFFFB703)
                    WaypointType.PHOTO_POINT -> Color(0xFFFF007F)
                    WaypointType.HAZARD -> DangerRed
                    WaypointType.REST_STOP -> Color(0xFF90E0EF)
                    WaypointType.TRAILHEAD -> ForestGreen
                    else -> AmberGold
                }

                drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = 13f, center = Offset(wpScr.x, wpScr.y + 2f))
                drawCircle(color = Color.White, radius = 11f, center = wpScr)
                drawCircle(color = pinColor, radius = 9f, center = wpScr)
                drawCircle(color = Color.White, radius = 3.5f, center = wpScr)
            }

            // 5. Draw Live User Location Dot with Pulsing Radar
            currentLocation?.let { loc ->
                val userScr = worldMercatorToScreen(latLngToWorldMercator(loc.latitude, loc.longitude, zoomLevel), centerWorld, width, height)

                // Outer animated radar pulse
                drawCircle(
                    color = SageGreen.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = userScr
                )
                // Accuracy circle
                if (loc.accuracy > 0f) {
                    val accuracyMeters = loc.accuracy.toDouble()
                    val scale = 256.0 * 2.0.pow(zoomLevel.toDouble())
                    val metersPerPixel = (156543.03392 * cos(loc.latitude * PI / 180.0)) / (2.0.pow(zoomLevel.toDouble()))
                    val radiusPx = (accuracyMeters / metersPerPixel).toFloat().coerceIn(12f, 140f)

                    drawCircle(
                        color = SageGreen.copy(alpha = 0.12f),
                        radius = radiusPx,
                        center = userScr
                    )
                    drawCircle(
                        color = SageGreen.copy(alpha = 0.35f),
                        radius = radiusPx,
                        center = userScr,
                        style = Stroke(width = 1.2f)
                    )
                }

                // Core GPS Location Dot
                drawCircle(color = Color.White, radius = 9f, center = userScr)
                drawCircle(color = SageGreen, radius = 7f, center = userScr)
                drawCircle(color = Color.White, radius = 2.5f, center = userScr)
            }
        }

        // Top Left: Map Style & Compass Badge
        Surface(
            color = NightCard.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .clickable(enabled = showControls) { showStyleChooserDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = mapStyle.iconEmoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = mapStyle.label,
                        color = TextPrimaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Zoom: ${String.format("%.1f", zoomLevel)}x",
                        color = TextSecondaryDark,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Map Control Floating Buttons (Layers, Recenter, Zoom +/-)
        if (showControls && isInteractive) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Layer Switcher Button
                IconButton(
                    onClick = { showStyleChooserDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = NightCard.copy(alpha = 0.92f),
                        contentColor = SageGreen
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("map_layers_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Map Styles",
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
                        containerColor = if (followUser) AmberGold else NightCard.copy(alpha = 0.92f),
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
                        containerColor = NightCard.copy(alpha = 0.92f),
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
                        containerColor = NightCard.copy(alpha = 0.92f),
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
        }

        // Full Interactive Map Style Chooser Dialog
        if (showStyleChooserDialog) {
            AlertDialog(
                onDismissRequest = { showStyleChooserDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Choose Map Style",
                                color = TextPrimaryDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select terrain layer & satellite view",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = { showStyleChooserDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MapLayerStyle.values().forEach { style ->
                            val isSelected = style == mapStyle
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SageGreen.copy(alpha = 0.18f) else NightSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) SageGreen else NightCardBorder
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        mapStyle = style
                                        onMapStyleChanged?.invoke(style)
                                        showStyleChooserDialog = false
                                        Toast.makeText(context, "${style.label} activated", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) SageGreen else NightCard,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = style.iconEmoji, fontSize = 18.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = style.label,
                                            color = if (isSelected) SageGreen else TextPrimaryDark,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = style.subtitle,
                                            color = TextSecondaryDark,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = SageGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showStyleChooserDialog = false }) {
                        Text("Dismiss", color = TextSecondaryDark)
                    }
                },
                containerColor = NightBlack,
                shape = RoundedCornerShape(18.dp)
            )
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
        MapLayerStyle.TOPO -> Color(0x2252B788)
        MapLayerStyle.SATELLITE -> Color(0x1648CAE4)
        MapLayerStyle.TERRAIN -> Color(0x20DDA15E)
        MapLayerStyle.DARK_TACTICAL -> Color(0x12FFFFFF)
        MapLayerStyle.NEON_TRAIL -> Color(0x2400F5D4)
        MapLayerStyle.STREET -> Color(0x163A86FF)
        MapLayerStyle.NATGEO -> Color(0x2052B788)
        MapLayerStyle.CYCLOSM -> Color(0x2238B000)
        MapLayerStyle.WINTER_ALPINE -> Color(0x1CB0D0D3)
        MapLayerStyle.USGS_SHADED -> Color(0x22A3B18A)
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

    // Topographic contour simulation lines
    val contourColor = lineColor.copy(alpha = lineColor.alpha * 1.6f)
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

private fun loadOrDownloadTile(file: File, style: MapLayerStyle, z: Int, x: Int, y: Int): Bitmap? {
    if (file.exists() && file.length() > 0) {
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    return try {
        val url = when (style) {
            MapLayerStyle.TOPO -> "https://tile.opentopomap.org/$z/$x/$y.png"
            MapLayerStyle.SATELLITE -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$z/$y/$x"
            MapLayerStyle.TERRAIN -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/$z/$y/$x"
            MapLayerStyle.DARK_TACTICAL -> "https://a.basemaps.cartocdn.com/dark_all/$z/$x/$y.png"
            MapLayerStyle.NEON_TRAIL -> "https://a.basemaps.cartocdn.com/rastertiles/voyager_labels_under/$z/$x/$y.png"
            MapLayerStyle.STREET -> "https://tile.openstreetmap.org/$z/$x/$y.png"
            MapLayerStyle.NATGEO -> "https://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/$z/$y/$x"
            MapLayerStyle.CYCLOSM -> "https://a.tile-cyclosm.openstreetmap.fr/cyclosm/$z/$x/$y.png"
            MapLayerStyle.WINTER_ALPINE -> "https://a.basemaps.cartocdn.com/light_all/$z/$x/$y.png"
            MapLayerStyle.USGS_SHADED -> "https://server.arcgisonline.com/ArcGIS/rest/services/World_Shaded_Relief/MapServer/tile/$z/$y/$x"
        }

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
