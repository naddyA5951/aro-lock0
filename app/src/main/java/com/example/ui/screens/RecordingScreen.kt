package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    // Location Permission Check
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
            viewModel.startTrek(context, isSimulation = false)
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

                // Live status badge
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
                                recordingState.isSimulating -> Color(0xFF00B4D8)
                                else -> DangerRed
                            },
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                !recordingState.isRecording -> "READY TO TREK"
                                recordingState.isPaused -> "PAUSED"
                                recordingState.isSimulating -> "DEMO WALK SIMULATION"
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

                // Secondary Metrics Grid: Elevation Gain, Speed, Waypoints, Calories
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

                    // Current Speed
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

                    // Estimated Calories
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CALORIES", color = TextSecondaryDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${recordingState.estimatedCalories} kcal",
                            color = TextPrimaryDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Controls Row
                if (!recordingState.isRecording) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Start Real GPS Trek
                        Button(
                            onClick = {
                                if (hasLocationPermission) {
                                    viewModel.startTrek(context, isSimulation = false)
                                } else {
                                    val permissions = mutableListOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    permissionLauncher.launch(permissions.toTypedArray())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(54.dp)
                                .testTag("recording_start_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = NightBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RECORD GPS",
                                color = NightBlack,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }

                        // Demo Walk Simulation (works everywhere on every device, including browser emulator)
                        OutlinedButton(
                            onClick = {
                                viewModel.startTrek(context, isSimulation = true)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                                .testTag("start_demo_walk_button")
                        ) {
                            Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = AmberGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DEMO WALK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
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
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Finish",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FINISH", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }

    // Add Waypoint Dialog
    if (showAddWaypointDialog) {
        var wpTitle by remember { mutableStateOf("") }
        var wpNote by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(WaypointType.VIEWPOINT) }

        val types = listOf(
            WaypointType.VIEWPOINT,
            WaypointType.SUMMIT,
            WaypointType.WATER_SOURCE,
            WaypointType.CAMPSITE,
            WaypointType.REST_STOP,
            WaypointType.HAZARD
        )

        AlertDialog(
            onDismissRequest = { showAddWaypointDialog = false },
            title = {
                Text("Drop Waypoint on Trail", color = TextPrimaryDark, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Mark an important summit, spring, camp, or viewpoint along your route.",
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )

                    OutlinedTextField(
                        value = wpTitle,
                        onValueChange = { wpTitle = it },
                        placeholder = { Text("e.g. Glacier Viewpoint / Spring") },
                        label = { Text("Waypoint Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("waypoint_name_input")
                    )

                    Text("Waypoint Category:", color = TextSecondaryDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(types) { type ->
                            val isSel = type == selectedType
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedType = type },
                                label = { Text("${type.iconSymbol} ${type.label}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreen,
                                    selectedLabelColor = NightBlack,
                                    containerColor = NightBlack,
                                    labelColor = TextSecondaryDark
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = wpNote,
                        onValueChange = { wpNote = it },
                        placeholder = { Text("Optional note or safety warning") },
                        label = { Text("Notes") },
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentLoc = recordingState.lastKnownLocation
                        val lat = currentLoc?.latitude ?: 45.9237
                        val lng = currentLoc?.longitude ?: 6.8694
                        val alt = currentLoc?.altitude ?: recordingState.currentAltitude
                        val waypoint = TrekWaypoint(
                            title = wpTitle.ifEmpty { selectedType.label },
                            type = selectedType,
                            latitude = lat,
                            longitude = lng,
                            altitude = alt,
                            note = wpNote
                        )
                        viewModel.addWaypointToActiveTrek(context, waypoint)
                        showAddWaypointDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                    modifier = Modifier.testTag("save_waypoint_button")
                ) {
                    Text("Drop Pin", color = NightBlack, fontWeight = FontWeight.Bold)
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

    // Finish Trek Confirmation Dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = {
                Text(
                    text = "Finish This Trek?",
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You covered ${UnitFormatter.formatDistance(recordingState.currentDistanceMeters, userProfile.useMetric)} in ${UnitFormatter.formatDuration(recordingState.elapsedTimeSeconds)}. Ending now will save your route and statistics.",
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
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                    modifier = Modifier.testTag("confirm_finish_button")
                ) {
                    Text("Finish & Save", color = NightBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Keep Trekking", color = TextSecondaryDark)
                }
            },
            containerColor = NightCard
        )
    }
}
