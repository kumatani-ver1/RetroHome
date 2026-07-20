package com.retro.retrohome.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RetroColorScheme = darkColorScheme(
    primary = RetroWhite,
    onPrimary = RetroBlack,
    background = RetroBlack,
    onBackground = RetroWhite,
    surface = RetroBlack,
    onSurface = RetroWhite
)

@Composable
fun RetroHomeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = Typography,
        content = content
    )
}