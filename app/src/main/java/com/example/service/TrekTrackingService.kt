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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.UserPreferences
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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class TrekTrackingService : Service(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sensorManager: SensorManager

    private var stepCounterSensor: Sensor? = null
    private var stepDetectorSensor: Sensor? = null
    private var pressureSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    // Sensor State
    private var initialStepCount = -1
    private var currentLiveSteps = 0
    private var baseBarometerAltitude: Double? = null
    private var lastBarometerAltitude: Double? = null
    private var userWeightKg: Float = 70f
    private var useMetricUnits: Boolean = true

    // Accelerometer-based step detector fallback (works on 100% of Android hardware)
    private var lastAccelMagnitude = 9.8f
    private var lastStepTimestamp = 0L
    private val stepThreshold = 11.4f // m/s^2 peak threshold for brisk walking
    private val minStepIntervalMs = 280L

    // Speed tracking
    private var lastMovementTimestamp = 0L

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Query physical hardware sensors
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Read user profile preferences
        try {
            val prefs = UserPreferences(this)
            val profile = prefs.userProfile.value
            userWeightKg = profile.weightKg
            useMetricUnits = profile.useMetric
        } catch (_: Exception) {}

        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    processRealGpsLocation(location)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val trailId = intent.getStringExtra(EXTRA_TRAIL_ID)
                val trailName = intent.getStringExtra(EXTRA_TRAIL_NAME)
                val trailPointsJson = intent.getStringExtra(EXTRA_TRAIL_POINTS_JSON)
                val trailPoints = GpsPointJsonHelper.jsonToPoints(trailPointsJson)
                startTracking(trailId, trailName, trailPoints)
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
        trailId: String?,
        trailName: String?,
        referenceTrail: List<GpsPoint>
    ) {
        if (_recordingState.value.isRecording) return

        initialStepCount = -1
        currentLiveSteps = 0
        baseBarometerAltitude = null
        lastBarometerAltitude = null
        lastMovementTimestamp = System.currentTimeMillis()

        val hasStep = (stepCounterSensor != null || stepDetectorSensor != null || accelerometerSensor != null)
        val hasBaro = (pressureSensor != null)

        _recordingState.value = RecordingState(
            isRecording = true,
            isPaused = false,
            isSimulating = false,
            activeTrailId = trailId,
            activeTrailName = trailName,
            referenceTrailPoints = referenceTrail,
            points = emptyList(),
            lastKnownLocation = null,
            currentAltitude = 0.0,
            hasBarometer = hasBaro,
            hasStepSensor = hasStep,
            startTimeMillis = System.currentTimeMillis()
        )

        startForegroundServiceWithNotification()
        startLocationUpdates()
        registerSensors()
        startTimer()
    }

    private fun registerSensors() {
        // Register step counter
        stepCounterSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        // Register step detector
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        // Register accelerometer for fallback step detection
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        // Register atmospheric pressure (barometer)
        pressureSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun unregisterSensors() {
        try {
            sensorManager.unregisterListener(this)
        } catch (_: Exception) {}
    }

    private fun pauseTracking() {
        if (!_recordingState.value.isRecording || _recordingState.value.isPaused) return
        _recordingState.update { it.copy(isPaused = true) }
        unregisterSensors()
        updateNotification("Trek Paused")
    }

    private fun resumeTracking() {
        if (!_recordingState.value.isRecording || !_recordingState.value.isPaused) return
        _recordingState.update { it.copy(isPaused = false) }
        registerSensors()
        lastMovementTimestamp = System.currentTimeMillis()
        updateNotification("Trek in Progress")
    }

    private fun stopTracking() {
        stopLocationUpdates()
        unregisterSensors()
        timerJob?.cancel()
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
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .setMinUpdateDistanceMeters(0f)
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

    /**
     * Ingests real GPS location from Android device hardware
     */
    private fun processRealGpsLocation(location: Location) {
        val state = _recordingState.value
        if (!state.isRecording || state.isPaused) return

        val now = location.time
        val hasAltitude = location.hasAltitude()
        val rawAltitude = if (hasAltitude) location.altitude else state.currentAltitude
        val accuracy = if (location.hasAccuracy()) location.accuracy else 0f

        val lastPoint = state.points.lastOrNull()
        val distDelta = if (lastPoint != null) {
            val results = FloatArray(1)
            Location.distanceBetween(
                lastPoint.latitude, lastPoint.longitude,
                location.latitude, location.longitude,
                results
            )
            results[0].toDouble()
        } else {
            0.0
        }

        val timeDeltaSeconds = if (lastPoint != null) {
            (now - lastPoint.timestamp) / 1000.0
        } else 0.0

        // Calculate real instantaneous speed
        val computedSpeedMps: Float = when {
            location.hasSpeed() && location.speed >= 0.1f -> location.speed
            timeDeltaSeconds > 0.5 && distDelta >= 0.5 -> (distDelta / timeDeltaSeconds).toFloat()
            distDelta < 0.3 -> 0f
            else -> state.currentSpeedMps * 0.5f // Smooth decay
        }

        if (computedSpeedMps > 0.1f) {
            lastMovementTimestamp = System.currentTimeMillis()
        }

        // Validate point using GpsFilter
        val tempPoint = GpsPoint(
            latitude = location.latitude,
            longitude = location.longitude,
            altitude = rawAltitude,
            speed = computedSpeedMps,
            accuracy = accuracy,
            timestamp = now
        )

        val isValidMovement = GpsFilter.isValidPoint(lastPoint, tempPoint)

        val (smoothedAlt, gainDelta, lossDelta) = if (!state.hasBarometer && hasAltitude) {
            GpsFilter.processElevation(
                lastAltitude = state.currentAltitude,
                newRawAltitude = rawAltitude,
                thresholdMeters = 2.0f
            )
        } else {
            Triple(state.currentAltitude, 0.0, 0.0)
        }

        val newPoints = if (isValidMovement || state.points.isEmpty()) {
            state.points + tempPoint
        } else {
            state.points
        }

        val newDistance = if (isValidMovement) state.currentDistanceMeters + distDelta else state.currentDistanceMeters
        val newGain = state.elevationGainMeters + gainDelta
        val newLoss = state.elevationLossMeters + lossDelta

        val currentAlt = if (state.hasBarometer && state.currentAltitude != 0.0) state.currentAltitude else smoothedAlt
        val newMinAlt = if (state.minAltitude == 0.0) currentAlt else min(state.minAltitude, currentAlt)
        val newMaxAlt = max(state.maxAltitude, currentAlt)

        val newMaxSpeed = max(state.maxSpeedMps, computedSpeedMps)
        val duration = state.elapsedTimeSeconds
        val avgSpeed = if (duration > 0) (newDistance / duration).toFloat() else 0f

        val calories = CalorieCalculator.estimateCalories(
            durationSeconds = duration,
            distanceMeters = newDistance,
            elevationGainMeters = newGain,
            weightKg = userWeightKg,
            stepCount = state.stepCount
        )

        _recordingState.value = state.copy(
            points = newPoints,
            lastKnownLocation = tempPoint,
            gpsAccuracyMeters = accuracy,
            currentDistanceMeters = newDistance,
            currentSpeedMps = computedSpeedMps,
            avgSpeedMps = avgSpeed,
            maxSpeedMps = newMaxSpeed,
            currentAltitude = currentAlt,
            minAltitude = newMinAlt,
            maxAltitude = newMaxAlt,
            elevationGainMeters = newGain,
            elevationLossMeters = newLoss,
            estimatedCalories = calories
        )
    }

    /**
     * Handles real physical sensor events: Step Counter, Step Detector, Barometer, and Accelerometer
     */
    override fun onSensorChanged(event: SensorEvent?) {
        val state = _recordingState.value
        if (!state.isRecording || state.isPaused || event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val cumulativeSteps = event.values[0].toInt()
                if (initialStepCount < 0) {
                    initialStepCount = cumulativeSteps
                }
                val delta = max(0, cumulativeSteps - initialStepCount)
                if (delta != currentLiveSteps) {
                    currentLiveSteps = delta
                    onNewStepDetected()
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values[0] == 1.0f) {
                    currentLiveSteps++
                    onNewStepDetected()
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // If neither step counter nor detector has fired recently, use high-precision accelerometer fallback
                if (stepCounterSensor == null && stepDetectorSensor == null) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    val deltaAccel = magnitude - lastAccelMagnitude
                    lastAccelMagnitude = magnitude

                    val now = System.currentTimeMillis()
                    if (magnitude > stepThreshold && deltaAccel > 1.2f && (now - lastStepTimestamp) > minStepIntervalMs) {
                        lastStepTimestamp = now
                        currentLiveSteps++
                        onNewStepDetected()
                    }
                }
            }
            Sensor.TYPE_PRESSURE -> {
                val pressureHpa = event.values[0]
                // Compute barometric altitude in meters
                val rawBaroAlt = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressureHpa).toDouble()

                if (baseBarometerAltitude == null) {
                    baseBarometerAltitude = rawBaroAlt
                    lastBarometerAltitude = rawBaroAlt
                    if (state.currentAltitude == 0.0) {
                        _recordingState.update { it.copy(currentAltitude = rawBaroAlt, minAltitude = rawBaroAlt, maxAltitude = rawBaroAlt) }
                    }
                } else {
                    val prevAlt = lastBarometerAltitude ?: rawBaroAlt
                    val altDelta = rawBaroAlt - prevAlt

                    // Filter micro barometric atmospheric jitter (< 0.6m)
                    if (abs(altDelta) >= 0.6) {
                        lastBarometerAltitude = rawBaroAlt
                        val newGain = if (altDelta > 0) state.elevationGainMeters + altDelta else state.elevationGainMeters
                        val newLoss = if (altDelta < 0) state.elevationLossMeters + abs(altDelta) else state.elevationLossMeters
                        val newMin = if (state.minAltitude == 0.0) rawBaroAlt else min(state.minAltitude, rawBaroAlt)
                        val newMax = max(state.maxAltitude, rawBaroAlt)

                        _recordingState.update {
                            it.copy(
                                currentAltitude = rawBaroAlt,
                                elevationGainMeters = newGain,
                                elevationLossMeters = newLoss,
                                minAltitude = newMin,
                                maxAltitude = newMax
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onNewStepDetected() {
        val state = _recordingState.value
        val durationMinutes = state.elapsedTimeSeconds / 60.0
        val cadence = if (durationMinutes > 0) (currentLiveSteps / durationMinutes).toInt() else 0

        // If distance from GPS is 0 or user is walking with device indoors, calculate realistic distance from step stride
        // Average human hiking stride is ~0.76 meters per step
        val stepStrideMeters = 0.76
        val stepDistance = currentLiveSteps * stepStrideMeters
        val finalDistance = max(state.currentDistanceMeters, if (state.points.size <= 2) stepDistance else state.currentDistanceMeters)

        val calories = CalorieCalculator.estimateCalories(
            durationSeconds = state.elapsedTimeSeconds,
            distanceMeters = finalDistance,
            elevationGainMeters = state.elevationGainMeters,
            weightKg = userWeightKg,
            stepCount = currentLiveSteps
        )

        lastMovementTimestamp = System.currentTimeMillis()

        _recordingState.update {
            it.copy(
                stepCount = currentLiveSteps,
                cadenceSpm = cadence,
                currentDistanceMeters = finalDistance,
                estimatedCalories = calories
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                val state = _recordingState.value
                if (state.isRecording && !state.isPaused) {
                    val newElapsed = state.elapsedTimeSeconds + 1
                    val newAvgSpeed = if (newElapsed > 0) (state.currentDistanceMeters / newElapsed).toFloat() else 0f

                    // If no real movement has occurred in > 3 seconds, reset live instantaneous speed to 0
                    val timeSinceMovement = System.currentTimeMillis() - lastMovementTimestamp
                    val liveSpeed = if (timeSinceMovement > 3500L) 0f else state.currentSpeedMps

                    val calories = CalorieCalculator.estimateCalories(
                        durationSeconds = newElapsed,
                        distanceMeters = state.currentDistanceMeters,
                        elevationGainMeters = state.elevationGainMeters,
                        weightKg = userWeightKg,
                        stepCount = state.stepCount
                    )

                    val durationMinutes = newElapsed / 60.0
                    val cadence = if (durationMinutes > 0) (state.stepCount / durationMinutes).toInt() else 0

                    _recordingState.update {
                        it.copy(
                            elapsedTimeSeconds = newElapsed,
                            currentSpeedMps = liveSpeed,
                            avgSpeedMps = newAvgSpeed,
                            cadenceSpm = cadence,
                            estimatedCalories = calories
                        )
                    }

                    if (newElapsed % 4L == 0L) {
                        updateNotification("Trek in Progress")
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
                description = "Shows live distance, time, steps, and elevation while recording a trek"
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

        val distStr = UnitFormatter.formatDistance(state.currentDistanceMeters, useMetricUnits)
        val timeStr = UnitFormatter.formatDuration(state.elapsedTimeSeconds)
        val eleStr = UnitFormatter.formatElevation(state.elevationGainMeters, useMetricUnits)
        val speedStr = UnitFormatter.formatSpeed(state.currentSpeedMps, useMetricUnits)
        val stepStr = "${state.stepCount} steps"

        val contentText = "$distStr • $speedStr • $stepStr • $eleStr"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Arolock: $statusText")
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

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
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

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        unregisterSensors()
        timerJob?.cancel()
    }

    companion object {
        const val ACTION_START = "com.example.service.action.START"
        const val ACTION_PAUSE = "com.example.service.action.PAUSE"
        const val ACTION_RESUME = "com.example.service.action.RESUME"
        const val ACTION_STOP = "com.example.service.action.STOP"
        const val ACTION_ADD_WAYPOINT = "com.example.service.action.ADD_WAYPOINT"
        const val ACTION_ADD_MEDIA = "com.example.service.action.ADD_MEDIA"

        const val EXTRA_TRAIL_ID = "extra_trail_id"
        const val EXTRA_TRAIL_NAME = "extra_trail_name"
        const val EXTRA_TRAIL_POINTS_JSON = "extra_trail_points_json"
        const val EXTRA_WAYPOINT_JSON = "extra_waypoint_json"
        const val EXTRA_MEDIA_PATH = "extra_media_path"

        private const val NOTIFICATION_ID = 2001
        private const val CHANNEL_ID = "arolock_live_trek_channel"

        private val _recordingState = MutableStateFlow(RecordingState())
        val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

        fun startService(
            context: Context,
            trailId: String? = null,
            trailName: String? = null,
            trailPoints: List<GpsPoint> = emptyList()
        ) {
            val intent = Intent(context, TrekTrackingService::class.java).apply {
                action = ACTION_START
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
