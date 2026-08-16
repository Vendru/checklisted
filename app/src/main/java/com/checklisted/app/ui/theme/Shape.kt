package com.checklisted.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corners are rounded enough to stop shouting and not so far as to go soft.
 *
 * The previous 0–4.dp scale was what made every surface read as a printed block;
 * 10.dp keeps the structure while letting the app sit still.
 */
val NeoShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(14.dp),
    extraLarge = RoundedCornerShape(18.dp),
)
