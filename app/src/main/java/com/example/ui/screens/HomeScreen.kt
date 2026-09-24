package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Trek
import com.example.ui.components.ArolockMapWidget
import com.example.ui.components.TrekCard
import com.example.ui.components.TrekStatCard
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
import com.example.util.UnitFormatter
import com.example.viewmodel.TrekViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: TrekViewModel,
    onStartTrekClick: () -> Unit,
    onExploreTrailsClick: () -> Unit,
    onViewTrekDetails: (Long) -> Unit,
    onViewAllTreks: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()
    val recentTreks by viewModel.recentTreks.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val featuredTrail = remember { viewModel.catalogTrails.firstOrNull() }

    val greeting = rememberGreeting()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NightBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Row: User Greeting & Settings icon
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$greeting, ${userProfile.name}",
                            color = TextSecondaryDark,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Ready for your next adventure?",
                            color = TextPrimaryDark,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NightCard)
                            .testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Active Recording Banner (if trek in progress)
            item {
                AnimatedVisibility(visible = recordingState.isRecording) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PineDeep),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartTrekClick() }
                            .testTag("active_recording_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FiberManualRecord,
                                    contentDescription = null,
                                    tint = if (recordingState.isPaused) AmberGold else DangerRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (recordingState.isPaused) "TREK PAUSED" else "LIVE GPS & STEP TRACKING",
                                        color = if (recordingState.isPaused) AmberGold else SageGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${UnitFormatter.formatDistance(recordingState.currentDistanceMeters, userProfile.useMetric)} • ${UnitFormatter.formatSteps(recordingState.stepCount)} steps • ${UnitFormatter.formatDuration(recordingState.elapsedTimeSeconds)}",
                                        color = TextPrimaryDark,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = onStartTrekClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Resume", color = NightBlack, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Hero "START TREK" Action Banner (when not recording)
            if (!recordingState.isRecording) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(ForestGreen, PineDeep)
                                )
                            )
                            .clickable { onStartTrekClick() }
                            .padding(20.dp)
                            .testTag("start_trek_hero_card")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = AmberGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "UNIVERSAL TRAIL TRACKER",
                                        color = AmberGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "START TREK",
                                    color = TextPrimaryDark,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp
                                )

                                Text(
                                    text = "Record live route, elevation profile, distance & waypoints.",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(AmberGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start Trek",
                                    tint = NightBlack,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Featured Mountain Trail of the Day Card
            featuredTrail?.let { trail ->
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = NightCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExploreTrailsClick() }
                            .testTag("featured_trail_card")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = SageGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "FEATURED MOUNTAIN TRAIL",
                                        color = SageGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                TextButton(onClick = onExploreTrailsClick) {
                                    Text("Explore All", color = AmberGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                                }
                            }

                            Text(
                                text = trail.name,
                                color = TextPrimaryDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${trail.region} • ${UnitFormatter.formatDistance(trail.distanceMeters, userProfile.useMetric)} • ${UnitFormatter.formatElevation(trail.elevationGainMeters, userProfile.useMetric)} gain",
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Mini Trail Route Map
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            ) {
                                ArolockMapWidget(
                                    points = emptyList(),
                                    currentLocation = null,
                                    referencePoints = trail.points,
                                    waypoints = trail.waypoints,
                                    isInteractive = false,
                                    showControls = false
                                )
                            }
                        }
                    }
                }
            }

            // Lifetime Statistics Strip
            item {
                Column {
                    Text(
                        text = "LIFETIME EXPEDITIONS",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrekStatCard(
                            title = "Distance",
                            value = UnitFormatter.formatDistance(statistics.totalDistanceMeters, userProfile.useMetric),
                            icon = Icons.Default.Timeline,
                            iconTint = SageGreen,
                            modifier = Modifier.weight(1f)
                        )
                        TrekStatCard(
                            title = "Steps",
                            value = UnitFormatter.formatSteps(statistics.totalSteps),
                            unit = "steps",
                            icon = Icons.Default.DirectionsWalk,
                            iconTint = AmberGold,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TrekStatCard(
                            title = "Elevation",
                            value = UnitFormatter.formatElevation(statistics.totalElevationGainMeters, userProfile.useMetric),
                            icon = Icons.Default.Terrain,
                            iconTint = Terracotta,
                            modifier = Modifier.weight(1f)
                        )
                        TrekStatCard(
                            title = "Treks",
                            value = statistics.totalTreks.toString(),
                            unit = "hikes",
                            icon = Icons.Default.Explore,
                            iconTint = Color(0xFF48CAE4),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Recent Treks Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT RECORDED TREKS",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (recentTreks.isNotEmpty()) {
                        TextButton(onClick = onViewAllTreks) {
                            Text(
                                text = "See All (${statistics.totalTreks})",
                                color = SageGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (recentTreks.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NightCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terrain,
                                contentDescription = null,
                                tint = SageGreen.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Treks Recorded Yet",
                                color = TextPrimaryDark,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hit 'START TREK' or 'Explore Trails' to track your first route!",
                                color = TextSecondaryDark,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(recentTreks) { trek ->
                    TrekCard(
                        trek = trek,
                        useMetric = userProfile.useMetric,
                        onClick = { onViewTrekDetails(trek.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Good night"
    }
}
