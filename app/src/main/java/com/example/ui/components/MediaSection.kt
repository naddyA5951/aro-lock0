package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ui.theme.AmberGold
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.NightCardBorder
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.io.File
import java.io.FileOutputStream

@Composable
fun MediaSection(
    mediaPaths: List<String>,
    onAddMedia: (String) -> Unit,
    onRemoveMedia: ((String) -> Unit)? = null,
    isEditable: Boolean = true,
    title: String = "CAPTURED PHOTOS & MEMORIES",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedMediaPreview by remember { mutableStateOf<String?>(null) }
    var showSourceChooserDialog by remember { mutableStateOf(false) }

    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher for taking photo with Camera
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraFile != null && pendingCameraFile!!.exists() && pendingCameraFile!!.length() > 0) {
            onAddMedia(pendingCameraFile!!.absolutePath)
            Toast.makeText(context, "Photo captured successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val photoFile = File(context.filesDir, "trek_photo_${System.currentTimeMillis()}.jpg")
                pendingCameraFile = photoFile
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                pendingCameraUri = uri
                takePhotoLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    // Modern Android Photo Picker (zero-permission gallery selector)
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val destFile = File(context.filesDir, "trek_upload_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onAddMedia(destFile.absolutePath)
                Toast.makeText(context, "Photo added to trek!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val photoFile = File(context.filesDir, "trek_photo_${System.currentTimeMillis()}.jpg")
                pendingCameraFile = photoFile
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                pendingCameraUri = uri
                takePhotoLauncher.launch(uri)
            } catch (e: Exception) {
                Toast.makeText(context, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Collections,
                    contentDescription = null,
                    tint = SageGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$title (${mediaPaths.size})",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (isEditable) {
                FilledTonalButton(
                    onClick = { showSourceChooserDialog = true },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = SageGreen.copy(alpha = 0.15f),
                        contentColor = SageGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_media_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (mediaPaths.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NightCard)
                    .border(1.dp, NightCardBorder, RoundedCornerShape(14.dp))
                    .clickable(enabled = isEditable) { showSourceChooserDialog = true }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = SageGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isEditable) "Capture or upload trail photos" else "No photos attached to this expedition",
                            color = TextPrimaryDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isEditable) "Tap here to take photo or pick from gallery" else "Photos taken during recording appear here",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mediaPaths) { path ->
                    val isVideo = path.endsWith(".mp4", ignoreCase = true) || path.endsWith(".mov", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NightCard)
                            .border(1.dp, NightCardBorder, RoundedCornerShape(14.dp))
                            .clickable { selectedMediaPreview = path }
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Trail photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle bottom gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                    )
                                )
                        )

                        if (isVideo) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        if (isEditable && onRemoveMedia != null) {
                            IconButton(
                                onClick = { onRemoveMedia(path) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Extra "+ Add Photo" card in the row for quick additions
                if (isEditable) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(NightCard.copy(alpha = 0.7f))
                                .border(1.dp, SageGreen.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .clickable { showSourceChooserDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Add More",
                                    tint = SageGreen,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+ Add",
                                    color = SageGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Photo Source Chooser Dialog: Camera vs Gallery
    if (showSourceChooserDialog) {
        AlertDialog(
            onDismissRequest = { showSourceChooserDialog = false },
            title = {
                Text(
                    text = "Add Trail Photo",
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose how you would like to add this photo to your expedition:",
                        color = TextSecondaryDark,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Take Photo with Camera
                    Button(
                        onClick = {
                            showSourceChooserDialog = false
                            launchCamera()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("choose_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = NightBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Take Photo with Camera",
                            color = NightBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Option 2: Upload from Gallery
                    FilledTonalButton(
                        onClick = {
                            showSourceChooserDialog = false
                            galleryPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NightCard,
                            contentColor = TextPrimaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, NightCardBorder, RoundedCornerShape(12.dp))
                            .testTag("choose_gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upload from Photos / Gallery",
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceChooserDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = NightBlack,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // High-Resolution Media Fullscreen Preview Dialog
    selectedMediaPreview?.let { path ->
        Dialog(onDismissRequest = { selectedMediaPreview = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NightBlack,
                border = androidx.compose.foundation.BorderStroke(1.dp, NightCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = File(path),
                        contentDescription = "Full Trail Photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
                    )

                    // Top Bar with Close
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trail Photo",
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        IconButton(
                            onClick = { selectedMediaPreview = null },
                            modifier = Modifier
                                .size(32.dp)
                                .background(NightCard, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
