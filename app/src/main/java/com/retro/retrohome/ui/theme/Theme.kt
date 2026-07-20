package com.retro.retrohome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = RetroWhite,
    onPrimary = RetroBlack,
    background = RetroBlack,
    onBackground = RetroWhite,
    surface = RetroBlack,
    onSurface = RetroWhite
)

private val LightColorScheme = lightColorScheme(
    primary = RetroBlack,
    onPrimary = RetroWhite,
    background = RetroWhite,
    onBackground = RetroBlack,
    surface = RetroWhite,
    onSurface = RetroBlack
)

@Composable
fun RetroHomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}