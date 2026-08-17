package com.checklisted.app.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
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

/**
 * A comb cell: point up, flat sides.
 *
 * Stretched to whatever box it is given rather than kept regular, so it can take the
 * place of a rounded square without anything around it having to move. Used where the
 * shape is the app's signature and nothing depends on it tessellating — the checkbox,
 * the empty state, the launcher icon. Deliberately **not** the heatmap: real comb
 * needs staggered rows to close up, and staggering that grid would cost the straight
 * weekday rows and flush week columns it took two rounds of device bugs to get right.
 */
val NeoCombCell: Shape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    moveTo(w * 0.5f, 0f)
    lineTo(w, h * 0.25f)
    lineTo(w, h * 0.75f)
    lineTo(w * 0.5f, h)
    lineTo(0f, h * 0.75f)
    lineTo(0f, h * 0.25f)
    close()
}
