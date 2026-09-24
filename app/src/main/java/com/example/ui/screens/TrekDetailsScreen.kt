package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Trek
import com.example.ui.components.ArolockMapWidget
import com.example.ui.components.ElevationChart
import com.example.ui.components.MediaSection
import com.example.ui.components.TrekStatCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.util.GpsPointJsonHelper
import com.example.util.GpxExporter
import com.example.util.UnitFormatter
import com.example.viewmodel.TrekViewModel

@Composable
fun TrekDetailsScreen(
    trekId: Long,
    viewModel: TrekViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTreks by viewModel.allTreks.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val trek = allTreks.firstOrNull { it.id == trekId }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (trek == null) {
        Box(
            modifier = modifier.fillMaxSize().background(NightBlack),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Trek not found", color = TextSecondaryDark)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onNavigateBack) {
                    Text("Back")
                }
            }
        }
        return
    }

    val points = remember(trek) { GpsPointJsonHelper.jsonToPoints(trek.pointsJson) }
    val waypoints = remember(trek) { GpsPointJsonHelper.jsonToWaypoints(trek.waypointsJson) }
    val mediaPaths = remember(trek) { GpsPointJsonHelper.jsonToStringList(trek.mediaPathsJson) }

    val difficultyColor = when (trek.difficulty.lowercase()) {
        "easy" -> SageGreen
        "moderate" -> AmberGold
        "hard" -> Terracotta
        "extreme" -> DangerRed
        else -> AmberGold
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NightBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Bar Actions: Back, Share GPX, Edit, Delete
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NightCard)
                        .testTag("trek_details_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // GPX Export / Share
                    IconButton(
                        onClick = { GpxExporter.shareGpxFile(context, trek) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NightCard)
                            .testTag("export_gpx_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share GPX Route",
                            tint = SageGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Edit
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NightCard)
                            .testTag("edit_trek_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Trek",
                            tint = AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NightCard)
                            .testTag("delete_trek_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Trek",
                            tint = DangerRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Title and Difficulty Badge
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = difficultyColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = trek.difficulty.uppercase(),
                            color = difficultyColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = UnitFormatter.formatDateTime(trek.startTimeMillis),
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = trek.name,
                    color = TextPrimaryDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                if (trek.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trek.description,
                        color = TextSecondaryDark,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Full Interactive Route Map
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                ArolockMapWidget(
                    points = points,
                    currentLocation = null,
                    waypoints = waypoints,
                    isInteractive = true,
                    showControls = true
                )
            }
        }

        // Elevation Profile Chart
        item {
            ElevationChart(
                points = points,
                useMetric = userProfile.useMetric
            )
        }

        // Recorded Waypoints Section (if any were marked)
        if (waypoints.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "RECORDED WAYPOINTS (${waypoints.size})",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    waypoints.forEach { wp ->
                        Surface(
                            color = NightCard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(wp.type.iconSymbol, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = wp.title,
                                        color = TextPrimaryDark,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${wp.type.label} • ${wp.altitude.toInt()}m${if (wp.note.isNotEmpty()) " • " + wp.note else ""}",
                                        color = TextSecondaryDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Detailed Performance Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "EXPEDITION METRICS",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrekStatCard(
                        title = "Distance",
                        value = UnitFormatter.formatDistance(trek.distanceMeters, userProfile.useMetric),
                        icon = Icons.Default.Timeline,
                        iconTint = SageGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TrekStatCard(
                        title = "Duration",
                        value = UnitFormatter.formatDurationHuman(trek.durationSeconds),
                        icon = Icons.Default.Schedule,
                        iconTint = AmberGold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrekStatCard(
                        title = "Elev. Gain",
                        value = UnitFormatter.formatElevation(trek.elevationGainMeters, userProfile.useMetric),
                        icon = Icons.Default.Terrain,
                        iconTint = Terracotta,
                        modifier = Modifier.weight(1f)
                    )
                    TrekStatCard(
                        title = "Elev. Loss",
                        value = UnitFormatter.formatElevation(trek.elevationLossMeters, userProfile.useMetric),
                        icon = Icons.Default.Terrain,
                        iconTint = SageGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrekStatCard(
                        title = "Avg Speed",
                        value = UnitFormatter.formatSpeed(trek.avgSpeedMps.toFloat(), userProfile.useMetric),
                        icon = Icons.Default.DirectionsWalk,
                        iconTint = SageGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TrekStatCard(
                        title = "Max Speed",
                        value = UnitFormatter.formatSpeed(trek.maxSpeedMps.toFloat(), userProfile.useMetric),
                        icon = Icons.Default.Speed,
                        iconTint = AmberGold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrekStatCard(
                        title = "Total Steps",
                        value = UnitFormatter.formatSteps(trek.steps),
                        unit = "steps",
                        icon = Icons.Default.DirectionsWalk,
                        iconTint = AmberGold,
                        modifier = Modifier.weight(1f)
                    )
                    TrekStatCard(
                        title = "Estimated Burn",
                        value = trek.estimatedCalories.toString(),
                        unit = "kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        iconTint = DangerRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrekStatCard(
                        title = "Max Altitude",
                        value = UnitFormatter.formatElevationAltitude(trek.maxAltitude, userProfile.useMetric),
                        icon = Icons.Default.Terrain,
                        iconTint = SageGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TrekStatCard(
                        title = "Min Altitude",
                        value = UnitFormatter.formatElevationAltitude(trek.minAltitude, userProfile.useMetric),
                        icon = Icons.Default.Terrain,
                        iconTint = Terracotta,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Media Gallery
        item {
            MediaSection(
                mediaPaths = mediaPaths,
                onAddMedia = { newPath ->
                    val updated = mediaPaths.toMutableList().apply { add(newPath) }
                    viewModel.updateTrek(
                        trek.copy(mediaPathsJson = GpsPointJsonHelper.stringListToJson(updated))
                    )
                },
                onRemoveMedia = { removePath ->
                    val updated = mediaPaths.toMutableList().apply { remove(removePath) }
                    viewModel.updateTrek(
                        trek.copy(mediaPathsJson = GpsPointJsonHelper.stringListToJson(updated))
                    )
                },
                isEditable = true
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete This Trek?", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete '${trek.name}' and all associated route telemetry.", color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteTrek(trek.id)
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = NightCard
        )
    }

    // Edit Trek Dialog
    if (showEditDialog) {
        var editName by remember { mutableStateOf(trek.name) }
        var editDesc by remember { mutableStateOf(trek.description) }
        var editDiff by remember { mutableStateOf(trek.difficulty) }
        val diffList = listOf("Easy", "Moderate", "Hard", "Extreme")

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Trek", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Trek Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = NightCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = NightCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Difficulty", color = TextSecondaryDark, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        diffList.forEach { diff ->
                            FilterChip(
                                selected = editDiff.equals(diff, ignoreCase = true),
                                onClick = { editDiff = diff },
                                label = { Text(diff, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SageGreen,
                                    selectedLabelColor = NightBlack,
                                    containerColor = NightCard,
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
                        showEditDialog = false
                        viewModel.updateTrek(
                            trek.copy(
                                name = editName,
                                description = editDesc,
                                difficulty = editDiff
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen)
                ) {
                    Text("Save", color = NightBlack, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = NightCard
        )
    }
}
