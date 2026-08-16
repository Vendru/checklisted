package com.checklisted.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** Fixed measurements the whole design system is built from. */
object NeoTokens {
    /** Every interactive component carries this border. */
    val BorderWidth = 3.dp

    /** Hard offset shadow: no blur, no spread, no Material elevation anywhere. */
    val ShadowOffset = 4.dp

    /** Thinner border for dense, non-interactive marks such as heatmap cells. */
    val HairlineBorder = 2.dp

    val MinTouchTarget = 48.dp
}

val LocalNeoColors = staticCompositionLocalOf { NeoLightColors }

/**
 * The app theme. Material 3 is present only as a substrate — the color scheme is
 * flattened so that any component reaching for a Material default still lands on
 * the neobrutalist palette instead of a tinted surface.
 */
@Composable
fun NeoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val neoColors = if (darkTheme) NeoDarkColors else NeoLightColors

    // Deliberately built from lightColorScheme in both themes: Material's dark
    // scheme applies elevation tints, which this design forbids.
    val colorScheme = lightColorScheme(
        primary = NeoYellow,
        onPrimary = neoColors.onAccent,
        secondary = NeoTeal,
        onSecondary = neoColors.onAccent,
        tertiary = NeoPink,
        onTertiary = neoColors.onAccent,
        background = neoColors.background,
        onBackground = neoColors.ink,
        surface = neoColors.surface,
        onSurface = neoColors.ink,
        surfaceVariant = neoColors.surfaceMuted,
        onSurfaceVariant = neoColors.ink,
        error = NeoOrange,
        onError = neoColors.onAccent,
        outline = neoColors.ink,
        outlineVariant = neoColors.ink,
    )

    CompositionLocalProvider(LocalNeoColors provides neoColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NeoTypography,
            shapes = NeoShapes,
            content = content,
        )
    }
}

/** Shorthand for the neobrutalist tokens Material has no slot for. */
object NeoTheme {
    val colors: NeoColors
        @Composable @ReadOnlyComposable
        get() = LocalNeoColors.current
}
