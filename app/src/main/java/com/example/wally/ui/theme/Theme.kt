package com.example.wally.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme

import androidx.compose.runtime.Composable

private val WallyDarkColors = darkColorScheme(
    primary = Purple,
    onPrimary = BackgroundDark,

    secondary = PurpleDark,
    onSecondary = TextPrimary,

    primaryContainer = PurpleContainer,
    onPrimaryContainer = TextPrimary,

    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary
)

@Composable
fun WallyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WallyDarkColors,
        content = content
    )
}