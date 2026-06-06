package com.anonymous.peep.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PeepDarkColorScheme = darkColorScheme(
    primary = PeepWhite,
    onPrimary = PeepBlack,
    primaryContainer = PeepWhite,
    onPrimaryContainer = PeepBlack,
    secondary = PeepSecondary,
    onSecondary = PeepBlack,
    tertiary = PeepMuted,
    onTertiary = PeepWhite,
    background = PeepBlack,
    onBackground = PeepWhite,
    surface = PeepSurface,
    onSurface = PeepWhite,
    surfaceVariant = PeepSurfaceBorder,
    onSurfaceVariant = PeepSecondary,
    error = PeepError,
    onError = PeepWhite,
    outline = PeepSurfaceBorder,
    outlineVariant = PeepMuted,
    surfaceContainerHigh = PeepSurface,
    surfaceContainerHighest = PeepSurfaceBorder,
)

@Composable
fun PeepTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PeepDarkColorScheme,
        typography = PeepTypography,
        content = content,
    )
}
