package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.model.Trail
import com.example.ui.components.ArolockMapWidget
import com.example.ui.components.ElevationChart
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

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun TrailsExplorerScreen(
    viewModel: TrekViewModel,
    onStartTrailTrek: (Trail) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allTrails = viewModel.catalogTrails

    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var inspectingTrail by remember { mutableStateOf<Trail?>(null) }

    val difficulties = listOf("All", "Easy", "Moderate", "Hard", "Expert")

    val filteredTrails = remember(allTrails, searchQuery, selectedDifficulty) {
        allTrails.filter { trail ->
            val matchesQuery = trail.name.contains(searchQuery, ignoreCase = true) ||
                    trail.region.contains(searchQuery, ignoreCase = true) ||
                    trail.country.contains(searchQuery, ignoreCase = true)
            val matchesDiff = selectedDifficulty == "All" ||
                    trail.difficulty.equals(selectedDifficulty, ignoreCase = true)
            matchesQuery && matchesDiff
        }
    }

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
                Text(
                    text = "WORLD TRAILS & MAPS",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Trail Explorer",
                    color = TextPrimaryDark,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Discover famous mountain routes, preview elevation profiles, or record your trek along them.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search trails, peaks, or countries...", color = TextSecondaryDark) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = SageGreen
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextSecondaryDark
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SageGreen,
                    unfocusedBorderColor = NightCard,
                    unfocusedContainerColor = NightCard,
                    focusedContainerColor = NightCard,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_trails_input")
            )
        }

        // Difficulty Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(difficulties) { diff ->
                    val isSelected = diff == selectedDifficulty
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDifficulty = diff },
                        label = { Text(diff, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreen,
                            selectedLabelColor = NightBlack,
                            containerColor = NightCard,
                            labelColor = TextSecondaryDark
                        ),
                        border = null,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Trails Cards
        items(filteredTrails) { trail ->
            TrailCatalogCard(
                trail = trail,
                useMetric = userProfile.useMetric,
                onInspect = { inspectingTrail = trail }
            )
        }
    }

    // Full Trail Detail & Map Preview Bottom Sheet
    inspectingTrail?.let { trail ->
        ModalBottomSheet(
            onDismissRequest = { inspectingTrail = null },
            containerColor = NightCard,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (trail.difficulty.lowercase()) {
                                "easy" -> SageGreen.copy(alpha = 0.2f)
                                "moderate" -> AmberGold.copy(alpha = 0.2f)
                                "hard" -> Terracotta.copy(alpha = 0.2f)
                                else -> DangerRed.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = trail.difficulty.uppercase(),
                                color = when (trail.difficulty.lowercase()) {
                                    "easy" -> SageGreen
                                    "moderate" -> AmberGold
                                    "hard" -> Terracotta
                                    else -> DangerRed
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${trail.region}, ${trail.country}",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = trail.name,
                        color = TextPrimaryDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = trail.description,
                        color = TextSecondaryDark,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                // Interactive Route Map
                item {
                    Text(
                        text = "TRAIL ROUTE & WAYPOINTS",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        ArolockMapWidget(
                            points = emptyList(),
                            currentLocation = null,
                            referencePoints = trail.points,
                            waypoints = trail.waypoints,
                            isInteractive = true,
                            showControls = true
                        )
                    }
                }

                // Elevation Profile
                item {
                    Text(
                        text = "ELEVATION PROFILE",
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ElevationChart(
                        points = trail.points,
                        useMetric = userProfile.useMetric
                    )
                }

                // Trail Highlights
                if (trail.highlights.isNotEmpty()) {
                    item {
                        Text(
                            text = "HIGHLIGHTS",
                            color = TextSecondaryDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        trail.highlights.forEach { h ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Text("•", color = SageGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(h, color = TextPrimaryDark, fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Start Buttons Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                inspectingTrail = null
                                onStartTrailTrek(trail)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_real_trail_button")
                        ) {
                            Icon(imageVector = Icons.Default.DirectionsWalk, contentDescription = null, tint = NightBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START GUIDED TREK", color = NightBlack, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrailCatalogCard(
    trail: Trail,
    useMetric: Boolean,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val diffColor = when (trail.difficulty.lowercase()) {
        "easy" -> SageGreen
        "moderate" -> AmberGold
        "hard" -> Terracotta
        else -> DangerRed
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NightCard),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onInspect() }
            .testTag("trail_card_${trail.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Region & Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trail.region} • ${trail.country}",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    color = diffColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = trail.difficulty.uppercase(),
                        color = diffColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = trail.name,
                color = TextPrimaryDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Trail Map Preview Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            // Key Metrics Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("DISTANCE", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = UnitFormatter.formatDistance(trail.distanceMeters, useMetric),
                        color = TextPrimaryDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("ELEV. GAIN", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = UnitFormatter.formatElevation(trail.elevationGainMeters, useMetric),
                        color = SageGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("EST. TIME", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "${trail.estimatedDurationMinutes / 60}h ${trail.estimatedDurationMinutes % 60}m",
                        color = AmberGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ROUTE", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = trail.trailType,
                        color = TextPrimaryDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
