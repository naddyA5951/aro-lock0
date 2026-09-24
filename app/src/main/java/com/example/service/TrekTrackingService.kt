package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.GpsPoint
import com.example.model.RecordingState
import com.example.model.TrekWaypoint
import com.example.util.CalorieCalculator
import com.example.util.GpsFilter
import com.example.util.GpsPointJsonHelper
import com.example.util.UnitFormatter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class TrekTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var simulationJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (_recordingState.value.isSimulating) return
                for (location in result.locations) {
                    processNewLocation(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val isSim = intent.getBooleanExtra(EXTRA_SIMULATION, false)
                val trailId = intent.getStringExtra(EXTRA_TRAIL_ID)
                val trailName = intent.getStringExtra(EXTRA_TRAIL_NAME)
                val trailPointsJson = intent.getStringExtra(EXTRA_TRAIL_POINTS_JSON)
                val trailPoints = GpsPointJsonHelper.jsonToPoints(trailPointsJson)
                startTracking(isSim, trailId, trailName, trailPoints)
            }
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
            ACTION_STOP -> stopTracking()
            ACTION_ADD_WAYPOINT -> {
                val wpJson = intent.getStringExtra(EXTRA_WAYPOINT_JSON)
                if (!wpJson.isNullOrEmpty()) {
                    val wps = GpsPointJsonHelper.jsonToWaypoints("[$wpJson]")
                    if (wps.isNotEmpty()) {
                        addWaypoint(wps.first())
                    }
                }
            }
            ACTION_ADD_MEDIA -> {
                val path = intent.getStringExtra(EXTRA_MEDIA_PATH)
                if (!path.isNullOrEmpty()) {
                    addMediaPath(path)
                }
            }
        }
        return START_STICKY
    }

    private fun startTracking(
        isSimulation: Boolean,
        trailId: String?,
        trailName: String?,
        referenceTrail: List<GpsPoint>
    ) {
        if (_recordingState.value.isRecording) return

        val initialPoints = if (isSimulation && referenceTrail.isNotEmpty()) {
            listOf(referenceTrail.first())
        } else emptyList()

        val initialLocation = initialPoints.firstOrNull()

        _recordingState.value = RecordingState(
            isRecording = true,
            isPaused = false,
            isSimulating = isSimulation,
            activeTrailId = trailId,
            activeTrailName = trailName,
            referenceTrailPoints = referenceTrail,
            points = initialPoints,
            lastKnownLocation = initialLocation,
            currentAltitude = initialLocation?.altitude ?: 0.0,
            startTimeMillis = System.currentTimeMillis()
        )

        startForegroundServiceWithNotification()

        if (isSimulation) {
            startSimulation(referenceTrail)
        } else {
            startLocationUpdates()
        }
        startTimer()
    }

    private fun pauseTracking() {
        if (!_recordingState.value.isRecording || _recordingState.value.isPaused) return

        _recordingState.update { it.copy(isPaused = true) }
        updateNotification("Trek Paused")
    }

    private fun resumeTracking() {
        if (!_recordingState.value.isRecording || !_recordingState.value.isPaused) return

        _recordingState.update { it.copy(isPaused = false) }
        updateNotification("Trek in Progress")
    }

    private fun stopTracking() {
        stopLocationUpdates()
        timerJob?.cancel()
        simulationJob?.cancel()
        _recordingState.update { it.copy(isRecording = false, isPaused = false) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun addWaypoint(waypoint: TrekWaypoint) {
        _recordingState.update { state ->
            state.copy(waypoints = state.waypoints + waypoint)
        }
    }

    private fun addMediaPath(path: String) {
        _recordingState.update { state ->
            val updated = state.attachedMediaPaths.toMutableList()
            if (!updated.contains(path)) {
                updated.add(path)
            }
            state.copy(attachedMediaPaths = updated)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1500L)
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(1.0f)
                .setWaitForAccurateLocation(false)
                .build()

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
            // Permission missing
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
    }

    private fun startSimulation(referenceTrail: List<GpsPoint>) {
        simulationJob?.cancel()
        simulationJob = serviceScope.launch(Dispatchers.Default) {
            var trailIndex = 0
            var simLat = 45.9237 // Default Mont Blanc base
            var simLng = 6.8694
            var simAlt = 1040.0
            var headingDeg = 35.0

            if (referenceTrail.isNotEmpty()) {
                simLat = referenceTrail[0].latitude
                simLng = referenceTrail[0].longitude
                simAlt = referenceTrail[0].altitude
            }

            while (isActive) {
                delay(1200L)
                val state = _recordingState.value
                if (!state.isRecording || state.isPaused) continue

                val newPoint: GpsPoint
                if (referenceTrail.isNotEmpty()) {
                    trailIndex = (trailIndex + 1) % referenceTrail.size
                    val target = referenceTrail[trailIndex]
                    simLat = target.latitude
                    simLng = target.longitude
                    simAlt = target.altitude
                    newPoint = GpsPoint(
                        latitude = simLat,
                        longitude = simLng,
                        altitude = simAlt,
                        speed = 1.35f,
                        accuracy = 2.5f,
                        timestamp = System.currentTimeMillis()
                    )
                } else {
                    // Generate realistic alpine walk trail steps
                    val dMeters = 2.2
                    val dLat = (dMeters * cos(Math.toRadians(headingDeg))) / 111139.0
                    val dLng = (dMeters * sin(Math.toRadians(headingDeg))) / (111139.0 * cos(Math.toRadians(simLat)))
                    simLat += dLat
                    simLng += dLng
                    simAlt += (0.4 + (Math.random() - 0.45) * 0.6)
                    headingDeg += (Math.random() - 0.5) * 12.0

                    newPoint = GpsPoint(
                        latitude = simLat,
                        longitude = simLng,
                        altitude = simAlt,
                        speed = 1.4f,
                        accuracy = 3.0f,
                        timestamp = System.currentTimeMillis()
                    )
                }

                // Ingest point on Main thread
                serviceScope.launch(Dispatchers.Main) {
                    processSimulatedPoint(newPoint)
                }
            }
        }
    }

    private fun processSimulatedPoint(newPoint: GpsPoint) {
        val state = _recordingState.value
        if (!state.isRecording || state.isPaused) return

        val lastPoint = state.points.lastOrNull()
        val distDelta = if (lastPoint != null) GpsFilter.calculateDistance(lastPoint, newPoint) else 0.0
        val newDistance = state.currentDistanceMeters + distDelta

        val (smoothedAlt, gainDelta, lossDelta) = GpsFilter.processElevation(
            lastAltitude = state.currentAltitude,
            newRawAltitude = newPoint.altitude
        )
        val newGain = state.elevationGainMeters + gainDelta
        val newLoss = state.elevationLossMeters + lossDelta

        val newMinAlt = if (state.minAltitude == 0.0) smoothedAlt else min(state.minAltitude, smoothedAlt)
        val newMaxAlt = max(state.maxAltitude, smoothedAlt)
        val newMaxSpeed = max(state.maxSpeedMps, newPoint.speed)
        val duration = state.elapsedTimeSeconds
        val avgSpeed = if (duration > 0) (newDistance / duration).toFloat() else 0f

        val calories = CalorieCalculator.estimateCalories(
            durationSeconds = duration,
            distanceMeters = newDistance,
            elevationGainMeters = newGain
        )

        _recordingState.value = state.copy(
            points = state.points + newPoint,
            lastKnownLocation = newPoint,
            currentDistanceMeters = newDistance,
            currentSpeedMps = newPoint.speed,
            avgSpeedMps = avgSpeed,
            maxSpeedMps = newMaxSpeed,
            currentAltitude = smoothedAlt,
            minAltitude = newMinAlt,
            maxAltitude = newMaxAlt,
            elevationGainMeters = newGain,
            elevationLossMeters = newLoss,
            estimatedCalories = calories
        )
    }

    private fun processNewLocation(location: Location) {
        val state = _recordingState.value
        if (!state.isRecording || state.isPaused) return

        val newPoint = GpsPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = if (location.hasAltitude()) location.altitude else 0.0,
            speed = if (location.hasSpeed()) location.speed else 0f,
            accuracy = if (location.hasAccuracy()) location.accuracy else 0f,
            timestamp = location.time
        )

        val lastPoint = state.points.lastOrNull()

        // Validate point using GpsFilter
        if (!GpsFilter.isValidPoint(lastPoint, newPoint)) {
            _recordingState.update { it.copy(lastKnownLocation = newPoint) }
            return
        }

        val updatedPoints = state.points + newPoint
        val distDelta = if (lastPoint != null) GpsFilter.calculateDistance(lastPoint, newPoint) else 0.0
        val newDistance = state.currentDistanceMeters + distDelta

        val (smoothedAlt, gainDelta, lossDelta) = GpsFilter.processElevation(
            lastAltitude = state.currentAltitude,
            newRawAltitude = newPoint.altitude
        )
        val newGain = state.elevationGainMeters + gainDelta
        val newLoss = state.elevationLossMeters + lossDelta

        val newMinAlt = if (state.minAltitude == 0.0) smoothedAlt else min(state.minAltitude, smoothedAlt)
        val newMaxAlt = max(state.maxAltitude, smoothedAlt)

        val newMaxSpeed = max(state.maxSpeedMps, newPoint.speed)
        val duration = state.elapsedTimeSeconds
        val avgSpeed = if (duration > 0) (newDistance / duration).toFloat() else 0f

        val calories = CalorieCalculator.estimateCalories(
            durationSeconds = duration,
            distanceMeters = newDistance,
            elevationGainMeters = newGain
        )

        _recordingState.value = state.copy(
            points = updatedPoints,
            lastKnownLocation = newPoint,
            currentDistanceMeters = newDistance,
            currentSpeedMps = newPoint.speed,
            avgSpeedMps = avgSpeed,
            maxSpeedMps = newMaxSpeed,
            currentAltitude = smoothedAlt,
            minAltitude = newMinAlt,
            maxAltitude = newMaxAlt,
            elevationGainMeters = newGain,
            elevationLossMeters = newLoss,
            estimatedCalories = calories
        )

        if (state.elapsedTimeSeconds % 5L == 0L) {
            updateNotification("Trek in Progress")
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                val state = _recordingState.value
                if (state.isRecording && !state.isPaused) {
                    val newElapsed = state.elapsedTimeSeconds + 1
                    val newAvgSpeed = if (newElapsed > 0) (state.currentDistanceMeters / newElapsed).toFloat() else 0f
                    val calories = CalorieCalculator.estimateCalories(
                        durationSeconds = newElapsed,
                        distanceMeters = state.currentDistanceMeters,
                        elevationGainMeters = state.elevationGainMeters
                    )
                    _recordingState.update {
                        it.copy(
                            elapsedTimeSeconds = newElapsed,
                            avgSpeedMps = newAvgSpeed,
                            estimatedCalories = calories
                        )
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Arolock Live Trek",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live distance, time, and elevation while recording a trek"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val state = _recordingState.value
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distStr = UnitFormatter.formatDistance(state.currentDistanceMeters)
        val timeStr = UnitFormatter.formatDuration(state.elapsedTimeSeconds)
        val eleStr = UnitFormatter.formatElevation(state.elevationGainMeters)

        val contentText = "$distStr • $timeStr • $eleStr • ${state.estimatedCalories} kcal"

        val title = if (state.isSimulating) "Arolock (Demo Mode): $statusText" else "Arolock: $statusText"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(pendingOpen)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (state.isPaused) {
            val resumeIntent = Intent(this, TrekTrackingService::class.java).apply { action = ACTION_RESUME }
            val pendingResume = PendingIntent.getService(this, 1, resumeIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_play, "Resume", pendingResume)
        } else {
            val pauseIntent = Intent(this, TrekTrackingService::class.java).apply { action = ACTION_PAUSE }
            val pendingPause = PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_IMMUTABLE)
            builder.addAction(android.R.drawable.ic_media_pause, "Pause", pendingPause)
        }

        return builder.build()
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification("Trek Started")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(statusText: String) {
        val notification = buildNotification(statusText)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        timerJob?.cancel()
        simulationJob?.cancel()
    }

    companion object {
        const val CHANNEL_ID = "arolock_tracking_channel"
        const val NOTIFICATION_ID = 4041

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_ADD_MEDIA = "ACTION_ADD_MEDIA"
        const val ACTION_ADD_WAYPOINT = "ACTION_ADD_WAYPOINT"

        const val EXTRA_SIMULATION = "EXTRA_SIMULATION"
        const val EXTRA_TRAIL_ID = "EXTRA_TRAIL_ID"
        const val EXTRA_TRAIL_NAME = "EXTRA_TRAIL_NAME"
        const val EXTRA_TRAIL_POINTS_JSON = "EXTRA_TRAIL_POINTS_JSON"
        const val EXTRA_MEDIA_PATH = "EXTRA_MEDIA_PATH"
        const val EXTRA_WAYPOINT_JSON = "EXTRA_WAYPOINT_JSON"

        private val _recordingState = MutableStateFlow(RecordingState())
        val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

        fun startService(
            context: Context,
            isSimulation: Boolean = false,
            trailId: String? = null,
            trailName: String? = null,
            trailPoints: List<GpsPoint> = emptyList()
        ) {
            val intent = Intent(context, TrekTrackingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SIMULATION, isSimulation)
                if (trailId != null) putExtra(EXTRA_TRAIL_ID, trailId)
                if (trailName != null) putExtra(EXTRA_TRAIL_NAME, trailName)
                if (trailPoints.isNotEmpty()) {
                    putExtra(EXTRA_TRAIL_POINTS_JSON, GpsPointJsonHelper.pointsToJson(trailPoints))
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pauseService(context: Context) {
            val intent = Intent(context, TrekTrackingService::class.java).apply { action = ACTION_PAUSE }
            context.startService(intent)
        }

        fun resumeService(context: Context) {
            val intent = Intent(context, TrekTrackingService::class.java).apply { action = ACTION_RESUME }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, TrekTrackingService::class.java).apply { action = ACTION_STOP }
            context.startService(intent)
        }

        fun addWaypoint(context: Context, waypoint: TrekWaypoint) {
            val intent = Intent(context, TrekTrackingService::class.java).apply {
                action = ACTION_ADD_WAYPOINT
                putExtra(EXTRA_WAYPOINT_JSON, GpsPointJsonHelper.waypointsToJson(listOf(waypoint)).trim('[', ']'))
            }
            context.startService(intent)
        }

        fun attachMedia(context: Context, path: String) {
            val intent = Intent(context, TrekTrackingService::class.java).apply {
                action = ACTION_ADD_MEDIA
                putExtra(EXTRA_MEDIA_PATH, path)
            }
            context.startService(intent)
        }

        fun resetState() {
            _recordingState.value = RecordingState()
        }
    }
}
