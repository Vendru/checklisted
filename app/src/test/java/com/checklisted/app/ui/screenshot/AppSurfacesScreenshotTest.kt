package com.checklisted.app.ui.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.checklisted.app.domain.history.HeatmapDay
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.GoalStatus
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.repository.Settings
import com.checklisted.app.ui.components.NeoButton
import com.checklisted.app.ui.components.NeoEmptyState
import com.checklisted.app.ui.components.NeoHeatmap
import com.checklisted.app.ui.components.NeoProgressBar
import com.checklisted.app.ui.components.NeoStatRow
import com.checklisted.app.ui.settings.SettingsContent
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.today.GoalRow
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Renders the surfaces the user actually spends time on.
 *
 * Assembled from the stateless pieces rather than driven through the screens, which
 * would need Hilt and a database. The point is the composition — how a real list of
 * goals reads with sections, counters and mixed accents.
 */
class AppSurfacesScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_6.copy(locale = "pt-rBR"),
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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

    private fun status(
        title: String,
        colorTag: String,
        completed: Boolean,
        description: String? = null,
    ) = GoalStatus(
        goal = Goal(
            id = title,
            title = title,
            description = description,
            recurrence = Recurrence.DAILY,
            colorTag = colorTag,
            position = 0,
            createdAt = Instant.EPOCH,
        ),
        periodKey = PeriodKey("2026-08-16"),
        isCompleted = completed,
    )

    @Test
    fun goalList() = bothThemes("lista-de-metas") {
        Text(
            text = "Hoje",
            style = MaterialTheme.typography.displaySmall,
            color = NeoTheme.colors.ink,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Diárias",
                style = MaterialTheme.typography.headlineSmall,
                color = NeoTheme.colors.ink,
            )
            Text(
                text = "2/3",
                style = MaterialTheme.typography.titleMedium,
                color = NeoTheme.colors.ink,
            )
        }
        NeoProgressBar(progress = 2f / 3f, animated = false)

        GoalRow(
            status = status("Beber 2L de água", "TEAL", completed = true),
            onToggle = {},
            onOpen = {},
        )
        GoalRow(
            status = status(
                title = "Ler 20 páginas",
                colorTag = "PINK",
                completed = false,
                description = "Antes de dormir, sem tela.",
            ),
            onToggle = {},
            onOpen = {},
        )
        GoalRow(
            status = status("Correr 5 km", "PURPLE", completed = false),
            onToggle = {},
            onOpen = {},
        )
    }

    @Test
    fun emptyState() = bothThemes("estado-vazio") {
        NeoEmptyState(
            title = "Nada aqui ainda",
            message = "Sua lista está em branco, o que é bem menos culpa do que parece. " +
                "Crie a primeira meta e comece a riscar.",
            action = { NeoButton(text = "Nova meta", onClick = {}) },
        )
    }

    @Test
    fun goalStats() = bothThemes("estatisticas") {
        NeoStatRow(
            currentStreak = 12,
            bestStreak = 31,
            ratePercent = 73,
            rateLabel = "Últimos 30 dias",
        )
    }

    /**
     * Locks the settings screen down.
     *
     * The five reminder-time chips overflowed a plain Row on a real phone and the
     * last one was squeezed until its text wrapped one character per line. Nothing
     * covered this screen, so nothing caught it.
     */
    @Test
    fun settings() = bothThemes("configuracoes") {
        SettingsContent(
            settings = Settings(reminderEnabled = true, reminderTime = LocalTime.of(8, 0)),
            permissionDenied = false,
            onBack = {},
            onWeekStartChange = {},
            onThemeChange = {},
            onReminderToggle = {},
            onReminderTimeChange = {},
        )
    }

    @Test
    fun heatmap() = bothThemes("heatmap") {
        // Twelve weeks and two days, with a mix of intensities and a stretch of
        // untracked days before the goals existed. The partial last column is the
        // point: a range of exactly twelve weeks never exercises it, and the app only
        // passes a whole number of weeks on one weekday out of seven.
        val start = LocalDate.parse("2026-05-25")
        val pattern = listOf(3, 3, 2, 0, 1, 3, 2, 0, 3, 3, 3, 1, 0, 2)
        val days = (0 until 86).map { offset ->
            HeatmapDay(
                date = start.plusDays(offset.toLong()),
                completed = if (offset < 9) 0 else pattern[offset % pattern.size],
                total = if (offset < 9) 0 else 3,
            )
        }
        NeoHeatmap(days = days)
    }
}
