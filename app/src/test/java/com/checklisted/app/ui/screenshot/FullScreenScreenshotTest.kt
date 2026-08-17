package com.checklisted.app.ui.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.ScreenOrientation
import com.checklisted.app.R
import com.checklisted.app.domain.history.HeatmapDay
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.GoalStatus
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.streak.CompletionRate
import com.checklisted.app.domain.streak.GoalStats
import com.checklisted.app.ui.AppLocale
import com.checklisted.app.ui.archive.ArchivedGoalsContent
import com.checklisted.app.ui.archive.ArchivedGoalsUiState
import com.checklisted.app.ui.components.NeoDialogContent
import com.checklisted.app.ui.goal.GoalDetailContent
import com.checklisted.app.ui.goal.GoalDetailUiState
import com.checklisted.app.ui.history.DayGoal
import com.checklisted.app.ui.history.DayGoalList
import com.checklisted.app.ui.history.HistoryContent
import com.checklisted.app.ui.history.HistoryUiState
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.today.TodayContent
import com.checklisted.app.ui.today.TodaySection
import com.checklisted.app.ui.today.TodayUiState
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

/**
 * Whole screens, at real device size, with the amount of data a used app holds.
 *
 * The component-level images all passed while the detail screen shipped a grid taller
 * than the display and the settings screen shipped a chip turned on its side. Both
 * defects needed the real screen at the real width with the real number of items;
 * neither was visible in a cropped component shot.
 *
 * These render at full device height — no SHRINK — so anything that overflows shows
 * up as content running off the frame.
 */
class FullScreenScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(
        // The app ships only pt-BR strings, so rendering under the default en locale
        // would show Portuguese copy next to English weekday initials and dates —
        // a mix no real user sees.
        deviceConfig = DeviceConfig.PIXEL_6.copy(locale = "pt-rBR"),
        showSystemUi = false,
    )

    // Paparazzi ships no landscape Pixel, so it is the same device turned over. It
    // has to be swapped into the one rule rather than added as a second: two render
    // sessions in one class fight over layoutlib and every test in the class dies.
    private val largeFontConfig = DeviceConfig.PIXEL_6.copy(locale = "pt-rBR", fontScale = 1.3f)

    private val landscapeConfig = DeviceConfig.PIXEL_6.copy(
        locale = "pt-rBR",
        screenWidth = DeviceConfig.PIXEL_6.screenHeight,
        screenHeight = DeviceConfig.PIXEL_6.screenWidth,
        orientation = ScreenOrientation.LANDSCAPE,
    )

    /** A Monday: the first day of a week, so the trailing column is at its shortest. */
    private val today = LocalDate.parse("2026-08-17")

    private fun screen(name: String, dark: Boolean, content: @Composable () -> Unit) {
        paparazzi.snapshot(name = name) {
            NeoTheme(darkTheme = dark) { content() }
        }
    }

    private fun bothThemes(name: String, content: @Composable () -> Unit) {
        screen("$name-claro", dark = false, content = content)
        screen("$name-escuro", dark = true, content = content)
    }

    private fun goal(
        id: String,
        title: String,
        recurrence: Recurrence,
        tag: String,
        description: String? = null,
    ) = Goal(
        id = id,
        title = title,
        description = description,
        recurrence = recurrence,
        colorTag = tag,
        position = 0,
        createdAt = Instant.parse("2026-06-01T00:00:00Z"),
    )

    private fun status(goal: Goal, key: String, completed: Boolean) =
        GoalStatus(goal = goal, periodKey = PeriodKey(key), isCompleted = completed)

    /**
     * The window the app actually passes: week-aligned at the start, ending at today.
     *
     * Deliberately **not** a multiple of seven. [today] is a Monday, so the last column
     * holds a single day — the case that put one cell of the top row outside the grid
     * on a real phone, and the case a tidy thirteen-week range hides.
     */
    private fun heatmap(): List<HeatmapDay> {
        val start = LocalDate.parse("2026-05-18")
        val days = ChronoUnit.DAYS.between(start, today).toInt() + 1
        val pattern = listOf(3, 2, 0, 1, 3, 3, 2, 0, 0, 1, 3, 2, 3, 0)
        return (0 until days).map { offset ->
            HeatmapDay(
                date = start.plusDays(offset.toLong()),
                completed = if (offset < 14) 0 else pattern[offset % pattern.size],
                total = if (offset < 14) 0 else 3,
            )
        }
    }

    @Test
    fun todayFull() = bothThemes("tela-hoje") { todayState() }

    @Composable
    private fun todayState() {
        val daily = PeriodKey("2026-08-17").value
        TodayContent(
            state = TodayUiState(
                date = today,
                isLoading = false,
                // Below, at and well past the point where the badge appears.
                streaks = mapOf("a" to 12, "b" to 1, "c" to 2, "e" to 143),
                sections = listOf(
                    TodaySection(
                        recurrence = Recurrence.DAILY,
                        periodKey = PeriodKey(daily),
                        goals = listOf(
                            status(goal("a", "Beber 2L de água", Recurrence.DAILY, "TEAL"), daily, true),
                            status(
                                goal(
                                    id = "b",
                                    title = "Ler 20 páginas",
                                    recurrence = Recurrence.DAILY,
                                    tag = "PINK",
                                    description = "Antes de dormir, sem tela.",
                                ),
                                daily,
                                false,
                            ),
                            status(goal("c", "Alongar 10 min", Recurrence.DAILY, "YELLOW"), daily, true),
                        ),
                    ),
                    TodaySection(
                        recurrence = Recurrence.WEEKLY,
                        periodKey = PeriodKey("2026-W34"),
                        goals = listOf(
                            status(goal("d", "Correr 5 km", Recurrence.WEEKLY, "PURPLE"), "2026-W34", false),
                            status(goal("e", "Ligar para a mãe", Recurrence.WEEKLY, "ORANGE"), "2026-W34", true),
                        ),
                    ),
                    TodaySection(
                        recurrence = Recurrence.MONTHLY,
                        periodKey = PeriodKey("2026-08"),
                        goals = listOf(
                            status(goal("f", "Revisar as finanças", Recurrence.MONTHLY, "TEAL"), "2026-08", false),
                        ),
                    ),
                ),
            ),
            onToggle = {},
            onOpenGoal = {},
            onCreateGoal = {},
            onOpenHistory = {},
            onOpenSettings = {},
            onDelete = {},
            onMove = { _, _ -> false },
            onCommitOrder = {},
            onCancelReorder = {},
        )
    }

    @Test
    fun goalDetailFull() = bothThemes("tela-detalhe") {
        GoalDetailContent(
            state = GoalDetailUiState(
                goal = goal(
                    id = "d",
                    title = "Correr 5 km",
                    recurrence = Recurrence.WEEKLY,
                    tag = "PURPLE",
                    description = "Três vezes por semana, sem negociar com o sono.",
                ),
                stats = GoalStats(
                    currentStreak = 12,
                    bestStreak = 31,
                    completionRate = CompletionRate(completed = 22, total = 30),
                ),
                heatmap = heatmap(),
                today = today,
                isLoading = false,
            ),
            onBack = {},
            onEdit = {},
            onDayClick = {},
        )
    }

    @Test
    fun historyFull() = bothThemes("tela-historico") {
        HistoryContent(
            state = HistoryUiState(
                heatmap = heatmap(),
                today = today,
                goalCount = 6,
                isLoading = false,
            ),
            onBack = {},
            onDayClick = {},
            onToggle = {},
            onDismissDay = {},
        )
    }

    /**
     * The header at a large system font.
     *
     * Every other render here is fontScale 1.0, which is the setting that hides a
     * header from squashing its own buttons — and a phone set to bigger text is a
     * setting plenty of people use, not an edge case.
     */
    @Test
    fun todayLargeFont() {
        paparazzi.unsafeUpdateConfig(largeFontConfig)
        paparazzi.snapshot(name = "tela-hoje-fonte-grande") {
            NeoTheme(darkTheme = false) { todayState() }
        }
    }

    /**
     * The one wide-screen render.
     *
     * Everything else here is a 411.dp portrait phone, which is exactly the shape that
     * hides a line-length problem. Landscape is 914.dp across and is what proves the
     * reading column is capped.
     */
    @Test
    fun todayLandscape() {
        paparazzi.unsafeUpdateConfig(landscapeConfig)
        paparazzi.snapshot(name = "tela-hoje-paisagem") {
            NeoTheme(darkTheme = false) { todayState() }
        }
    }

    /** The way back out of the archive, which for a while did not exist at all. */
    @Test
    fun archivedGoalsFull() = bothThemes("tela-arquivadas") {
        ArchivedGoalsContent(
            state = ArchivedGoalsUiState(
                isLoading = false,
                goals = listOf(
                    goal("x", "Meditar 10 minutos", Recurrence.DAILY, "PURPLE")
                        .copy(isArchived = true),
                    goal("y", "Estudar alemão toda terça e quinta de manhã", Recurrence.WEEKLY, "TEAL")
                        .copy(isArchived = true),
                    goal("z", "Fechar as contas do mês", Recurrence.MONTHLY, "ORANGE")
                        .copy(isArchived = true),
                ),
            ),
            onBack = {},
            onUnarchive = {},
            onDelete = {},
        )
    }

    @Test
    fun archivedGoalsEmpty() = bothThemes("tela-arquivadas-vazia") {
        ArchivedGoalsContent(
            state = ArchivedGoalsUiState(isLoading = false, goals = emptyList()),
            onBack = {},
            onUnarchive = {},
            onDelete = {},
        )
    }

    /**
     * The question a left swipe asks.
     *
     * The message names the goal, so it is the one dialog whose length depends on user
     * input — a long title is what would push the buttons off the bottom.
     */
    @Test
    fun deleteConfirmation() = bothThemes("confirmar-exclusao") {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeoTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            NeoDialogContent(
                title = stringResource(R.string.dialog_delete_title),
                width = 363.dp,
                message = stringResource(
                    R.string.dialog_delete_goal_message,
                    "Correr 5 km toda terça e quinta de manhã",
                ),
                confirmText = stringResource(R.string.action_delete),
                dismissText = stringResource(R.string.action_cancel),
                destructive = true,
                onConfirm = {},
            )
        }
    }

    /**
     * The day sheet, which only appears after a tap and so was never captured.
     *
     * Rendered as a dialog body rather than through [androidx.compose.ui.window.Dialog]:
     * Paparazzi pins a dialog window to a fixed width and clips content wider than it,
     * so a real window shows the tool's crop instead of the app's layout. The width
     * passed here is the one the app computes on a 411.dp phone — screen minus the
     * 24.dp margins.
     */
    @Test
    fun historyDaySheet() = bothThemes("tela-historico-dia") {
        val day = LocalDate.parse("2026-08-12")
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NeoTheme.colors.background),
            contentAlignment = Alignment.Center,
        ) {
            NeoDialogContent(
                title = day.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(AppLocale),
                ),
                width = 363.dp,
                message = stringResource(R.string.history_day_hint),
                dismissText = stringResource(R.string.action_close),
            ) {
                DayGoalList(
                    goals = listOf(
                        DayGoal(
                            goal("a", "Beber 2L de água", Recurrence.DAILY, "TEAL"),
                            PeriodKey("2026-08-12"),
                            true,
                        ),
                        DayGoal(
                            goal("b", "Ler 20 páginas", Recurrence.DAILY, "PINK"),
                            PeriodKey("2026-08-12"),
                            false,
                        ),
                        DayGoal(
                            goal("d", "Correr 5 km", Recurrence.WEEKLY, "PURPLE").copy(isArchived = true),
                            PeriodKey("2026-W34"),
                            false,
                        ),
                    ),
                    onToggle = {},
                )
            }
        }
    }
}
