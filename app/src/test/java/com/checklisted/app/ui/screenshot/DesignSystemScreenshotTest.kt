package com.checklisted.app.ui.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.checklisted.app.ui.components.NeoButtonSamples
import com.checklisted.app.ui.components.NeoCardSamples
import com.checklisted.app.ui.components.NeoCheckboxSamples
import com.checklisted.app.ui.components.NeoChipSamples
import com.checklisted.app.ui.components.NeoProgressBarSamples
import com.checklisted.app.ui.components.NeoSwipeRowSamples
import com.checklisted.app.ui.components.NeoTextFieldSamples
import com.checklisted.app.ui.theme.NeoTheme
import org.junit.Rule
import org.junit.Test

/**
 * Renders the design system to PNGs on the JVM.
 *
 * This is the only automated check on how any of this actually looks: the rest of
 * the suite proves behaviour, and previews need an IDE to render. Each component is
 * captured in both themes, since the dark theme flips the ink and the shadow and is
 * where contrast mistakes hide.
 */
class DesignSystemScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_6,
        // SHRINK crops to the content, so a component's own proportions are visible
        // instead of a mostly empty phone screen.
        renderingMode = SessionParams.RenderingMode.SHRINK,
        showSystemUi = false,
    )

    private fun snapshot(name: String, dark: Boolean, content: @Composable () -> Unit) {
        paparazzi.snapshot(name = name) {
            NeoTheme(darkTheme = dark) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeoTheme.colors.background)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    content()
                }
            }
        }
    }

    private fun bothThemes(name: String, content: @Composable () -> Unit) {
        snapshot("$name-claro", dark = false, content = content)
        snapshot("$name-escuro", dark = true, content = content)
    }

    @Test
    fun buttons() = bothThemes("botoes") { NeoButtonSamples() }

    @Test
    fun checkboxes() = bothThemes("checkbox") { NeoCheckboxSamples() }

    @Test
    fun chips() = bothThemes("chips") { NeoChipSamples() }

    @Test
    fun progressBars() = bothThemes("progresso") { NeoProgressBarSamples() }

    @Test
    fun textFields() = bothThemes("campo-de-texto") { NeoTextFieldSamples() }

    @Test
    fun cards() = bothThemes("cards") { NeoCardSamples() }

    /** Both drawers open, which is the only state worth looking at. */
    @Test
    fun swipeActions() = bothThemes("gestos") { NeoSwipeRowSamples() }
}
