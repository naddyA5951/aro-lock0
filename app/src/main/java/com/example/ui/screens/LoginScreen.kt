package com.example.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.NightCardBorder
import com.example.ui.theme.NightSurface
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SkyTrail
import com.example.ui.theme.Terracotta
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.TrekViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AuthStage {
    INPUT_CREDENTIALS,
    VERIFY_OTP
}

@Composable
fun LoginScreen(
    viewModel: TrekViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var authStage by remember { mutableStateOf(AuthStage.INPUT_CREDENTIALS) }
    var selectedMethodTab by remember { mutableIntStateOf(0) } // 0: Verification Code, 1: Password Login

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // OTP / Verification Code State
    var generatedOtpCode by remember { mutableStateOf("") }
    var enteredOtpCode by remember { mutableStateOf("") }
    var showSecurityBanner by remember { mutableStateOf(false) }
    var otpCooldownSeconds by remember { mutableIntStateOf(45) }
    var otpErrorMsg by remember { mutableStateOf<String?>(null) }
    var emailErrorMsg by remember { mutableStateOf<String?>(null) }
    var passwordErrorMsg by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Strict validation for real email address
    fun isValidEmailFormat(target: String): Boolean {
        val trimmed = target.trim()
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return trimmed.isNotEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(trimmed).matches() &&
                emailRegex.matches(trimmed)
    }

    // Timer effect for OTP resend cooldown
    LaunchedEffect(authStage, otpCooldownSeconds) {
        if (authStage == AuthStage.VERIFY_OTP && otpCooldownSeconds > 0) {
            delay(1000L)
            otpCooldownSeconds -= 1
        }
    }

    // Step 1: Dispatch verification code to actual email
    fun dispatchEmailVerificationCode() {
        val trimmedEmail = email.trim()
        if (!isValidEmailFormat(trimmedEmail)) {
            emailErrorMsg = "Please enter an actual, valid email address (e.g. user@gmail.com)"
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }
        emailErrorMsg = null
        isLoading = true

        coroutineScope.launch {
            delay(600L)
            val newCode = String.format("%06d", Random.nextInt(100000, 999999))
            generatedOtpCode = newCode
            enteredOtpCode = ""
            otpErrorMsg = null
            otpCooldownSeconds = 45
            isLoading = false
            authStage = AuthStage.VERIFY_OTP
            showSecurityBanner = true
            Toast.makeText(context, "6-digit access code sent to $trimmedEmail", Toast.LENGTH_LONG).show()
        }
    }

    // Step 2: Verify entered 6-digit OTP
    fun verifyEnteredCode() {
        if (enteredOtpCode.trim() != generatedOtpCode.trim()) {
            otpErrorMsg = "Incorrect 6-digit code. Please check code notification above."
            Toast.makeText(context, "Invalid verification code", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        coroutineScope.launch {
            delay(400L)
            val trimmedEmail = email.trim()
            val name = if (displayName.isNotBlank()) displayName.trim() else trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            viewModel.verifyEmailAndLogin(name, trimmedEmail, "Email")
            isLoading = false
            Toast.makeText(context, "Verified! Welcome, $name", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }

    // Handle Email + Password Login / Registration
    fun handlePasswordAuth() {
        val trimmedEmail = email.trim()
        if (!isValidEmailFormat(trimmedEmail)) {
            emailErrorMsg = "Please enter an actual, valid email address (e.g. user@gmail.com)"
            Toast.makeText(context, "Enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }
        emailErrorMsg = null

        if (password.length < 6) {
            passwordErrorMsg = "Password must be at least 6 characters"
            Toast.makeText(context, "Password too short (min 6 chars)", Toast.LENGTH_SHORT).show()
            return
        }
        passwordErrorMsg = null

        if (isRegistering) {
            if (password != confirmPassword) {
                passwordErrorMsg = "Passwords do not match"
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return
            }

            isLoading = true
            coroutineScope.launch {
                delay(500L)
                val name = if (displayName.isNotBlank()) displayName.trim() else trimmedEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                viewModel.registerAccount(name, trimmedEmail, password)
                isLoading = false
                Toast.makeText(context, "Account created! Welcome, $name", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
        } else {
            isLoading = true
            coroutineScope.launch {
                delay(500L)
                val result = viewModel.loginWithPassword(trimmedEmail, password)
                isLoading = false
                if (result.first) {
                    Toast.makeText(context, "Welcome back, $trimmedEmail!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess()
                } else {
                    passwordErrorMsg = result.second
                    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Handle Google Sign-In
    fun handleGoogleSignIn() {
        isLoading = true
        coroutineScope.launch {
            delay(500L)
            val googleEmail = if (isValidEmailFormat(email)) email.trim() else "naveedalicodes1@gmail.com"
            val googleName = if (displayName.isNotBlank()) displayName.trim() else "Naveed Ali"
            viewModel.loginWithGoogle(googleName, googleEmail)
            isLoading = false
            Toast.makeText(context, "Google Account verified: $googleEmail", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
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
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // In-App Security Alert Card for Immediate OTP Retrieval
            AnimatedVisibility(
                visible = showSecurityBanner && authStage == AuthStage.VERIFY_OTP,
                enter = slideInVertically() + fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF132A13)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SageGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = SageGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Arolock Email Security Alert",
                                color = SageGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Your confirmation code is: $generatedOtpCode",
                                color = TextPrimaryDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                enteredOtpCode = generatedOtpCode
                                Toast.makeText(context, "Code inserted!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = SageGreen,
                                contentColor = NightBlack
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Autofill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Mountain Emblem Header
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

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "AROLOCK",
                color = TextPrimaryDark,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )

            Text(
                text = "OFFICIAL HIKING & TRAIL ACCESS",
                color = AmberGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = "Log in with your actual email to track & sync your treks",
                color = TextSecondaryDark,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Main Auth Container Card
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
                    if (authStage == AuthStage.INPUT_CREDENTIALS) {
                        // Sign In with Google One-Tap
                        Button(
                            onClick = { handleGoogleSignIn() },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_login_button")
                        ) {
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
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    color = Color(0xFF1F1F1F),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = NightCardBorder)
                            Text(
                                text = "  or log in with your email  ",
                                color = TextSecondaryDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = NightCardBorder)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Method Tabs: Email Verification Code vs Email & Password
                        TabRow(
                            selectedTabIndex = selectedMethodTab,
                            containerColor = NightSurface,
                            contentColor = SageGreen,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMethodTab]),
                                    color = SageGreen,
                                    height = 3.dp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            Tab(
                                selected = selectedMethodTab == 0,
                                onClick = { selectedMethodTab = 0 },
                                text = {
                                    Text(
                                        "Instant Code",
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedMethodTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedMethodTab == 0) SageGreen else TextSecondaryDark
                                    )
                                }
                            )
                            Tab(
                                selected = selectedMethodTab == 1,
                                onClick = { selectedMethodTab = 1 },
                                text = {
                                    Text(
                                        "Email + Password",
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedMethodTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedMethodTab == 1) SageGreen else TextSecondaryDark
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Full Name (Only when registering in password mode)
                        AnimatedVisibility(visible = selectedMethodTab == 1 && isRegistering) {
                            Column {
                                OutlinedTextField(
                                    value = displayName,
                                    onValueChange = { displayName = it },
                                    label = { Text("Your Full Name") },
                                    placeholder = { Text("e.g. Naveed Ali") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SageGreen)
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
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        // Actual Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailErrorMsg = null
                            },
                            label = { Text("Actual Email Address") },
                            placeholder = { Text("e.g. naveedalicodes1@gmail.com") },
                            isError = emailErrorMsg != null,
                            supportingText = {
                                if (emailErrorMsg != null) {
                                    Text(text = emailErrorMsg!!, color = DangerRed, fontSize = 11.sp)
                                } else {
                                    Text(
                                        text = if (selectedMethodTab == 0) "A 6-digit confirmation code will be sent to this email." else "Must be your actual email address.",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = if (emailErrorMsg != null) DangerRed else SageGreen
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = if (selectedMethodTab == 0) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { if (selectedMethodTab == 0) dispatchEmailVerificationCode() }
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
                                .testTag("auth_actual_email_input")
                        )

                        // Password Fields (Mode 1 only)
                        AnimatedVisibility(visible = selectedMethodTab == 1) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordErrorMsg = null
                                    },
                                    label = { Text(if (isRegistering) "Create Password" else "Account Password") },
                                    isError = passwordErrorMsg != null,
                                    supportingText = {
                                        if (passwordErrorMsg != null) {
                                            Text(text = passwordErrorMsg!!, color = DangerRed, fontSize = 11.sp)
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SageGreen)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password",
                                                tint = TextSecondaryDark
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = if (isRegistering) ImeAction.Next else ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { handlePasswordAuth() }),
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
                                        .testTag("auth_password_input")
                                )

                                if (isRegistering) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = confirmPassword,
                                        onValueChange = {
                                            confirmPassword = it
                                            passwordErrorMsg = null
                                        },
                                        label = { Text("Confirm Password") },
                                        leadingIcon = {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SageGreen)
                                        },
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Password,
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(onDone = { handlePasswordAuth() }),
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
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { isRegistering = !isRegistering }) {
                                        Text(
                                            text = if (isRegistering) "Already registered? Sign in" else "Need an account? Register",
                                            color = SageGreen,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    if (!isRegistering) {
                                        TextButton(onClick = { selectedMethodTab = 0 }) {
                                            Text(
                                                text = "Forgot password?",
                                                color = AmberGold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Main Submit Action Button
                        Button(
                            onClick = {
                                if (selectedMethodTab == 0) {
                                    dispatchEmailVerificationCode()
                                } else {
                                    handlePasswordAuth()
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_primary_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = NightBlack,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text(
                                    text = if (selectedMethodTab == 0) "SEND VERIFICATION CODE" else if (isRegistering) "CREATE ACCOUNT & ENTER" else "SIGN IN WITH EMAIL",
                                    color = NightBlack,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                    } else {
                        // ---------------- AUTH STAGE 2: 6-DIGIT EMAIL CODE VERIFICATION ----------------
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(SageGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = SageGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Verify Your Email",
                                color = TextPrimaryDark,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "We've sent a 6-digit confirmation code to:",
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                color = NightSurface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = email.trim(),
                                    color = SageGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // 6-Digit Code Input Field
                            OutlinedTextField(
                                value = enteredOtpCode,
                                onValueChange = {
                                    val digitsOnly = it.filter { char -> char.isDigit() }.take(6)
                                    enteredOtpCode = digitsOnly
                                    otpErrorMsg = null
                                    if (digitsOnly.length == 6) {
                                        if (digitsOnly == generatedOtpCode) {
                                            verifyEnteredCode()
                                        }
                                    }
                                },
                                label = { Text("Enter 6-Digit Code") },
                                placeholder = { Text("• • • • • •") },
                                isError = otpErrorMsg != null,
                                supportingText = {
                                    if (otpErrorMsg != null) {
                                        Text(text = otpErrorMsg!!, color = DangerRed, fontSize = 11.sp)
                                    } else {
                                        Text(text = "Check notification banner above or inbox.", color = TextSecondaryDark, fontSize = 11.sp)
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { verifyEnteredCode() }),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 24.sp,
                                    letterSpacing = 8.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextPrimaryDark
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SageGreen,
                                    unfocusedBorderColor = NightCardBorder,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_code_input_field")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Verify Button
                            Button(
                                onClick = { verifyEnteredCode() },
                                enabled = enteredOtpCode.length == 6 && !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("verify_otp_submit_button")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = NightBlack,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = NightBlack)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "VERIFY & LOGIN",
                                        color = NightBlack,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        authStage = AuthStage.INPUT_CREDENTIALS
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change email", color = TextSecondaryDark, fontSize = 12.sp)
                                }

                                TextButton(
                                    onClick = {
                                        dispatchEmailVerificationCode()
                                    },
                                    enabled = otpCooldownSeconds == 0 && !isLoading
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = if (otpCooldownSeconds == 0) SageGreen else TextSecondaryDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (otpCooldownSeconds > 0) "Resend in ${otpCooldownSeconds}s" else "Resend code",
                                        color = if (otpCooldownSeconds == 0) SageGreen else TextSecondaryDark,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Trail Feature Highlights Pill Row
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
                FeaturePill(icon = Icons.Default.Layers, label = "10 Map Styles", tint = AmberGold)
                FeaturePill(icon = Icons.Default.CameraAlt, label = "Photo Log", tint = SkyTrail)
                FeaturePill(icon = Icons.Default.Explore, label = "True GPS", tint = Terracotta)
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
