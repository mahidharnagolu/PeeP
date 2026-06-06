package com.anonymous.peep.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PeepTypography = Typography(
    // Logo text
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 48.sp,
        letterSpacing = 1.sp,
        color = PeepWhite,
    ),
    // Screen header logo
    headlineLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 26.sp,
        letterSpacing = 0.5.sp,
        color = PeepWhite,
    ),
    // Username large
    headlineMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        color = PeepWhite,
    ),
    // Card title / name
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        color = PeepWhite,
    ),
    // Button text
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = PeepBlack,
    ),
    // Small labels
    labelMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = PeepBlack,
    ),
    // Body text
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = PeepWhite,
    ),
    // Secondary text
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = PeepSecondary,
    ),
    // Small meta text
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = PeepSecondary,
    ),
    // Tiny text
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = PeepMuted,
    ),
)
