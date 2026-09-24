package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.NightCardBorder
import com.example.ui.theme.NightSurface
import com.example.ui.theme.PineDeep
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SkyTrail
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.TrekViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: TrekViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Create Account
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    fun handleGoogleSignIn() {
        isLoading = true
        coroutineScope.launch {
            delay(600L) // Subtle realistic authentication feedback
            viewModel.loginWithGoogle(
                displayName = "Naveed Ali",
                email = "naveedalicodes1@gmail.com"
            )
            isLoading = false
            Toast.makeText(context, "Signed in with Google as Naveed Ali", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    fun handleEmailAuth() {
        if (email.isBlank() || !email.contains("@")) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        coroutineScope.launch {
            delay(500L)
            val name = if (selectedTab == 1 && displayName.isNotBlank()) displayName else email.substringBefore("@")
            viewModel.loginWithEmail(name, email)
            isLoading = false
            Toast.makeText(context, "Welcome back, $name!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    fun handleGuestMode() {
        viewModel.continueAsGuest()
        Toast.makeText(context, "Exploring in Offline Trekker Mode", Toast.LENGTH_SHORT).show()
        onLoginSuccess()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NightBlack),
        contentAlignment = Alignment.TopCenter
    ) {
        // Decorative Topo Canvas Background
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
        ) {
            val width = size.width
            val height = size.height
            val stroke = Stroke(width = 1.5f)

            // Draw contoured topo elevation lines
            for (i in 1..7) {
                val path = Path()
                val yOffset = height * (i / 8f)
                path.moveTo(0f, yOffset)
                path.cubicTo(
                    width * 0.25f, yOffset - 30f + (i * 8f),
                    width * 0.65f, yOffset + 35f - (i * 6f),
                    width, yOffset
                )
                drawPath(
                    path = path,
                    color = SageGreen.copy(alpha = 0.04f + (i * 0.015f)),
                    style = stroke
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Brand Emblem Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SageGreen.copy(alpha = 0.35f), NightCard)
                        )
                    )
                    .border(2.dp, SageGreen.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Terrain,
                    contentDescription = "Arolock Emblem",
                    tint = SageGreen,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "AROLOCK",
                color = TextPrimaryDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )

            Text(
                text = "WILDERNESS TELEMETRY & EXPEDITIONS",
                color = AmberGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Card
            Card(
                colors = CardDefaults.cardColors(containerColor = NightCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NightCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Sign In vs Register Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = NightSurface,
                        contentColor = SageGreen,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = SageGreen,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "Sign In",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) SageGreen else TextSecondaryDark
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "New Account",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) SageGreen else TextSecondaryDark
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // One-Click Google Authentication Button
                    Button(
                        onClick = { handleGoogleSignIn() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_login_button")
                    ) {
                        // Google 'G' Symbol
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        color = Color(0xFF4285F4),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Continue with Google",
                                color = Color(0xFF1F1F1F),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Or with divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = NightCardBorder
                        )
                        Text(
                            text = "  or with email  ",
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = NightCardBorder
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Name (Only when Registering)
                    AnimatedVisibility(visible = selectedTab == 1) {
                        Column {
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Trail Nickname / Name") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = SageGreen
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SageGreen,
                                    unfocusedBorderColor = NightCardBorder,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark,
                                    focusedLabelColor = SageGreen,
                                    unfocusedLabelColor = TextSecondaryDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_field")
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("explorer@mountain.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = SageGreen
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = NightCardBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedLabelColor = SageGreen,
                            unfocusedLabelColor = TextSecondaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = SageGreen
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = TextSecondaryDark
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { handleEmailAuth() }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = NightCardBorder,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark,
                            focusedLabelColor = SageGreen,
                            unfocusedLabelColor = TextSecondaryDark
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_field")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Submit Button
                    Button(
                        onClick = { handleEmailAuth() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = NightBlack,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (selectedTab == 0) "SIGN IN TO AROLOCK" else "CREATE FREE ACCOUNT",
                                color = NightBlack,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Guest / Offline Mode Button
                    FilledTonalButton(
                        onClick = { handleGuestMode() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = NightSurface,
                            contentColor = TextSecondaryDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .border(1.dp, NightCardBorder, RoundedCornerShape(14.dp))
                            .testTag("guest_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Continue as Offline Guest",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Highlights Badge Bar (Pro Aesthetics)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NightCard.copy(alpha = 0.6f))
                    .border(1.dp, NightCardBorder, RoundedCornerShape(14.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FeaturePill(icon = Icons.Default.DirectionsWalk, label = "Step Sensor", tint = SageGreen)
                FeaturePill(icon = Icons.Default.Terrain, label = "Baro Elev.", tint = AmberGold)
                FeaturePill(icon = Icons.Default.CameraAlt, label = "Photo Log", tint = SkyTrail)
                FeaturePill(icon = Icons.Default.Explore, label = "100% Offline", tint = Terracotta)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeaturePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = TextSecondaryDark,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
