package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun getDarkColorScheme(accent: AppAccent) = darkColorScheme(
    primary = accent.primary,
    onPrimary = Color.White,
    primaryContainer = accent.light.copy(alpha = 0.2f),
    onPrimaryContainer = accent.light,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = NeonCyanLight.copy(alpha = 0.2f),
    onSecondaryContainer = NeonCyanLight,
    tertiary = NeonPink,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private fun getAmoledColorScheme(accent: AppAccent) = darkColorScheme(
    primary = accent.light,
    onPrimary = Color.Black,
    secondary = NeonCyanLight,
    onSecondary = Color.Black,
    tertiary = NeonPink,
    background = AmoledBackground,
    onBackground = Color.White,
    surface = AmoledSurface,
    onSurface = Color.White,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private fun getLightColorScheme(accent: AppAccent) = lightColorScheme(
    primary = accent.primary,
    onPrimary = Color.White,
    secondary = NeonCyan,
    onSecondary = Color.Black,
    tertiary = NeonPink,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun UMusicTheme(
    themePreference: String = "Dark",
    accentPreference: String = "Violet",
    content: @Composable () -> Unit
) {
    val accent = AppAccent.fromName(accentPreference)
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themePreference) {
        "AMOLED" -> getAmoledColorScheme(accent)
        "Light" -> getLightColorScheme(accent)
        "Auto" -> if (isSystemDark) getDarkColorScheme(accent) else getLightColorScheme(accent)
        else -> getDarkColorScheme(accent)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = (themePreference == "Light")
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = (themePreference == "Light")
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    UMusicTheme(themePreference = if (darkTheme) "Dark" else "Light", content = content)
}
