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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ArolockMapWidget
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
import com.example.util.UnitFormatter
import com.example.viewmodel.TrekViewModel

@Composable
fun SaveTrekScreen(
    viewModel: TrekViewModel,
    onSavedSuccessfully: (Long) -> Unit,
    onDiscarded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingTrek by viewModel.pendingCompletedTrek.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    if (pendingTrek == null) {
        Box(
            modifier = modifier.fillMaxSize().background(NightBlack),
            contentAlignment = Alignment.Center
        ) {
            Text("No completed trek found", color = TextSecondaryDark)
        }
        return
    }

    val trek = pendingTrek!!
    val points = remember(trek) { GpsPointJsonHelper.jsonToPoints(trek.pointsJson) }
    val mediaPaths = remember(trek) { GpsPointJsonHelper.jsonToStringList(trek.mediaPathsJson) }

    var trekName by remember { mutableStateOf(trek.name) }
    var description by remember { mutableStateOf(trek.description) }
    var selectedDifficulty by remember { mutableStateOf("Moderate") }
    var showDiscardConfirm by remember { mutableStateOf(false) }

    val difficulties = listOf("Easy", "Moderate", "Hard", "Extreme")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NightBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Surface(
                    color = SageGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "EXPEDITION FINISHED",
                        color = SageGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Trek Completed!",
                    color = TextPrimaryDark,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Review your journey stats and save to your logbook.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp
                )
            }
        }

        // Full route map preview
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                ArolockMapWidget(
                    points = points,
                    currentLocation = null,
                    isInteractive = true,
                    showControls = true
                )
            }
        }

        // Summary Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "PERFORMANCE STATS",
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
                        title = "Est. Calories",
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
            }
        }

        // Details Form: Name, Description, Difficulty
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "TRAIL DETAILS",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = trekName,
                    onValueChange = { trekName = it },
                    label = { Text("Trek Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = NightCard,
                        focusedLabelColor = SageGreen,
                        unfocusedContainerColor = NightCard,
                        focusedContainerColor = NightCard,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_trek_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes (optional)") },
                    placeholder = { Text("Weather condition, terrain observations...") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = NightCard,
                        focusedLabelColor = SageGreen,
                        unfocusedContainerColor = NightCard,
                        focusedContainerColor = NightCard,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_trek_description_input")
                )

                Text(
                    text = "DIFFICULTY RATING",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    difficulties.forEach { diff ->
                        val selected = selectedDifficulty.equals(diff, ignoreCase = true)
                        FilterChip(
                            selected = selected,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff, fontSize = 12.sp) },
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
        }

        // Media Section (Attach Photos/Videos)
        item {
            MediaSection(
                mediaPaths = mediaPaths,
                onAddMedia = { path ->
                    viewModel.addMediaToPendingTrek(path)
                },
                isEditable = true
            )
        }

        // Action Buttons: Save & Discard
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.saveCompletedTrek(
                            name = trekName,
                            description = description,
                            difficulty = selectedDifficulty,
                            onSaved = onSavedSuccessfully
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_trek_submit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = NightBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SAVE TREK TO LOGBOOK",
                        color = NightBlack,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                OutlinedButton(
                    onClick = { showDiscardConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("discard_trek_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Discard Trek", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Discard Confirmation Dialog
    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Discard Trek?", color = TextPrimaryDark, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to discard this trek? The recorded GPS route and metrics will be deleted permanently.", color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirm = false
                        viewModel.discardPendingTrek()
                        onDiscarded()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("Discard", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = NightCard
        )
    }
}
