package com.checklisted.app.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.components.NeoNoIndication

/** Fixed measurements the whole design system is built from. */
object NeoTokens {
    /**
     * Every surface carries this border.
     *
     * Down from 3.dp: the edge still draws the shape, but stops being the loudest
     * thing on screen.
     */
    val BorderWidth = 1.5.dp

    /**
     * Offset shadow, still hard-edged and still not Material elevation.
     *
     * Shorter than before and painted in a warm tone from the palette rather than
     * black, so it reads as depth instead of as a printing artefact.
     */
    val ShadowOffset = 3.dp

    /** Thinner border for dense marks such as heatmap cells. */
    val HairlineBorder = 1.dp

    val MinTouchTarget = 48.dp

    /**
     * Applied to a disabled component's *content* only.
     *
     * The fill switches to `surfaceDisabled` and the border and shadow stay at full
     * strength, so a disabled control still reads as part of this design system.
     */
    const val DISABLED_CONTENT_ALPHA = 0.55f
}

val LocalNeoColors = staticCompositionLocalOf { NeoLightColors }

/**
 * The app theme.
 *
 * Material 3 is present only as a substrate. Two things have to be forced here or
 * they leak Material defaults into the design:
 *
 * - `LocalContentColor` is **not** provided by `MaterialTheme` — only by `Surface`,
 *   which this design system never uses. Left alone it stays `Color.Black`, so any
 *   `Text` without an explicit color would be invisible in the dark theme.
 * - `LocalIndication` still carries the ripple. Overriding it here means a plain
 *   `Modifier.clickable` anywhere in the tree is ripple-free by default, instead of
 *   the rule depending on every call site remembering to opt out.
 */
@Composable
fun NeoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val neoColors = if (darkTheme) NeoDarkColors else NeoLightColors

    CompositionLocalProvider(
        LocalNeoColors provides neoColors,
        LocalContentColor provides neoColors.ink,
        LocalIndication provides NeoNoIndication,
    ) {
        MaterialTheme(
            colorScheme = neoColors.toColorScheme(),
            typography = NeoTypography,
            shapes = NeoShapes,
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides NeoTypography.bodyLarge.copy(color = neoColors.ink),
                content = content,
            )
        }
    }
}

/**
 * Flattens the palette into every Material color slot.
 *
 * Built from [lightColorScheme] in both themes on purpose — Material's dark scheme
 * applies elevation tints, which this design forbids. That means **every** slot has
 * to be named explicitly: any left at its default keeps a light value in the dark
 * theme, which is how a dropdown ends up painting near-white behind bone text.
 * `surfaceTint` is pinned to transparent for the same reason — its default is
 * `primary`, which would wash every raised Material surface in yellow.
 */
private fun NeoColors.toColorScheme(): ColorScheme = lightColorScheme(
    primary = action,
    onPrimary = onAction,
    primaryContainer = action,
    onPrimaryContainer = onAction,
    inversePrimary = action,
    secondary = dataHigh,
    onSecondary = onAction,
    secondaryContainer = dataHigh,
    onSecondaryContainer = onAction,
    tertiary = action,
    onTertiary = onAction,
    tertiaryContainer = action,
    onTertiaryContainer = onAction,
    background = background,
    onBackground = ink,
    surface = surface,
    onSurface = ink,
    surfaceVariant = surfaceMuted,
    onSurfaceVariant = inkSoft,
    surfaceTint = Color.Transparent,
    inverseSurface = ink,
    inverseOnSurface = surface,
    error = danger,
    onError = onDanger,
    errorContainer = danger,
    onErrorContainer = onDanger,
    outline = divider,
    outlineVariant = divider,
    scrim = Color.Black,
    surfaceBright = surface,
    surfaceContainer = surface,
    surfaceContainerHigh = surface,
    surfaceContainerHighest = surface,
    surfaceContainerLow = surface,
    surfaceContainerLowest = surface,
    surfaceDim = background,
)

/** Shorthand for the neobrutalist tokens Material has no slot for. */
object NeoTheme {
    val colors: NeoColors
        @Composable @ReadOnlyComposable
        get() = LocalNeoColors.current
}
