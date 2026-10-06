package com.example.csievent.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LocalCsiColors = staticCompositionLocalOf { MidnightGold }

/** Access to the app's colour tokens: `CsiTheme.colors.accent`. */
object CsiTheme {
    val colors: CsiColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCsiColors.current
}

/**
 * The root theme. [darkTheme] picks Midnight Gold (dark) or Ledger (light).
 * Material components (dialogs, text fields, snackbars) get a matching colour
 * scheme so they blend in without per-screen styling.
 */
@Composable
fun CSIEventTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val c = if (darkTheme) MidnightGold else Ledger

    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = c.accent, onPrimary = c.onAccent,
            primaryContainer = c.highlightContainer, onPrimaryContainer = c.onHighlightContainer,
            secondary = c.highlight, onSecondary = c.onAccent,
            secondaryContainer = c.raised, onSecondaryContainer = c.text,
            tertiary = c.success, onTertiary = c.background,
            background = c.background, onBackground = c.text,
            surface = c.surface, onSurface = c.text,
            surfaceVariant = c.raised, onSurfaceVariant = c.textMuted,
            surfaceTint = c.surface,
            inverseSurface = c.text, inverseOnSurface = c.background, inversePrimary = c.accent,
            error = c.danger, onError = c.background,
            errorContainer = c.dangerContainer, onErrorContainer = c.danger,
            outline = c.line, outlineVariant = c.line, scrim = c.background,
            surfaceBright = c.raised, surfaceDim = c.background,
            surfaceContainerLowest = c.background, surfaceContainerLow = c.surface,
            surfaceContainer = c.surface, surfaceContainerHigh = c.raised,
            surfaceContainerHighest = c.raised
        )
    } else {
        lightColorScheme(
            primary = c.accent, onPrimary = c.onAccent,
            primaryContainer = c.highlightContainer, onPrimaryContainer = c.onHighlightContainer,
            secondary = c.highlight, onSecondary = c.surface,
            secondaryContainer = c.raised, onSecondaryContainer = c.text,
            tertiary = c.success, onTertiary = c.surface,
            background = c.background, onBackground = c.text,
            surface = c.surface, onSurface = c.text,
            surfaceVariant = c.raised, onSurfaceVariant = c.textMuted,
            surfaceTint = c.surface,
            inverseSurface = c.text, inverseOnSurface = c.surface, inversePrimary = c.highlight,
            error = c.danger, onError = c.surface,
            errorContainer = c.dangerContainer, onErrorContainer = c.danger,
            outline = c.line, outlineVariant = c.line, scrim = c.text,
            surfaceBright = c.surface, surfaceDim = c.raised,
            surfaceContainerLowest = c.surface, surfaceContainerLow = c.surface,
            surfaceContainer = c.surface, surfaceContainerHigh = c.surface,
            surfaceContainerHighest = c.raised
        )
    }

    // Status/navigation bar icons: light icons on dark screens and vice versa
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalCsiColors provides c) {
        MaterialTheme(
            colorScheme = scheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
