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

@Composable
fun ProfileScreen(
    viewModel: TrekViewModel,
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
                    text = "Used exclusively on your device for accurate calorie and pace estimates.",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Avatar placeholder
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NightCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(SageGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = NightBlack,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = name.ifEmpty { "Explorer" },
                            color = TextPrimaryDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (useMetric) "${weightText} kg • Metric" else "${(weightText.toFloatOrNull() ?: 70f * 2.2f).toInt()} lbs • Imperial",
                            color = SageGreen,
                            fontSize = 13.sp
                        )
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
