package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LafangaDarkColorScheme = darkColorScheme(
    primary = SaffronGold,
    onPrimary = ObsidianVoid,
    primaryContainer = SaffronContainer,
    onPrimaryContainer = SaffronSoft,
    secondary = ElectricCyan,
    onSecondary = ObsidianVoid,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = ElectricCyan,
    tertiary = CoralPulse,
    onTertiary = Color.White,
    tertiaryContainer = CoralContainer,
    onTertiaryContainer = CoralPulse,
    background = ObsidianVoid,
    onBackground = SnowText,
    surface = MidnightSurface,
    onSurface = SnowText,
    surfaceVariant = SlateContainer,
    onSurfaceVariant = MutedSlate,
    surfaceContainer = SlateContainer,
    surfaceContainerHigh = ElevatedContainer,
    outline = SubtleBorder,
    outlineVariant = SubtleBorder,
    error = CoralPulse,
    onError = Color.White,
    errorContainer = CoralContainer,
    onErrorContainer = CoralPulse
)

@Composable
fun LafangaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = LafangaDarkColorScheme,
        typography = Typography,
        content = content
    )
}
