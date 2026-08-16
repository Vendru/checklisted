package com.checklisted.app.ui.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.checklisted.app.R
import org.junit.Rule
import org.junit.Test

/**
 * Throwaway comparison of the four restyle directions.
 *
 * Nothing here touches the app: each direction is described by a local [StyleSpec]
 * and rendered through the same mock screen, so the differences on screen are the
 * differences being proposed and nothing else. Delete this file once a direction is
 * picked.
 */
class StyleExplorationScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_6,
        renderingMode = SessionParams.RenderingMode.SHRINK,
        showSystemUi = false,
    )

    private enum class ShadowStyle { NONE, SOFT, HARD }

    private data class StyleSpec(
        val bg: Color,
        val surface: Color,
        val text: Color,
        val secondary: Color,
        val accent: Color,
        val onAccent: Color,
        val divider: Color,
        val track: Color,
        val radius: Dp,
        val border: Dp,
        val shadow: ShadowStyle,
        val shadowColor: Color = Color.Black,
        val shadowOffset: Dp = 4.dp,
        val display: FontFamily,
        val body: FontFamily,
        val displaySize: Int,
        val uppercase: Boolean,
        val rowPadding: Dp,
        val gap: Dp,
        val tagAsDot: Boolean,
    )

    private val anton = FontFamily(Font(R.font.anton_regular, FontWeight.Normal))

    private val neo = StyleSpec(
        bg = Color(0xFFFAF3E0),
        surface = Color(0xFFFFFFFF),
        text = Color(0xFF000000),
        secondary = Color(0xFF000000),
        accent = Color(0xFFFFD23F),
        onAccent = Color(0xFF000000),
        divider = Color(0xFF000000),
        track = Color(0xFFFFFFFF),
        radius = 2.dp,
        border = 3.dp,
        shadow = ShadowStyle.HARD,
        display = anton,
        body = FontFamily.SansSerif,
        displaySize = 34,
        uppercase = true,
        rowPadding = 12.dp,
        gap = 10.dp,
        tagAsDot = false,
    )

    private val paperLight = StyleSpec(
        bg = Color(0xFFFBFAF7),
        surface = Color(0xFFFFFFFF),
        text = Color(0xFF1C1B19),
        secondary = Color(0xFF5E5B54),
        accent = Color(0xFF2F6F5B),
        onAccent = Color(0xFFFFFFFF),
        divider = Color(0xFFE7E4DD),
        track = Color(0xFFEDEBE5),
        radius = 12.dp,
        border = 0.dp,
        shadow = ShadowStyle.SOFT,
        display = FontFamily.SansSerif,
        body = FontFamily.SansSerif,
        displaySize = 28,
        uppercase = false,
        rowPadding = 16.dp,
        gap = 10.dp,
        tagAsDot = true,
    )

    private val paperDark = paperLight.copy(
        bg = Color(0xFF131316),
        surface = Color(0xFF1B1B1F),
        text = Color(0xFFEDECE8),
        secondary = Color(0xFFA5A29B),
        accent = Color(0xFF6FBFA3),
        onAccent = Color(0xFF0F1412),
        divider = Color(0xFF2A2A2F),
        track = Color(0xFF26262B),
        shadow = ShadowStyle.NONE,
    )

    private val editorial = StyleSpec(
        bg = Color(0xFFFFFDF9),
        surface = Color(0xFFFFFDF9),
        text = Color(0xFF14110E),
        secondary = Color(0xFF665C51),
        accent = Color(0xFFA33F1B),
        onAccent = Color(0xFFFFFFFF),
        divider = Color(0xFFE3DCD1),
        track = Color(0xFFEFE8DC),
        radius = 0.dp,
        border = 0.dp,
        shadow = ShadowStyle.NONE,
        display = FontFamily.Serif,
        body = FontFamily.SansSerif,
        displaySize = 34,
        uppercase = false,
        rowPadding = 18.dp,
        gap = 14.dp,
        tagAsDot = true,
    )

    private val material = StyleSpec(
        bg = Color(0xFFFBF8FD),
        surface = Color(0xFFFFFFFF),
        text = Color(0xFF1D1B20),
        secondary = Color(0xFF49454F),
        accent = Color(0xFF65558F),
        onAccent = Color(0xFFFFFFFF),
        divider = Color(0xFFE8DEF8),
        track = Color(0xFFE8DEF8),
        radius = 20.dp,
        border = 0.dp,
        shadow = ShadowStyle.SOFT,
        display = FontFamily.SansSerif,
        body = FontFamily.SansSerif,
        displaySize = 30,
        uppercase = false,
        rowPadding = 16.dp,
        gap = 8.dp,
        tagAsDot = true,
    )

    private val focusDark = StyleSpec(
        bg = Color(0xFF0A0A0B),
        surface = Color(0xFF141416),
        text = Color(0xFFF4F4F5),
        secondary = Color(0xFF9B9BA1),
        accent = Color(0xFF7DD3A0),
        onAccent = Color(0xFF06120C),
        divider = Color(0xFF232326),
        track = Color(0xFF232326),
        radius = 10.dp,
        border = 1.dp,
        shadow = ShadowStyle.NONE,
        display = FontFamily.SansSerif,
        body = FontFamily.SansSerif,
        displaySize = 32,
        uppercase = false,
        rowPadding = 14.dp,
        gap = 8.dp,
        tagAsDot = true,
    )

    // The three directions in the middle band: keep the structural personality,
    // turn every dial down instead of turning the whole thing off.

    private val tamedLight = StyleSpec(
        bg = Color(0xFFF7F2E9),
        surface = Color(0xFFFFFFFF),
        text = Color(0xFF241F1A),
        secondary = Color(0xFF6B6259),
        accent = Color(0xFFB83E1B),
        onAccent = Color(0xFFFFFFFF),
        divider = Color(0xFF241F1A),
        track = Color(0xFFEDE5D6),
        radius = 10.dp,
        border = 1.5.dp,
        shadow = ShadowStyle.HARD,
        shadowColor = Color(0xFFD8CDBA),
        shadowOffset = 3.dp,
        display = anton,
        body = FontFamily.SansSerif,
        displaySize = 32,
        uppercase = false,
        rowPadding = 14.dp,
        gap = 10.dp,
        tagAsDot = true,
    )

    private val tamedDark = tamedLight.copy(
        bg = Color(0xFF1A1714),
        surface = Color(0xFF241F1A),
        text = Color(0xFFF0EAE0),
        secondary = Color(0xFFA79C8E),
        accent = Color(0xFFFF7A4D),
        onAccent = Color(0xFF1A1005),
        divider = Color(0xFF4A4238),
        track = Color(0xFF2E2822),
        shadowColor = Color(0xFF0E0C0A),
    )

    private val editorialSurface = StyleSpec(
        bg = Color(0xFFFBF6EE),
        surface = Color(0xFFFFFFFF),
        text = Color(0xFF1A1512),
        secondary = Color(0xFF6E6155),
        accent = Color(0xFF8C3A2B),
        onAccent = Color(0xFFFFFFFF),
        divider = Color(0xFFEADFD0),
        track = Color(0xFFEFE6D8),
        radius = 6.dp,
        border = 1.dp,
        shadow = ShadowStyle.NONE,
        display = FontFamily.Serif,
        body = FontFamily.SansSerif,
        displaySize = 34,
        uppercase = false,
        rowPadding = 16.dp,
        gap = 12.dp,
        tagAsDot = true,
    )

    private val expressive = StyleSpec(
        bg = Color(0xFFF5F1FB),
        surface = Color(0xFFFFFFFF),
        text = Color(0xFF1D1926),
        secondary = Color(0xFF5A5266),
        accent = Color(0xFF5B3FD6),
        onAccent = Color(0xFFFFFFFF),
        divider = Color(0xFFE9E0FB),
        track = Color(0xFFE9E0FB),
        radius = 24.dp,
        border = 0.dp,
        shadow = ShadowStyle.SOFT,
        display = FontFamily.SansSerif,
        body = FontFamily.SansSerif,
        displaySize = 34,
        uppercase = false,
        rowPadding = 18.dp,
        gap = 10.dp,
        tagAsDot = true,
    )

    private fun render(name: String, spec: StyleSpec) {
        paparazzi.snapshot(name = name) { SampleToday(spec) }
    }

    @Test fun current() = render("0-atual-neobrutalista", neo)

    @Test fun paperCalmLight() = render("a-papel-calmo-claro", paperLight)

    @Test fun paperCalmDark() = render("a-papel-calmo-escuro", paperDark)

    @Test fun editorialType() = render("b-editorial", editorial)

    @Test fun materialTonal() = render("c-material-tonal", material)

    @Test fun focusDarkMode() = render("d-foco-escuro", focusDark)

    @Test fun tamedBrutalLight() = render("e-brutalismo-domado-claro", tamedLight)

    @Test fun tamedBrutalDark() = render("e-brutalismo-domado-escuro", tamedDark)

    @Test fun editorialWithSurface() = render("f-editorial-com-superficie", editorialSurface)

    @Test fun expressiveTonal() = render("g-tonal-expressivo", expressive)

    // region the mock screen

    @Composable
    private fun SampleToday(spec: StyleSpec) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(spec.bg)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(spec.gap),
        ) {
            Text(
                text = spec.label("Hoje"),
                style = TextStyle(
                    fontFamily = spec.display,
                    fontSize = spec.displaySize.sp,
                    fontWeight = if (spec.display == FontFamily.SansSerif) FontWeight.Bold else FontWeight.Normal,
                    color = spec.text,
                ),
            )
            Text(
                text = "domingo, 16 de agosto",
                style = TextStyle(fontFamily = spec.body, fontSize = 14.sp, color = spec.secondary),
                modifier = Modifier.padding(bottom = spec.gap),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = spec.label("Diárias"),
                    style = TextStyle(
                        fontFamily = spec.display,
                        fontSize = 19.sp,
                        fontWeight = if (spec.display == FontFamily.SansSerif) FontWeight.Bold else FontWeight.Normal,
                        color = spec.text,
                    ),
                )
                Text(
                    text = "2/3",
                    style = TextStyle(fontFamily = spec.body, fontSize = 15.sp, color = spec.secondary),
                )
            }

            Progress(spec, fraction = 2f / 3f)

            GoalRow(spec, "Beber 2L de água", Color(0xFF4ECDC4), done = true)
            GoalRow(spec, "Ler 20 páginas", Color(0xFFFF6B9D), done = false, note = "Antes de dormir, sem tela.")
            GoalRow(spec, "Correr 5 km", Color(0xFFA78BFA), done = false)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spec.gap)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(spec.gap),
            ) {
                Stat(spec, "12", "Sequência", filled = true, modifier = Modifier.weight(1f))
                Stat(spec, "31", "Recorde", filled = false, modifier = Modifier.weight(1f))
                Stat(spec, "73%", "Últimos 30 dias", filled = false, modifier = Modifier.weight(1f))
            }
        }
    }

    @Composable
    private fun Progress(spec: StyleSpec, fraction: Float) {
        val shape = RoundedCornerShape(if (spec.radius > 8.dp) 8.dp else spec.radius)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (spec.shadow == ShadowStyle.HARD) 18.dp else 8.dp)
                .surface(spec, shape, fill = spec.track),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(spec.accent, shape),
            )
        }
    }

    @Composable
    private fun GoalRow(
        spec: StyleSpec,
        title: String,
        tag: Color,
        done: Boolean,
        note: String? = null,
    ) {
        val shape = RoundedCornerShape(spec.radius)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .surface(spec, shape, fill = spec.surface)
                .padding(spec.rowPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Check(spec, done = done, tint = if (spec.tagAsDot) spec.accent else tag)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = spec.body,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (done) spec.secondary else spec.text,
                        textDecoration = if (done) TextDecoration.LineThrough else null,
                    ),
                )
                if (note != null) {
                    Text(
                        text = note,
                        style = TextStyle(fontFamily = spec.body, fontSize = 13.sp, color = spec.secondary),
                    )
                }
            }
            // The colour that identifies a goal: a loud fill in the current style, a
            // quiet dot in every proposal — which is what frees colour for the data.
            if (spec.tagAsDot) {
                Box(modifier = Modifier.size(8.dp).background(tag, RoundedCornerShape(4.dp)))
            }
        }
    }

    @Composable
    private fun Check(spec: StyleSpec, done: Boolean, tint: Color) {
        val shape = RoundedCornerShape(if (spec.radius > 8.dp) 8.dp else spec.radius)
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(if (done) tint else Color.Transparent, shape)
                .border(
                    width = if (spec.border > 0.dp) spec.border else 1.5.dp,
                    color = if (done) tint else spec.secondary.copy(alpha = 0.5f),
                    shape = shape,
                )
                .drawBehind {
                    if (!done) return@drawBehind
                    val w = size.width
                    val h = size.height
                    val path = Path().apply {
                        moveTo(w * 0.26f, h * 0.52f)
                        lineTo(w * 0.44f, h * 0.70f)
                        lineTo(w * 0.76f, h * 0.30f)
                    }
                    drawPath(
                        path = path,
                        color = spec.onAccent,
                        style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter),
                    )
                },
        )
    }

    @Composable
    private fun Stat(
        spec: StyleSpec,
        value: String,
        label: String,
        filled: Boolean,
        modifier: Modifier = Modifier,
    ) {
        val shape = RoundedCornerShape(spec.radius)
        Column(
            modifier = modifier
                .fillMaxHeight()
                .surface(spec, shape, fill = if (filled) spec.accent else spec.surface)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = spec.display,
                    fontSize = 26.sp,
                    fontWeight = if (spec.display == FontFamily.SansSerif) FontWeight.Bold else FontWeight.Normal,
                    color = if (filled) spec.onAccent else spec.text,
                ),
            )
            Text(
                text = spec.label(label),
                style = TextStyle(
                    fontFamily = spec.body,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (filled) spec.onAccent else spec.secondary,
                ),
            )
        }
    }

    private fun StyleSpec.label(text: String) = if (uppercase) text.uppercase() else text

    /** The one place the four directions actually diverge: how a surface is separated. */
    private fun Modifier.surface(spec: StyleSpec, shape: Shape, fill: Color): Modifier = when (spec.shadow) {
        ShadowStyle.HARD ->
            drawBehind {
                val outline = shape.createOutline(size, layoutDirection, this)
                translate(left = spec.shadowOffset.toPx(), top = spec.shadowOffset.toPx()) {
                    drawOutline(outline, spec.shadowColor)
                }
            }
                .background(fill, shape)
                .border(spec.border, spec.divider, shape)

        ShadowStyle.SOFT ->
            shadow(elevation = 2.dp, shape = shape, ambientColor = Color.Black, spotColor = Color.Black)
                .background(fill, shape)

        ShadowStyle.NONE ->
            background(fill, shape)
                .then(
                    if (spec.border > 0.dp) Modifier.border(spec.border, spec.divider, shape) else Modifier,
                )
    }

    // endregion
}
