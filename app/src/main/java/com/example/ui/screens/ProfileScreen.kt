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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AmberGold
import com.example.ui.theme.NightBlack
import com.example.ui.theme.NightCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.TrekViewModel

import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.OutlinedButton

@Composable
fun ProfileScreen(
    viewModel: TrekViewModel,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var name by remember(userProfile) { mutableStateOf(userProfile.name) }
    var ageText by remember(userProfile) { mutableStateOf(userProfile.age.toString()) }
    var weightText by remember(userProfile) { mutableStateOf(userProfile.weightKg.toInt().toString()) }
    var selectedGender by remember(userProfile) { mutableStateOf(userProfile.gender) }
    var useMetric by remember(userProfile) { mutableStateOf(userProfile.useMetric) }
    var savedFeedback by remember { mutableStateOf(false) }

    val genders = listOf("Male", "Female", "Other")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NightBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "ADVENTURER PROFILE",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Hiker Information",
                    color = TextPrimaryDark,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Used on your device for physical calories, pace estimates, and cloud sync.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Account & Avatar Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NightCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, com.example.ui.theme.NightCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(SageGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.take(1).uppercase().ifEmpty { "A" },
                                color = NightBlack,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name.ifEmpty { "Explorer" },
                                color = TextPrimaryDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userProfile.email,
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (userProfile.authProvider) {
                                    "Google" -> Color(0xFF4285F4).copy(alpha = 0.2f)
                                    "Email" -> SageGreen.copy(alpha = 0.2f)
                                    else -> AmberGold.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = when (userProfile.authProvider) {
                                        "Google" -> "✓ Google Account"
                                        "Email" -> "✓ Email Account"
                                        else -> "Offline Adventurer"
                                    },
                                    color = when (userProfile.authProvider) {
                                        "Google" -> Color(0xFF8AB4F8)
                                        "Email" -> SageGreen
                                        else -> AmberGold
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Member Since: ${userProfile.memberSince}",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )

                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                                onNavigateToLogin()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("profile_switch_account_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (userProfile.isLoggedIn) "Sign Out" else "Sign In",
                                color = AmberGold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Profile Form
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageGreen,
                        unfocusedBorderColor = NightCard,
                        focusedLabelColor = SageGreen,
                        unfocusedContainerColor = NightCard,
                        focusedContainerColor = NightCard,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) ageText = it },
                        label = { Text("Age (years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = NightCard,
                            focusedLabelColor = SageGreen,
                            unfocusedContainerColor = NightCard,
                            focusedContainerColor = NightCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_age_input")
                    )

                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) weightText = it },
                        label = { Text(if (useMetric) "Weight (kg)" else "Weight (lbs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SageGreen,
                            unfocusedBorderColor = NightCard,
                            focusedLabelColor = SageGreen,
                            unfocusedContainerColor = NightCard,
                            focusedContainerColor = NightCard,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_weight_input")
                    )
                }

                Text(
                    text = "GENDER",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    genders.forEach { g ->
                        FilterChip(
                            selected = selectedGender.equals(g, ignoreCase = true),
                            onClick = { selectedGender = g },
                            label = { Text(g, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreen,
                                selectedLabelColor = NightBlack,
                                containerColor = NightCard,
                                labelColor = TextSecondaryDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "MEASUREMENT SYSTEM",
                    color = TextSecondaryDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = useMetric,
                        onClick = { useMetric = true },
                        label = { Text("Metric (km, m, km/h)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGold,
                            selectedLabelColor = NightBlack,
                            containerColor = NightCard,
                            labelColor = TextSecondaryDark
                        )
                    )
                    FilterChip(
                        selected = !useMetric,
                        onClick = { useMetric = false },
                        label = { Text("Imperial (mi, ft, mph)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberGold,
                            selectedLabelColor = NightBlack,
                            containerColor = NightCard,
                            labelColor = TextSecondaryDark
                        )
                    )
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    val ageVal = ageText.toIntOrNull() ?: 28
                    val weightVal = weightText.toFloatOrNull() ?: 70f
                    val weightKg = if (useMetric) weightVal else weightVal * 0.453592f

                    viewModel.updateProfile(
                        userProfile.copy(
                            name = name.ifEmpty { "Explorer" },
                            age = ageVal,
                            weightKg = weightKg,
                            gender = selectedGender,
                            useMetric = useMetric
                        )
                    )
                    savedFeedback = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_save_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NightBlack
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (savedFeedback) "PROFILE SAVED!" else "UPDATE PROFILE",
                    color = NightBlack,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
