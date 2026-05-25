package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val MinimalistColorScheme = lightColorScheme(
    primary = MinimalistPrimary,
    secondary = MinimalistSecondary,
    tertiary = MinimalistSecondaryText,
    background = MinimalistBg,
    surface = MinimalistCardNormal,
    onPrimary = MinimalistCardNormal, // white text on primary
    onSecondary = MinimalistSecondaryText, // deep red on peach background
    onTertiary = MinimalistSecondaryText,
    onBackground = MinimalistText,
    onSurface = MinimalistText,
    surfaceVariant = MinimalistCardHighlight,
    onSurfaceVariant = MinimalistMutedText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force false for the clean light minimal look
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MinimalistColorScheme,
        typography = Typography,
        content = content
    )
}
