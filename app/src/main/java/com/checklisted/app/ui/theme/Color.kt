package com.checklisted.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Saturated accents are shared by both themes: they carry enough contrast
// against the cream ground and against the dark ground alike.
val NeoYellow = Color(0xFFFFD23F)
val NeoPink = Color(0xFFFF6B9D)
val NeoTeal = Color(0xFF4ECDC4)
val NeoOrange = Color(0xFFFF7A3D)
val NeoPurple = Color(0xFFA78BFA)

val NeoBlack = Color(0xFF000000)
val NeoWhite = Color(0xFFFFFFFF)
val NeoCream = Color(0xFFFAF3E0)

// Dark theme ground and ink. Borders and solid shadows flip to bone so the
// neobrutalist outline stays visible; pure black on black would vanish.
val NeoBone = Color(0xFFF2ECDC)
val NeoNight = Color(0xFF14120F)
val NeoNightSurface = Color(0xFF221F1A)

// Disabled fills. Flat, clearly "off", and still high contrast against the ink —
// fading the whole component instead would produce exactly the low-contrast grey
// the design forbids.
val NeoStoneLight = Color(0xFFDDD6C4)
val NeoStoneDark = Color(0xFF3A352D)

/**
 * Accent slots a goal can be tagged with. Persisted by [name], so entries must
 * keep their identifiers stable across releases.
 */
enum class NeoAccent(val color: Color) {
    YELLOW(NeoYellow),
    PINK(NeoPink),
    TEAL(NeoTeal),
    ORANGE(NeoOrange),
    PURPLE(NeoPurple),
    ;

    companion object {
        val Default = YELLOW

        /** Resolves a persisted tag, falling back to [Default] for unknown values. */
        fun fromTag(tag: String?): NeoAccent = entries.firstOrNull { it.name == tag } ?: Default
    }
}

/**
 * Design tokens that Material 3's [androidx.compose.material3.ColorScheme] has no
 * slot for: the ink used for both text and the 3.dp borders, and the color of the
 * hard offset shadow.
 */
@Immutable
data class NeoColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val surfaceDisabled: Color,
    val ink: Color,
    val onAccent: Color,
    val shadow: Color,
    val isDark: Boolean,
)

val NeoLightColors = NeoColors(
    background = NeoCream,
    surface = NeoWhite,
    surfaceMuted = NeoCream,
    surfaceDisabled = NeoStoneLight,
    ink = NeoBlack,
    onAccent = NeoBlack,
    shadow = NeoBlack,
    isDark = false,
)

val NeoDarkColors = NeoColors(
    background = NeoNight,
    surface = NeoNightSurface,
    surfaceMuted = NeoNight,
    surfaceDisabled = NeoStoneDark,
    ink = NeoBone,
    onAccent = NeoBlack,
    shadow = NeoBone,
    isDark = true,
)
