package com.grillpoet.bily.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF18191C),
    onPrimary = Color.White,
    background = Color(0xFFF6F7F8),
    onBackground = Color(0xFF18191C),
    surface = Color.White,
    onSurface = Color(0xFF18191C),
    error = Color(0xFFD93F3F)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFEEEEEE),
    onPrimary = Color(0xFF0F0F10),
    background = Color(0xFF0F0F10),
    onBackground = Color(0xFFEEEEEE),
    surface = Color(0xFF1A1A1C),
    onSurface = Color(0xFFEEEEEE),
    error = Color(0xFFE26B6B)
)

private val AppTypography = Typography(
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal)
)

@Composable
fun BilyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}