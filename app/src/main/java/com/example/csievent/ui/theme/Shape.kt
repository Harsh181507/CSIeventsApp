package com.example.csievent.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// =============================================================================
// CSI EVENTS — SHAPES
// =============================================================================
//
// Generous rounding gives the app a modern, friendly feel.
// Consistent shape usage across all components.
//
// Reference — Material3 shapes:
// https://m3.material.io/styles/shape/overview
// =============================================================================

val AppShapes = Shapes(

    // Extra small — chips, small badges, text fields
    extraSmall = RoundedCornerShape(4.dp),

    // Small — small cards, snackbars, menus
    small = RoundedCornerShape(8.dp),

    // Medium — cards, dialogs, sheets (most common)
    medium = RoundedCornerShape(12.dp),

    // Large — bottom sheets, side sheets
    large = RoundedCornerShape(16.dp),

    // Extra large — full-screen dialogs, large cards
    extraLarge = RoundedCornerShape(24.dp)
)

// Convenience shapes for specific use cases
val PillShape      = RoundedCornerShape(50)        // fully rounded pills for chips
val CardShape      = RoundedCornerShape(16.dp)     // standard card
val ButtonShape    = RoundedCornerShape(12.dp)     // buttons
val InputShape     = RoundedCornerShape(10.dp)     // text fields
val JoinCodeShape  = RoundedCornerShape(12.dp)     // join code display box
val BadgeShape     = RoundedCornerShape(6.dp)      // small status badges