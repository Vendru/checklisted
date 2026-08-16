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
    /** Every interactive component carries this border. */
    val BorderWidth = 3.dp

    /** Hard offset shadow: no blur, no spread, no Material elevation anywhere. */
    val ShadowOffset = 4.dp

    /** Thinner border for dense, non-interactive marks such as heatmap cells. */
    val HairlineBorder = 2.dp

    val MinTouchTarget = 48.dp

    /** Applied to the entire component — fill, border and shadow — when disabled. */
    const val DISABLED_ALPHA = 0.45f
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
    primary = NeoYellow,
    onPrimary = onAccent,
    primaryContainer = NeoYellow,
    onPrimaryContainer = onAccent,
    inversePrimary = NeoYellow,
    secondary = NeoTeal,
    onSecondary = onAccent,
    secondaryContainer = NeoTeal,
    onSecondaryContainer = onAccent,
    tertiary = NeoPink,
    onTertiary = onAccent,
    tertiaryContainer = NeoPink,
    onTertiaryContainer = onAccent,
    background = background,
    onBackground = ink,
    surface = surface,
    onSurface = ink,
    surfaceVariant = surfaceMuted,
    onSurfaceVariant = ink,
    surfaceTint = Color.Transparent,
    inverseSurface = ink,
    inverseOnSurface = surface,
    error = NeoOrange,
    onError = onAccent,
    errorContainer = NeoOrange,
    onErrorContainer = onAccent,
    outline = ink,
    outlineVariant = ink,
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
