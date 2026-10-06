package com.example.csievent.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colour tokens for the app. Screens use these (via [CsiTheme.colors]) instead
 * of hard-coded colours, so dark and light mode stay consistent.
 *
 * Dark  = "Midnight Gold": near-black surfaces, gold actions — matches the CSI badge.
 * Light = "Ledger": warm ivory surfaces, ink actions, gold highlights.
 */
@Immutable
data class CsiColors(
    val isDark: Boolean,

    // Surfaces
    val background: Color,
    val surface: Color,
    val raised: Color,
    val line: Color,

    // Text
    val text: Color,
    val textMuted: Color,
    val textSubtle: Color,

    // Primary action (buttons, selected states)
    val accent: Color,
    val onAccent: Color,

    // Gold highlight (codes, scores, badges)
    val highlight: Color,
    val highlightContainer: Color,
    val onHighlightContainer: Color,
    val highlightBorder: Color,

    // Status
    val success: Color,
    val successContainer: Color,
    val danger: Color,
    val dangerContainer: Color,

    // Results podium
    val winnerContainer: Color,
    val onWinnerContainer: Color,
    val silver: Color,
    val bronze: Color
)

val MidnightGold = CsiColors(
    isDark = true,
    background = Color(0xFF0B0B0D),
    surface = Color(0xFF151518),
    raised = Color(0xFF1C1C21),
    line = Color(0xFF2A2A31),
    text = Color(0xFFF3F1EA),
    textMuted = Color(0xFFA8A498),
    textSubtle = Color(0xFF77746B),
    accent = Color(0xFFE3B04B),
    onAccent = Color(0xFF15120A),
    highlight = Color(0xFFE3B04B),
    highlightContainer = Color(0xFF2A2414),
    onHighlightContainer = Color(0xFFE3B04B),
    highlightBorder = Color(0xFF5C4A22),
    success = Color(0xFF7FD9A6),
    successContainer = Color(0xFF12301F),
    danger = Color(0xFFF28B82),
    dangerContainer = Color(0xFF3A1614),
    winnerContainer = Color(0xFFE3B04B),
    onWinnerContainer = Color(0xFF15120A),
    silver = Color(0xFFCFCBC0),
    bronze = Color(0xFFC99A6B)
)

val Ledger = CsiColors(
    isDark = false,
    background = Color(0xFFF6F4EF),
    surface = Color(0xFFFFFFFF),
    raised = Color(0xFFEFEBE3),
    line = Color(0xFFE4DED2),
    text = Color(0xFF17140F),
    textMuted = Color(0xFF5F584D),
    textSubtle = Color(0xFF8A8276),
    accent = Color(0xFF17140F),
    onAccent = Color(0xFFFFFFFF),
    highlight = Color(0xFF8A5D0C),
    highlightContainer = Color(0xFFF1E6CE),
    onHighlightContainer = Color(0xFF6B4708),
    highlightBorder = Color(0xFFD9C493),
    success = Color(0xFF1E5C3D),
    successContainer = Color(0xFFE1F0E6),
    danger = Color(0xFFA33A1E),
    dangerContainer = Color(0xFFFBE4DD),
    winnerContainer = Color(0xFF17140F),
    onWinnerContainer = Color(0xFFE2B65A),
    silver = Color(0xFF6E6A62),
    bronze = Color(0xFF9A5B2A)
)
