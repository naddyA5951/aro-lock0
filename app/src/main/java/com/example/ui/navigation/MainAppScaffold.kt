package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RecordingScreen
import com.example.ui.screens.SaveTrekScreen
import com.example.ui.screens.SavedTreksScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TrailsExplorerScreen
import com.example.ui.screens.TrekDetailsScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.TrekViewModel

enum class Screen {
    SPLASH,
    HOME,
    TRAILS_EXPLORER,
    RECORDING,
    SAVE_TREK,
    SAVED_TREKS,
    TREK_DETAILS,
    PROFILE,
    SETTINGS
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: TrekViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    var selectedTrekId by remember { mutableLongStateOf(0L) }

    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()

    val showBottomBar = currentScreen in listOf(
        Screen.HOME,
        Screen.TRAILS_EXPLORER,
        Screen.SAVED_TREKS,
        Screen.PROFILE
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = NightCard,
                    contentColor = TextPrimaryDark,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    // Home Tab
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { currentScreen = Screen.HOME },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NightBlack,
                            selectedTextColor = SageGreen,
                            indicatorColor = SageGreen,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_home")
                    )

                    // Trails & Maps Tab
                    NavigationBarItem(
                        selected = currentScreen == Screen.TRAILS_EXPLORER,
                        onClick = { currentScreen = Screen.TRAILS_EXPLORER },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Trails"
                            )
                        },
                        label = { Text("Trails", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NightBlack,
                            selectedTextColor = SageGreen,
                            indicatorColor = SageGreen,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_trails")
                    )

                    // Record Tab (Live trek action)
                    NavigationBarItem(
                        selected = currentScreen == Screen.RECORDING,
                        onClick = { currentScreen = Screen.RECORDING },
                        icon = {
                            if (recordingState.isRecording) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = if (recordingState.isPaused) AmberGold else DangerRed
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsWalk,
                                        contentDescription = "Active Trek"
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DirectionsWalk,
                                    contentDescription = "Record"
                                )
                            }
                        },
                        label = {
                            Text(
                                if (recordingState.isRecording) "Live Trek" else "Record",
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NightBlack,
                            selectedTextColor = AmberGold,
                            indicatorColor = AmberGold,
                            unselectedIconColor = if (recordingState.isRecording) SageGreen else TextSecondaryDark,
                            unselectedTextColor = if (recordingState.isRecording) SageGreen else TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_record")
                    )

                    // Treks Logbook Tab
                    NavigationBarItem(
                        selected = currentScreen == Screen.SAVED_TREKS,
                        onClick = { currentScreen = Screen.SAVED_TREKS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Terrain,
                                contentDescription = "Logbook"
                            )
                        },
                        label = { Text("Logbook", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NightBlack,
                            selectedTextColor = SageGreen,
                            indicatorColor = SageGreen,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_treks")
                    )

                    // Profile Tab
                    NavigationBarItem(
                        selected = currentScreen == Screen.PROFILE,
                        onClick = { currentScreen = Screen.PROFILE },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NightBlack,
                            selectedTextColor = SageGreen,
                            indicatorColor = SageGreen,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
            }
        },
        containerColor = NightBlack,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.SPLASH -> {
                    SplashScreen(
                        onTimeout = { currentScreen = Screen.HOME }
                    )
                }

                Screen.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onStartTrekClick = { currentScreen = Screen.RECORDING },
                        onExploreTrailsClick = { currentScreen = Screen.TRAILS_EXPLORER },
                        onViewTrekDetails = { id ->
                            selectedTrekId = id
                            currentScreen = Screen.TREK_DETAILS
                        },
                        onViewAllTreks = { currentScreen = Screen.SAVED_TREKS },
                        onSettingsClick = { currentScreen = Screen.SETTINGS }
                    )
                }

                Screen.TRAILS_EXPLORER -> {
                    TrailsExplorerScreen(
                        viewModel = viewModel,
                        onStartTrailTrek = { trail, isSimulation ->
                            viewModel.selectCatalogTrail(trail)
                            viewModel.startTrek(context, isSimulation = isSimulation, trail = trail)
                            currentScreen = Screen.RECORDING
                        }
                    )
                }

                Screen.RECORDING -> {
                    RecordingScreen(
                        viewModel = viewModel,
                        onFinishTrek = { currentScreen = Screen.SAVE_TREK },
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                }

                Screen.SAVE_TREK -> {
                    SaveTrekScreen(
                        viewModel = viewModel,
                        onSavedSuccessfully = { newId ->
                            selectedTrekId = newId
                            currentScreen = Screen.TREK_DETAILS
                        },
                        onDiscarded = { currentScreen = Screen.HOME }
                    )
                }

                Screen.SAVED_TREKS -> {
                    SavedTreksScreen(
                        viewModel = viewModel,
                        onTrekClick = { id ->
                            selectedTrekId = id
                            currentScreen = Screen.TREK_DETAILS
                        }
                    )
                }

                Screen.TREK_DETAILS -> {
                    TrekDetailsScreen(
                        trekId = selectedTrekId,
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.SAVED_TREKS }
                    )
                }

                Screen.PROFILE -> {
                    ProfileScreen(
                        viewModel = viewModel
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME }
                    )
                }
            }
        }
    }
}
