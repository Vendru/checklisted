package com.checklisted.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Ground and ink. Warm rather than neutral — the cream carries over from the
// previous design and is the one thing about it worth keeping.
val NeoCream = Color(0xFFF7F2E9)
val NeoWhite = Color(0xFFFFFFFF)
val NeoInk = Color(0xFF241F1A)
val NeoInkSoft = Color(0xFF6B6259)
val NeoSand = Color(0xFFEDE5D6)
val NeoDust = Color(0xFFD8CDBA)

val NeoNight = Color(0xFF1A1714)
val NeoNightSurface = Color(0xFF241F1A)
val NeoBone = Color(0xFFF0EAE0)
val NeoBoneSoft = Color(0xFFA79C8E)
val NeoNightTrack = Color(0xFF2E2822)
val NeoNightShadow = Color(0xFF0E0C0A)

/**
 * Borders in the dark theme.
 *
 * Deliberately lighter than the surface it sits on would suggest: at #4A4238 the
 * edge measures 1.65:1 against the card, well under the 3:1 that WCAG asks of a
 * graphical object you need to see to understand the layout. The card's own fill
 * barely separates from the page, so the border is what draws the boundary.
 */
val NeoNightDivider = Color(0xFF786C5E)

/** The single action colour: buttons, checked boxes, the filled stat. */
val NeoTerracotta = Color(0xFFB83E1B)
val NeoEmber = Color(0xFFFF7A4D)

/**
 * The data ramp, deliberately a different hue from the action colour.
 *
 * The heatmap repeats its colour roughly eighty times on one screen. Painting it in
 * the action colour turned the detail screen into a wall of terracotta and made
 * "this is a button" and "this is a busy week" look like the same thing.
 */
val NeoSlate = Color(0xFF3F6B63)
val NeoSlateLight = Color(0xFF6FB3A6)

/**
 * Tags a goal carries. These are now small dots rather than fills, so they can stay
 * saturated: at 8.dp they read as identity, not as decoration.
 *
 * Persisted by [name] — entries must keep their identifiers stable across releases.
 */
enum class NeoAccent(val color: Color) {
    YELLOW(Color(0xFFE0A100)),
    PINK(Color(0xFFE0447C)),
    TEAL(Color(0xFF2FA89C)),
    ORANGE(Color(0xFFD1631F)),
    PURPLE(Color(0xFF7C5CD6)),
    ;

    companion object {
        val Default = TEAL

        /** Resolves a persisted tag, falling back to [Default] for unknown values. */
        fun fromTag(tag: String?): NeoAccent = entries.firstOrNull { it.name == tag } ?: Default
    }
}

/**
 * Design tokens Material 3's [androidx.compose.material3.ColorScheme] has no slot
 * for: the ink used for text, the border that draws every edge, the colour of the
 * offset shadow, and the two ends of the data ramp.
 */
@Immutable
data class NeoColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val surfaceDisabled: Color,
    val ink: Color,
    val inkSoft: Color,
    val divider: Color,
    val shadow: Color,
    val action: Color,
    val onAction: Color,
    val dataLow: Color,
    val dataHigh: Color,
    val isDark: Boolean,
)

val NeoLightColors = NeoColors(
    background = NeoCream,
    surface = NeoWhite,
    surfaceMuted = NeoSand,
    surfaceDisabled = NeoSand,
    ink = NeoInk,
    inkSoft = NeoInkSoft,
    divider = NeoInk,
    shadow = NeoDust,
    action = NeoTerracotta,
    onAction = NeoWhite,
    dataLow = NeoSand,
    dataHigh = NeoSlate,
    isDark = false,
)

val NeoDarkColors = NeoColors(
    background = NeoNight,
    surface = NeoNightSurface,
    surfaceMuted = NeoNightTrack,
    surfaceDisabled = NeoNightTrack,
    ink = NeoBone,
    inkSoft = NeoBoneSoft,
    divider = NeoNightDivider,
    shadow = NeoNightShadow,
    action = NeoEmber,
    onAction = Color(0xFF1A1005),
    dataLow = NeoNightTrack,
    dataHigh = NeoSlateLight,
    isDark = true,
)
