package com.example.financetracker.core.designsystem

import androidx.compose.ui.graphics.Color

object FintechColors {
    // Primary Brand - Royal Electric Blue
    val Primary = Color(0xFF1E60F2)
    val PrimaryDark = Color(0xFF0F4AD1)
    val PrimaryLight = Color(0xFFEAF1FF)
    val PrimaryContainer = Color(0xFFDCE8FE)

    // Surfaces & Backgrounds
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceSubtle = Color(0xFFF1F5F9)
    val BorderHairline = Color(0xFFE2E8F0)
    val BorderFocus = Color(0xFF1E60F2)

    // Background Gradient (Soft Top Blue Fade)
    val BackgroundGradientStart = Color(0xFF1E60F2) // Soft Electric Blue Tint
    val BackgroundGradientMiddle = Color(0xFFEBF2FE)
    val BackgroundGradientEnd = Color(0xFFF8FAFC)

    // Typography
    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF64748B)
    val TextMuted = Color(0xFF94A3B8)
    val TextInverse = Color(0xFFFFFFFF)

    // Semantic Status Colors (Clean & Professional)
    val InflowGreen = Color(0xFF10B981)
    val InflowGreenBg = Color(0xFFECFDF5)
    val OutflowRed = Color(0xFFEF4444)
    val OutflowRedBg = Color(0xFFFEF2F2)
    val WarningAmber = Color(0xFFF59E0B)
    val WarningAmberBg = Color(0xFFFFFBEB)
    val ColdWalletBlue = Color(0xFF0284C7)
    val ColdWalletBlueBg = Color(0xFFF0F9FF)

    fun topBackgroundBrush() = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(
            BackgroundGradientStart,
//            BackgroundGradientMiddle,
            BackgroundGradientEnd,
            BackgroundGradientEnd
        ),
        startY = 0f,
        endY = 2500f
    )
}
