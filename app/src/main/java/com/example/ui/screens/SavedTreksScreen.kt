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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GpsPoint
import com.example.model.TrekWaypoint
import com.example.ui.components.ArolockMapWidget
import com.example.ui.components.TrekCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.util.GpsPointJsonHelper
import com.example.viewmodel.TrekViewModel

@Composable
fun SavedTreksScreen(
    viewModel: TrekViewModel,
    onTrekClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allTreks by viewModel.allTreks.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("All") }
    var isCombinedMapView by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Easy", "Moderate", "Hard", "Extreme")

    val filteredTreks = remember(allTreks, searchQuery, selectedDifficulty) {
        allTreks.filter { trek ->
            val matchesQuery = trek.name.contains(searchQuery, ignoreCase = true) ||
                    trek.description.contains(searchQuery, ignoreCase = true)
            val matchesDifficulty = selectedDifficulty == "All" ||
                    trek.difficulty.equals(selectedDifficulty, ignoreCase = true)
            matchesQuery && matchesDifficulty
        }
    }

    // Combine all points and waypoints across filtered treks for world/region map overview
    val combinedPoints = remember(filteredTreks) {
        val points = mutableListOf<GpsPoint>()
        filteredTreks.forEach { t ->
            points.addAll(GpsPointJsonHelper.jsonToPoints(t.pointsJson))
        }
        points
    }

    val combinedWaypoints = remember(filteredTreks) {
        val wps = mutableListOf<TrekWaypoint>()
        filteredTreks.forEach { t ->
            wps.addAll(GpsPointJsonHelper.jsonToWaypoints(t.waypointsJson))
        }
        wps
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NightBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                        text = "TRAIL LOGBOOK",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Recorded Treks (${allTreks.size})",
                        color = TextPrimaryDark,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Map vs List Toggle
                IconButton(
                    onClick = { isCombinedMapView = !isCombinedMapView },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isCombinedMapView) SageGreen else NightCard,
                        contentColor = if (isCombinedMapView) NightBlack else TextPrimaryDark
                    ),
                    modifier = Modifier.size(44.dp).testTag("toggle_logbook_map_view")
                ) {
                    Icon(
                        imageVector = if (isCombinedMapView) Icons.Default.ViewList else Icons.Default.Map,
                        contentDescription = "Toggle Map Overview"
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by trail name...", color = TextSecondaryDark) },
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
                                contentDescription = "Clear search",
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
                    .testTag("search_treks_input")
            )
        }

        // Filter Chips Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = selectedDifficulty == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDifficulty = filter },
                        label = { Text(filter, fontSize = 12.sp) },
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

        // Combined Trail Map Overview (if toggled)
        if (isCombinedMapView && combinedPoints.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "ALL RECORDED EXPEDITIONS ON MAP",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        ArolockMapWidget(
                            points = combinedPoints,
                            currentLocation = null,
                            waypoints = combinedWaypoints,
                            isInteractive = true,
                            showControls = true
                        )
                    }
                }
            }
        }

        if (filteredTreks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NightCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = null,
                            tint = SageGreen.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty() || selectedDifficulty != "All") "No Matching Treks" else "No Recorded Treks Yet",
                            color = TextPrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search query or clear filters." else "Hit 'Record' to track your first mountain trail with live GPS!",
                            color = TextSecondaryDark,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredTreks) { trek ->
                TrekCard(
                    trek = trek,
                    useMetric = userProfile.useMetric,
                    onClick = { onTrekClick(trek.id) }
                )
            }
        }
    }
}
