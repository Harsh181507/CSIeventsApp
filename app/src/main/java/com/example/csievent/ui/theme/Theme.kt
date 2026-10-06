package com.example.csievent.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(

    // Primary — main interactive elements, buttons, FAB
    primary              = Indigo50,
    onPrimary            = White,
    primaryContainer     = Indigo90,
    onPrimaryContainer   = Indigo10,

    // Secondary — chips, secondary buttons, filter states
    secondary            = Violet40,
    onSecondary          = White,
    secondaryContainer   = Violet90,
    onSecondaryContainer = Violet40,

    // Tertiary — judge role, cyan accents
    tertiary             = Cyan40,
    onTertiary           = White,
    tertiaryContainer    = Cyan90,
    onTertiaryContainer  = Cyan40,

    // Background & Surface
    background           = Gray5,
    onBackground         = Gray90,
    surface              = White,
    onSurface            = Gray90,
    surfaceVariant       = Gray10,
    onSurfaceVariant     = Gray60,

    // Outline
    outline              = Gray20,
    outlineVariant       = Gray15,

    // Error
    error                = Red40,
    onError              = White,
    errorContainer       = Red90,
    onErrorContainer     = Red40,

    // Inverse
    inverseSurface       = Gray80,
    inverseOnSurface     = Gray10,
    inversePrimary       = Indigo80,

    // Scrim
    scrim                = Black,
)

private val DarkColorScheme = darkColorScheme(

    // Primary
    primary              = Indigo80,
    onPrimary            = Indigo20,
    primaryContainer     = Indigo30,
    onPrimaryContainer   = Indigo90,

    // Secondary
    secondary            = Violet80,
    onSecondary          = Violet40,
    secondaryContainer   = Color(0xFF4C1D95),
    onSecondaryContainer = Violet90,

    // Tertiary
    tertiary             = Cyan80,
    onTertiary           = Cyan40,
    tertiaryContainer    = Color(0xFF164E63),
    onTertiaryContainer  = Cyan90,

    // Background & Surface
    background           = Gray95,
    onBackground         = Gray10,
    surface              = Gray90,
    onSurface            = Gray10,
    surfaceVariant       = Gray80,
    onSurfaceVariant     = Gray30,

    // Outline
    outline              = Gray60,
    outlineVariant       = Gray70,

    // Error
    error                = Red80,
    onError              = Red40,
    errorContainer       = Color(0xFF7F1D1D),
    onErrorContainer     = Red80,

    // Inverse
    inverseSurface       = Gray10,
    inverseOnSurface     = Gray90,
    inversePrimary       = Indigo50,

    // Scrim
    scrim                = Black,
)

// =============================================================================
// THEME COMPOSABLE
// =============================================================================

/**
 * The root theme for CSI Events app.
 *
 * - Always uses our brand colors (dynamicColor disabled)
 * - Supports light and dark mode
 * - Sets status bar color to match the app background
 *
 * File: app/src/main/java/com/example/csievent/ui/theme/Theme.kt
 */
@Composable
fun CSIEventTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Set status bar color to match app background
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Every screen has a dark background (edge-to-edge, set up in
            // MainActivity), so system bar icons are always light
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        shapes      = AppShapes,
        content     = content
    )
}