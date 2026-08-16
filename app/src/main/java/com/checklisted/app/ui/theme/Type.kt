package com.checklisted.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.unit.sp
import com.checklisted.app.R

/** Anton: a heavy condensed grotesque, bundled so the app never touches the network. */
val NeoDisplayFamily = FontFamily(Font(R.font.anton_regular, FontWeight.Normal))

/** Body copy leans on the platform grotesque at medium or heavier — never light. */
val NeoBodyFamily = FontFamily.SansSerif

private val Display = TextStyle(
    fontFamily = NeoDisplayFamily,
    fontWeight = FontWeight.Normal,
    letterSpacing = 0.sp,
)

private fun display(size: Int, lineHeight: Int, tracking: Double = 0.0) = Display.copy(
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
)

private fun body(size: Int, lineHeight: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = NeoBodyFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp,
)

val NeoTypography = Typography(
    displayLarge = display(size = 56, lineHeight = 56, tracking = -1.0),
    displayMedium = display(size = 44, lineHeight = 46, tracking = -0.5),
    displaySmall = display(size = 34, lineHeight = 38),
    headlineLarge = display(size = 30, lineHeight = 34),
    headlineMedium = display(size = 26, lineHeight = 30),
    headlineSmall = display(size = 22, lineHeight = 26),
    titleLarge = display(size = 20, lineHeight = 24, tracking = 0.5),
    titleMedium = body(size = 17, lineHeight = 22, weight = FontWeight.Bold, tracking = 0.2),
    titleSmall = body(size = 15, lineHeight = 20, weight = FontWeight.Bold, tracking = 0.2),
    bodyLarge = body(size = 17, lineHeight = 24, weight = FontWeight.Medium),
    bodyMedium = body(size = 15, lineHeight = 21, weight = FontWeight.Medium),
    bodySmall = body(size = 13, lineHeight = 18, weight = FontWeight.Medium),
    labelLarge = body(size = 15, lineHeight = 18, weight = FontWeight.Black, tracking = 1.0),
    labelMedium = body(size = 13, lineHeight = 16, weight = FontWeight.Black, tracking = 1.0),
    labelSmall = body(size = 11, lineHeight = 14, weight = FontWeight.Black, tracking = 1.2),
)

/**
 * Squeezes a style horizontally. Used for the oversized uppercase headings where
 * Anton alone is not tight enough for the layout.
 */
fun TextStyle.condensed(scale: Float = 0.94f): TextStyle =
    copy(textGeometricTransform = TextGeometricTransform(scaleX = scale))
