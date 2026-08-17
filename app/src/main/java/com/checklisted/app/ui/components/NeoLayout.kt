package com.checklisted.app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Roughly seventy characters at this app's body size. */
private val ReadableWidth = 560.dp

/**
 * Caps a screen's content to a readable measure and centres it.
 *
 * Every screen here is one column, and one column with no cap runs the full width of
 * whatever it is given: in landscape that is 914.dp, which puts a goal's title and its
 * tag dot half a screen apart and stretches body text past 140 characters a line.
 *
 * Reports the **full** incoming width while measuring the content narrower, so a
 * background or a scrollbar chained before it still covers the whole screen and only
 * the content is inset. On any phone in portrait the cap never binds and nothing
 * about the layout changes.
 */
fun Modifier.readableWidth(max: Dp = ReadableWidth): Modifier = layout { measurable, constraints ->
    val capped = constraints.maxWidth.coerceAtMost(max.roundToPx())
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.minWidth.coerceAtMost(capped),
            maxWidth = capped,
        ),
    )
    layout(constraints.maxWidth, placeable.height) {
        placeable.place(x = (constraints.maxWidth - placeable.width) / 2, y = 0)
    }
}
