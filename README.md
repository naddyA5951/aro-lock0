# Arolock — Outdoor Trekking & Trail GPS Explorer

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM%20%2B%20Offline--First-orange.svg)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-teal.svg)](LICENSE)

Arolock is a high-performance, offline-first trekking and hiking application built natively for Android using Kotlin and Jetpack Compose. Designed specifically for alpine mountaineering, backcountry hiking, and wilderness exploration where cellular coverage is unavailable.

---

## Key Features

### 1. High-Precision GPS Telemetry
- **Foreground Tracking Service:** Continuous location recording with an ongoing Android system notification. Never loses GPS points when the screen is locked or while switching apps to take photos.
- **Smart GPS Filter & Spike Rejection:** Automatically rejects satellite multi-path jumps (accuracy > 40m) and impossible teleport spikes.
- **Elevation Noise Smoothing:** Employs an exponential moving average low-pass filter with a configurable altitude delta threshold (default 3m) to eliminate vertical barometer/GPS jitter.

### 2. Interactive Route Mapping
- **OpenStreetMap Integration:** Fast, lightweight tile rendering directly onto Compose Canvas.
- **Offline Topographic Fallback:** Seamlessly displays a vector topographic grid, scale lines, coordinates, and full route when completely out of network coverage.
- **Live User Beacon:** Animated pulsing compass marker with accuracy radius.
- **Dynamic Polyline Styling:** Multi-stop gradient route line (Forest Green → Amber Gold → Terracotta) highlighting elevation and distance.
- **Navigation Controls:** Pinch-to-zoom, pan gestures, zoom in/out buttons, and a single-tap "Center on Me / Follow Trek" lock mode.

### 3. Comprehensive Performance Metrics
- **Real-time Stats:** Distance, Elapsed Time, Current Speed, Average Speed, Maximum Speed, Current Altitude, Minimum Altitude, Maximum Altitude, Elevation Gain, and Elevation Loss.
- **MET-Based Calorie Estimation:** Calculates estimated calorie burn based on Metabolic Equivalent of Task (MET), adjusted for walking speed, hiker weight, and elevation incline.
- **Elevation Profile:** Visual elevation curve rendered with smooth gradient fill and min/max markers.

### 4. Trail Memories & Media
- **Photo & Video Journal:** Attach trail photos and videos taken during the hike or selected from the gallery.
- **Android Photo Picker:** Utilizes modern zero-permission photo and video selection (`PickVisualMedia`).
- **Fullscreen Preview:** Inspect and review media with an in-app lightbox viewer.

### 5. Logbook & Data Persistence
- **Room SQLite Database:** 100% local persistence. All routes, GPS coordinates, timestamps, and photos remain on the device.
- **Search & Filters:** Search treks by trail name or filter by difficulty level (Easy, Moderate, Hard, Extreme).
- **Edit & Delete:** Full control to edit trek names, descriptions, or remove past treks.

### 6. GPX 1.1 Export & Sharing
- **Standard GPX Output:** Exports recorded treks to standard `.gpx` XML format containing metadata, track segments, latitude, longitude, elevation, and UTC timestamps.
- **Universal Share Sheet:** Send routes directly to friends, hiking apps, or desktop mapping tools via Android's native share sheet.

### 7. Customization & Privacy
- **Measurement Systems:** Toggle between Metric (`km`, `m`, `km/h`) and Imperial (`mi`, `ft`, `mph`).
- **User Profile:** Configure name, age, weight, and gender for precise calorie calculations.
- **Elevation Sensitivity:** Fine-tune noise thresholds from 1 to 10 meters.
- **Zero Account & Zero Tracking:** No logins, no passwords, no servers, and zero cloud tracking. 100% private.

---

## Technical Architecture

- **UI Layer:** Jetpack Compose (Material 3), Edge-to-Edge display, Custom Outdoor Dark Theme.
- **State Management:** MVVM with Kotlin Coroutines and reactive `StateFlow`.
- **Database:** Android Room 2.6 with KSP (Kotlin Symbol Processing).
- **Location Engine:** Google Play Services `FusedLocationProviderClient` with `PRIORITY_HIGH_ACCURACY`.
- **Image Loading:** Coil 3 for Compose.
- **Networking:** OkHttp3 for OpenStreetMap tile caching.

---

## Permissions Declared

| Permission | Purpose |
|------------|---------|
| `ACCESS_FINE_LOCATION` | High-accuracy GPS positioning for trail recording. |
| `ACCESS_COARSE_LOCATION` | Approximate network-based location fallback. |
| `FOREGROUND_SERVICE_LOCATION` | Continuous GPS tracking while the app is in background or screen is off. |
| `POST_NOTIFICATIONS` | Ongoing tracking notification with live distance and duration (Android 13+). |
| `INTERNET` | Optional fetching of OpenStreetMap base tiles when online. |

---

## Building the Application

### Prerequisites
- JDK 17 or higher
- Android SDK with Platform 36 and Build-Tools

### Build Debug APK
```bash
gradle :app:assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Run Unit Tests
```bash
gradle :app:testDebugUnitTest
```

---

## Releases & Export

To export the project:
1. Use the **Settings menu** in Google AI Studio to download the project ZIP or push to GitHub.
2. Build signed release APKs using the release signing config in `app/build.gradle.kts`.
