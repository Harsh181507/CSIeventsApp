package com.example.csievent.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/** Cards and list rows. */
val CardShape = RoundedCornerShape(16.dp)

/** Buttons and inputs. */
val ControlShape = RoundedCornerShape(14.dp)

/** Chips and pills. */
val PillShape = RoundedCornerShape(percent = 50)
