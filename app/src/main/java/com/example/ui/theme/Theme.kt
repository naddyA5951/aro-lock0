package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen,
    onPrimary = NightBlack,
    primaryContainer = PineDeep,
    onPrimaryContainer = MintLight,
    secondary = AmberGold,
    onSecondary = NightBlack,
    secondaryContainer = Color(0xFF4A3800),
    onSecondaryContainer = AmberGold,
    tertiary = OchreWarm,
    onTertiary = NightBlack,
    background = NightBlack,
    onBackground = TextPrimaryDark,
    surface = NightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NightCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = NightCardBorder,
    error = DangerRed
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = MintLight,
    onPrimaryContainer = PineDeep,
    secondary = Terracotta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBCF),
    onSecondaryContainer = Color(0xFF380E04),
    tertiary = AmberGold,
    onTertiary = NightBlack,
    background = MistWhite,
    onBackground = TextPrimaryLight,
    surface = MistSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = MistCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = MistCardBorder,
    error = DangerRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek outdoor dark theme
    dynamicColor: Boolean = false, // Keep authentic outdoor adventure branding
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
