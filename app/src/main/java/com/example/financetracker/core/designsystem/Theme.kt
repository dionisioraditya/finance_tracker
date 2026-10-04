package com.example.financetracker.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = FintechColors.Primary,
    onPrimary = FintechColors.TextInverse,
    primaryContainer = FintechColors.PrimaryLight,
    onPrimaryContainer = FintechColors.PrimaryDark,
    background = FintechColors.Background,
    onBackground = FintechColors.TextPrimary,
    surface = FintechColors.Surface,
    onSurface = FintechColors.TextPrimary,
    surfaceVariant = FintechColors.SurfaceSubtle,
    onSurfaceVariant = FintechColors.TextSecondary,
    outline = FintechColors.BorderHairline,
)

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Dedicated sleek light fintech theme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FintechTypography,
        content = content
    )
}
