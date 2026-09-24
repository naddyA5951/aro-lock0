package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.TrekWaypoint
import com.example.model.WaypointType
import com.example.ui.components.ArolockMapWidget
import com.example.ui.components.ElevationChart
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.util.UnitFormatter
import com.example.viewmodel.TrekViewModel

@Composable
fun RecordingScreen(
    viewModel: TrekViewModel,
    onFinishTrek: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showFinishDialog by remember { mutableStateOf(false) }
    var showElevationSheet by remember { mutableStateOf(false) }
    var showAddWaypointDialog by remember { mutableStateOf(false) }
    var showPhotoCaptureDialog by remember { mutableStateOf(false) }
    var pendingCameraPhotoFile by remember { mutableStateOf<File?>(null) }

    fun onNewPhotoAdded(path: String) {
        viewModel.addMediaToActiveTrek(context, path)
        val lat = recordingState.lastKnownLocation?.latitude ?: 0.0
        val lon = recordingState.lastKnownLocation?.longitude ?: 0.0
        val alt = recordingState.currentAltitude
        val wp = TrekWaypoint(
            title = "Trail Photo #${recordingState.attachedMediaPaths.size + 1}",
            type = WaypointType.PHOTO_POINT,
            latitude = lat,
            longitude = lon,
            altitude = alt,
            note = path
        )
        viewModel.addWaypointToActiveTrek(context, wp)
        Toast.makeText(context, "Photo captured & geo-tagged!", Toast.LENGTH_SHORT).show()
    }

    val cameraPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraPhotoFile != null && pendingCameraPhotoFile!!.exists() && pendingCameraPhotoFile!!.length() > 0) {
            onNewPhotoAdded(pendingCameraPhotoFile!!.absolutePath)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val file = File(context.filesDir, "trek_live_${System.currentTimeMillis()}.jpg")
                pendingCameraPhotoFile = file
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                cameraPhotoLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera launch error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission needed", Toast.LENGTH_SHORT).show()
        }
    }

    val livePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val destFile = File(context.filesDir, "trek_live_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onNewPhotoAdded(destFile.absolutePath)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun triggerCameraCapture() {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            try {
                val file = File(context.filesDir, "trek_live_${System.currentTimeMillis()}.jpg")
                pendingCameraPhotoFile = file
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                cameraPhotoLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Check location and activity recognition permissions
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasLocationPermission = fineGranted || coarseGranted
        if (hasLocationPermission && !recordingState.isRecording) {
            viewModel.startTrek(context)
        }
    }

    fun requestAllTrekPermissionsAndStart() {
        val permissionsToRequest = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            viewModel.startTrek(context)
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NightBlack)
    ) {
        // Full screen interactive route map with reference trail & waypoints
        ArolockMapWidget(
            points = recordingState.points,
            currentLocation = recordingState.lastKnownLocation,
            referencePoints = recordingState.referenceTrailPoints,
            waypoints = recordingState.waypoints,
            isInteractive = true,
            showControls = true,
            autoCenterOnUser = true,
            initialStyle = com.example.ui.components.MapLayerStyle.fromKey(userProfile.preferredMapStyle),
            onMapStyleChanged = { style ->
                viewModel.setPreferredMapStyle(style.key)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top Status Header Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NightCard.copy(alpha = 0.9f))
                        .testTag("recording_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Live status badge with GPS precision & sensors
                Surface(
                    color = NightCard.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FiberManualRecord,
                            contentDescription = null,
                            tint = when {
                                !recordingState.isRecording -> TextSecondaryDark
                                recordingState.isPaused -> AmberGold
                                else -> DangerRed
                            },
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                !recordingState.isRecording -> "READY TO TREK"
                                recordingState.isPaused -> "PAUSED"
                                recordingState.gpsAccuracyMeters > 0f -> "LIVE GPS (±${recordingState.gpsAccuracyMeters.toInt()}m)"
                                else -> "RECORDING LIVE GPS"
                            },
                            color = TextPrimaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Capture / Upload Photo Action
                    if (recordingState.isRecording) {
                        IconButton(
                            onClick = { showPhotoCaptureDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (recordingState.attachedMediaPaths.isNotEmpty()) SageGreen else Color(0xFF48CAE4))
                                .testTag("snap_photo_button")
                        ) {
                            if (recordingState.attachedMediaPaths.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = AmberGold) {
                                            Text(
                                                "${recordingState.attachedMediaPaths.size}",
                                                fontSize = 9.sp,
                                                color = NightBlack,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Capture Photo",
                                        tint = NightBlack,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Capture Photo",
                                    tint = NightBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Waypoint Dropping Action
                    if (recordingState.isRecording) {
                        IconButton(
                            onClick = { showAddWaypointDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AmberGold)
                                .testTag("add_waypoint_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddLocation,
                                contentDescription = "Add Waypoint",
                                tint = NightBlack,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Elevation Chart Drawer Toggle
                    IconButton(
                        onClick = { showElevationSheet = !showElevationSheet },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (showElevationSheet) SageGreen else NightCard.copy(alpha = 0.9f))
                            .testTag("elevation_chart_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = "Elevation Profile",
                            tint = if (showElevationSheet) NightBlack else TextPrimaryDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Active Guide Trail Title Banner (if following a specific trail)
            if (!recordingState.activeTrailName.isNullOrEmpty()) {
                Surface(
                    color = NightCard.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Trail Guide: ${recordingState.activeTrailName}",
                        color = SageGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Elevation Profile Overlay Drawer
        AnimatedVisibility(
            visible = showElevationSheet,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 82.dp, start = 16.dp, end = 16.dp)
        ) {
            val chartPoints = if (recordingState.points.size >= 2) recordingState.points else recordingState.referenceTrailPoints
            ElevationChart(
                points = chartPoints,
                useMetric = userProfile.useMetric,
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(12.dp))
            )
        }

        // Bottom Dashboard HUD Cockpit
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = NightCard.copy(alpha = 0.96f)),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Primary Metrics Row: Distance & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "DISTANCE",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = UnitFormatter.formatDistance(recordingState.currentDistanceMeters, userProfile.useMetric),
                            color = TextPrimaryDark,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DURATION",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = UnitFormatter.formatDuration(recordingState.elapsedTimeSeconds),
                            color = AmberGold,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Metrics Grid Row 1: Real Steps, Live Speed, Pace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Physical Steps Counter
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsWalk,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("STEPS", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = UnitFormatter.formatSteps(recordingState.stepCount),
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (recordingState.cadenceSpm > 0) {
                            Text(
                                text = "${recordingState.cadenceSpm} spm",
                                color = SageGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Real Current Speed
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color(0xFF48CAE4),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SPEED", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = UnitFormatter.formatSpeed(recordingState.currentSpeedMps, userProfile.useMetric),
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Current Pace
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PACE", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = UnitFormatter.formatPace(recordingState.currentSpeedMps, userProfile.useMetric),
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Estimated Calories
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CALORIES", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${recordingState.estimatedCalories} kcal",
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Secondary Metrics Grid Row 2: Elevation Gain, Altitude, Waypoints
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Elevation Gain
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terrain,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ELEV. GAIN", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = UnitFormatter.formatElevation(recordingState.elevationGainMeters, userProfile.useMetric),
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Current Altitude
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = null,
                                tint = Color(0xFF90E0EF),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ALTITUDE", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = UnitFormatter.formatElevationAltitude(recordingState.currentAltitude, userProfile.useMetric),
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Waypoints Dropped
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddLocation,
                                contentDescription = null,
                                tint = Terracotta,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WAYPOINTS", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${recordingState.waypoints.size}",
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // GPS Sensor Fix
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (recordingState.points.isNotEmpty()) SageGreen else AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GPS FIX", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = if (recordingState.points.isNotEmpty()) "${recordingState.points.size} pts" else "Acquiring",
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Controls Row
                if (!recordingState.isRecording) {
                    Button(
                        onClick = { requestAllTrekPermissionsAndStart() },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("recording_start_button")
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = NightBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START RECORDING TREK",
                            color = NightBlack,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                } else {
                    // Active recording controls: Pause/Resume + Finish + Add Waypoint
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pause / Resume Button
                        Button(
                            onClick = {
                                if (recordingState.isPaused) {
                                    viewModel.resumeTrek(context)
                                } else {
                                    viewModel.pauseTrek(context)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (recordingState.isPaused) AmberGold else NightBlack
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("recording_pause_resume_button")
                        ) {
                            Icon(
                                imageVector = if (recordingState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (recordingState.isPaused) "Resume" else "Pause",
                                tint = if (recordingState.isPaused) NightBlack else AmberGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (recordingState.isPaused) "RESUME" else "PAUSE",
                                color = if (recordingState.isPaused) NightBlack else AmberGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // FINISH Button (with confirmation)
                        Button(
                            onClick = { showFinishDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("recording_finish_button")
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Finish Trek", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FINISH", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog to Finish Trek
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Complete Expedition?", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Are you sure you want to finish this trek? Your route, elevation stats, distance, and waypoints will be saved.",
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        viewModel.finishTrek(context)
                        onFinishTrek()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                ) {
                    Text("Save & Finish", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Keep Trekking", color = AmberGold)
                }
            },
            containerColor = NightCard
        )
    }

    // Dialog for adding a custom waypoint at user's current location
    if (showAddWaypointDialog) {
        var wpTitle by remember { mutableStateOf("") }
        var wpNote by remember { mutableStateOf("") }
        var wpType by remember { mutableStateOf(WaypointType.VIEWPOINT) }

        AlertDialog(
            onDismissRequest = { showAddWaypointDialog = false },
            title = { Text("Mark Trail Waypoint", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Pin an observation, water source, campsite or hazard at your exact GPS coordinates.",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = wpTitle,
                        onValueChange = { wpTitle = it },
                        label = { Text("Waypoint Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = TextSecondaryDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = wpNote,
                        onValueChange = { wpNote = it },
                        label = { Text("Field Notes (Optional)") },
                        singleLine = false,
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = TextSecondaryDark,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Waypoint Type:", color = TextPrimaryDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(WaypointType.values().toList()) { type ->
                            FilterChip(
                                selected = wpType == type,
                                onClick = { wpType = type },
                                label = { Text(type.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreen,
                                    selectedLabelColor = NightBlack,
                                    containerColor = NightBlack,
                                    labelColor = TextSecondaryDark
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentLoc = recordingState.lastKnownLocation
                        val lat = currentLoc?.latitude ?: 0.0
                        val lng = currentLoc?.longitude ?: 0.0
                        val alt = currentLoc?.altitude ?: recordingState.currentAltitude

                        val newWaypoint = TrekWaypoint(
                            title = wpTitle.ifEmpty { "Waypoint #${recordingState.waypoints.size + 1}" },
                            type = wpType,
                            latitude = lat,
                            longitude = lng,
                            altitude = alt,
                            note = wpNote
                        )
                        viewModel.addWaypointToActiveTrek(context, newWaypoint)
                        showAddWaypointDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                ) {
                    Text("Drop Waypoint", color = NightBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWaypointDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = NightCard
        )
    }

    // Photo Capture / Upload Dialog during live trek
    if (showPhotoCaptureDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoCaptureDialog = false },
            title = {
                Text(
                    text = "Snap Trail Photo",
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Take a photo right now with your camera or select from your gallery. It will be geo-tagged and pinned to your live trek route.",
                        color = TextSecondaryDark,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            showPhotoCaptureDialog = false
                            triggerCameraCapture()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("live_take_camera_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = NightBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take Photo with Camera",
                            color = NightBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            showPhotoCaptureDialog = false
                            livePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NightBlack,
                            contentColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("live_upload_gallery_photo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upload from Gallery",
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoCaptureDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = NightCard,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

