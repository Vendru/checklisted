package com.checklisted.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Wax and ink. The ground is beeswax rather than paper — warmer and yellower than
// the cream it replaces, which is most of what makes the app read as a hive before
// a single hexagon is drawn.
val NeoWax = Color(0xFFFBF3E2)
val NeoWhite = Color(0xFFFFFFFF)
val NeoInk = Color(0xFF1E1809)
val NeoInkSoft = Color(0xFF6E6047)
val NeoWaxDeep = Color(0xFFEFE3C8)
val NeoWaxShadow = Color(0xFFE0CBA0)

val NeoNight = Color(0xFF141009)
val NeoNightSurface = Color(0xFF1F1810)
val NeoBone = Color(0xFFF4EBD6)
val NeoBoneSoft = Color(0xFFA2917A)
val NeoNightTrack = Color(0xFF2A2216)
val NeoNightShadow = Color(0xFF0A0805)

/**
 * Borders in the dark theme.
 *
 * Deliberately lighter than the surface it sits on would suggest: a dim edge measures
 * well under the 3:1 that WCAG asks of a graphical object you need to see to
 * understand the layout. The card's own fill barely separates from the page, so the
 * border is what draws the boundary. This one is 3.54:1 against the card.
 */
val NeoNightDivider = Color(0xFF7E6E52)

/**
 * Honey: the single action colour, on buttons, checked boxes and the filled stat.
 *
 * Deep rather than the bright yellow a bee suggests, because this colour has to work
 * as a mark on a white card as well as a fill under a label. At full brightness it
 * measured 2:1 against the card — invisible as the empty state's tick or the progress
 * bar's fill. This one clears 3:1 there and 5.4:1 under ink.
 *
 * The label on it is **ink, not white**, which is the whole point: black on amber is
 * the one colour pairing everyone already reads as a bee.
 */
val NeoHoney = Color(0xFFC77F00)
val NeoHoneyGlow = Color(0xFFFFC233)

/**
 * Destruction. Crimson, and the one colour in the app that owes nothing to a hive.
 *
 * Everything else here is wax, honey or ink, all of them neighbours on the wheel. A
 * destructive red drawn from that family would read as another shade of honey; pushed
 * blue it reads as the exception it is, at 8.1:1 under white in the light theme and
 * 5.9:1 under ink in the dark one.
 */
val NeoCrimson = Color(0xFF9B1B30)
val NeoRose = Color(0xFFFF6B7A)

/** Text and marks laid on a bright fill in the dark theme. */
val NeoNightInk = Color(0xFF1A1305)

/**
 * The comb filling with honey: the top of the data ramp.
 *
 * Shares the honey hue on purpose now — a heatmap cell that fills in *is* a comb cell
 * filling up — but is held a clear value step darker than the action colour, 2.2:1
 * apart in the light theme. That gap is doing real work: painting the grid in exactly
 * the colour of buttons once made "this is tappable" and "this was a busy week" look
 * like the same thing.
 */
val NeoCombFull = Color(0xFF7A4E0C)
val NeoCombFullDark = Color(0xFFD2941F)

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
    val danger: Color,
    val onDanger: Color,
    val dataLow: Color,
    val dataHigh: Color,
    val isDark: Boolean,
)

val NeoLightColors = NeoColors(
    background = NeoWax,
    surface = NeoWhite,
    surfaceMuted = NeoWaxDeep,
    surfaceDisabled = NeoWaxDeep,
    ink = NeoInk,
    inkSoft = NeoInkSoft,
    divider = NeoInk,
    shadow = NeoWaxShadow,
    action = NeoHoney,
    onAction = NeoInk,
    danger = NeoCrimson,
    onDanger = NeoWhite,
    dataLow = NeoWaxDeep,
    dataHigh = NeoCombFull,
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
    action = NeoHoneyGlow,
    onAction = NeoNightInk,
    danger = NeoRose,
    onDanger = NeoNightInk,
    dataLow = NeoNightTrack,
    dataHigh = NeoCombFullDark,
    isDark = true,
)
