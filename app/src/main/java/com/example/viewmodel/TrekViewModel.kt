package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TrailsCatalog
import com.example.data.TrekRepository
import com.example.model.GpsPoint
import com.example.model.RecordingState
import com.example.model.Trail
import com.example.model.Trek
import com.example.model.TrekStatistics
import com.example.model.TrekWaypoint
import com.example.model.UserProfile
import com.example.service.TrekTrackingService
import com.example.util.GpsPointJsonHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrekViewModel(
    private val repository: TrekRepository
) : ViewModel() {

    // Live service recording state
    val recordingState: StateFlow<RecordingState> = TrekTrackingService.recordingState

    // Saved treks and stats
    val allTreks: StateFlow<List<Trek>> = repository.allTreks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTreks: StateFlow<List<Trek>> = repository.recentTreks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statistics: StateFlow<TrekStatistics> = repository.statistics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrekStatistics())

    val userProfile: StateFlow<UserProfile> = repository.userProfile

    // Available famous trails catalog
    val catalogTrails: List<Trail> = TrailsCatalog.allTrails

    private val _selectedCatalogTrail = MutableStateFlow<Trail?>(null)
    val selectedCatalogTrail: StateFlow<Trail?> = _selectedCatalogTrail.asStateFlow()

    fun selectCatalogTrail(trail: Trail?) {
        _selectedCatalogTrail.value = trail
    }

    // Pending trek to be saved after finish
    private val _pendingCompletedTrek = MutableStateFlow<Trek?>(null)
    val pendingCompletedTrek: StateFlow<Trek?> = _pendingCompletedTrek.asStateFlow()

    fun startTrek(
        context: Context,
        trail: Trail? = null
    ) {
        val targetTrail = trail ?: _selectedCatalogTrail.value
        TrekTrackingService.startService(
            context = context,
            trailId = targetTrail?.id,
            trailName = targetTrail?.name,
            trailPoints = targetTrail?.points ?: emptyList()
        )
    }

    fun pauseTrek(context: Context) {
        TrekTrackingService.pauseService(context)
    }

    fun resumeTrek(context: Context) {
        TrekTrackingService.resumeService(context)
    }

    fun addWaypointToActiveTrek(context: Context, waypoint: TrekWaypoint) {
        TrekTrackingService.addWaypoint(context, waypoint)
    }

    fun finishTrek(context: Context) {
        val state = recordingState.value
        val defaultName = if (!state.activeTrailName.isNullOrEmpty()) {
            "Trek on ${state.activeTrailName}"
        } else {
            "Trek on ${android.text.format.DateFormat.format("MMM dd, yyyy", state.startTimeMillis)}"
        }

        val trekToSave = Trek(
            name = defaultName,
            description = "",
            startTimeMillis = state.startTimeMillis,
            endTimeMillis = System.currentTimeMillis(),
            durationSeconds = state.elapsedTimeSeconds,
            distanceMeters = state.currentDistanceMeters,
            elevationGainMeters = state.elevationGainMeters,
            elevationLossMeters = state.elevationLossMeters,
            minAltitude = state.minAltitude,
            maxAltitude = state.maxAltitude,
            currentAltitude = state.currentAltitude,
            avgSpeedMps = state.avgSpeedMps.toDouble(),
            maxSpeedMps = state.maxSpeedMps.toDouble(),
            steps = state.stepCount,
            estimatedCalories = state.estimatedCalories,
            difficulty = "Moderate",
            pointsJson = GpsPointJsonHelper.pointsToJson(state.points),
            waypointsJson = GpsPointJsonHelper.waypointsToJson(state.waypoints),
            mediaPathsJson = GpsPointJsonHelper.stringListToJson(state.attachedMediaPaths),
            isFinished = true
        )

        TrekTrackingService.stopService(context)
        _pendingCompletedTrek.value = trekToSave
    }

    fun saveCompletedTrek(
        name: String,
        description: String,
        difficulty: String,
        onSaved: (Long) -> Unit
    ) {
        val pending = _pendingCompletedTrek.value ?: return
        viewModelScope.launch {
            val finalTrek = pending.copy(
                name = name.ifEmpty { "Mountain Adventure" },
                description = description,
                difficulty = difficulty
            )
            val newId = repository.saveTrek(finalTrek)
            _pendingCompletedTrek.value = null
            TrekTrackingService.resetState()
            onSaved(newId)
        }
    }

    fun discardPendingTrek() {
        _pendingCompletedTrek.value = null
        TrekTrackingService.resetState()
    }

    fun addMediaToActiveTrek(context: Context, path: String) {
        TrekTrackingService.attachMedia(context, path)
    }

    fun addMediaToPendingTrek(path: String) {
        val pending = _pendingCompletedTrek.value ?: return
        val currentMedia = GpsPointJsonHelper.jsonToStringList(pending.mediaPathsJson).toMutableList()
        if (!currentMedia.contains(path)) {
            currentMedia.add(path)
            _pendingCompletedTrek.value = pending.copy(
                mediaPathsJson = GpsPointJsonHelper.stringListToJson(currentMedia)
            )
        }
    }

    fun deleteTrek(id: Long) {
        viewModelScope.launch {
            repository.deleteTrek(id)
        }
    }

    fun updateTrek(trek: Trek) {
        viewModelScope.launch {
            repository.updateTrek(trek)
        }
    }

    fun addMediaToSavedTrek(trekId: Long, path: String) {
        viewModelScope.launch {
            val trek = repository.getTrekById(trekId) ?: return@launch
            val currentMedia = GpsPointJsonHelper.jsonToStringList(trek.mediaPathsJson).toMutableList()
            if (!currentMedia.contains(path)) {
                currentMedia.add(path)
                repository.updateTrek(trek.copy(mediaPathsJson = GpsPointJsonHelper.stringListToJson(currentMedia)))
            }
        }
    }

    fun loginWithGoogle(displayName: String, email: String, photoUri: String? = null) {
        repository.loginWithGoogle(displayName, email, photoUri)
    }

    fun loginWithEmail(displayName: String, email: String) {
        repository.loginWithEmail(displayName, email)
    }

    fun registerAccount(name: String, email: String, password: String): Boolean {
        return repository.registerAccount(name, email, password)
    }

    fun loginWithPassword(email: String, password: String): Pair<Boolean, String> {
        return repository.loginWithPassword(email, password)
    }

    fun verifyEmailAndLogin(displayName: String, email: String, authProvider: String = "Email") {
        repository.verifyEmailAndLogin(displayName, email, authProvider)
    }

    fun setPreferredMapStyle(styleKey: String) {
        repository.setPreferredMapStyle(styleKey)
    }

    fun logout() {
        repository.logout()
    }

    fun updateProfile(profile: UserProfile) {
        repository.updateProfile(profile)
    }

    fun setUnitSystem(useMetric: Boolean) {
        repository.setUnitSystem(useMetric)
    }

    fun setDarkMode(isDark: Boolean) {
        repository.setDarkMode(isDark)
    }

    class Factory(private val repository: TrekRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrekViewModel(repository) as T
        }
    }
}
