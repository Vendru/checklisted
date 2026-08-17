package com.checklisted.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checklisted.app.R
import com.checklisted.app.domain.model.ThemeMode
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.domain.repository.Settings
import com.checklisted.app.ui.components.NeoBackButton
import com.checklisted.app.ui.components.NeoCard
import com.checklisted.app.ui.components.NeoCheckbox
import com.checklisted.app.ui.components.NeoChip
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.components.readableWidth
import com.checklisted.app.ui.theme.NeoTheme
import java.time.LocalTime

/** Five minutes is fine enough for a daily nudge and keeps the grid scannable. */
private const val MINUTE_STEP = 5

/** Title, buttons, padding and the dialog's own margins, around whatever it wraps. */
private val DialogChromeHeight = 240.dp

private fun LocalTime.formatted(): String = "%02d:%02d".format(hour, minute)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenArchived: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var permissionDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionDenied = !granted
        // The preference is stored either way: the user asked for a reminder, and
        // silently switching it back off would be lying to them about their own
        // setting. The worker checks the permission again before posting.
        viewModel.setReminderEnabled(true)
    }

    SettingsContent(
        settings = state.settings,
        permissionDenied = permissionDenied,
        onBack = onBack,
        onWeekStartChange = viewModel::setWeekStart,
        onThemeChange = viewModel::setThemeMode,
        onReminderToggle = { enabled ->
            if (enabled && needsNotificationPermission(context)) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                permissionDenied = false
                viewModel.setReminderEnabled(enabled)
            }
        },
        onReminderTimeChange = viewModel::setReminderTime,
        nextReminder = state.nextReminder,
        onOpenArchived = onOpenArchived,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsContent(
    settings: Settings,
    permissionDenied: Boolean,
    onBack: () -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeChange: (LocalTime) -> Unit,
    onOpenArchived: () -> Unit,
    modifier: Modifier = Modifier,
    nextReminder: NextReminder? = null,
) {
    val colors = NeoTheme.colors
    var picking by rememberSaveable { mutableStateOf(false) }

    val insets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
        .asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .readableWidth()
            .verticalScroll(rememberScrollState())
            .padding(insets),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoBackButton(onClick = onBack)
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
            )
        }

        SettingsSection(
            title = stringResource(R.string.settings_week_start),
            hint = stringResource(R.string.settings_week_start_hint),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WeekStart.entries.forEach { option ->
                    NeoChip(
                        label = stringResource(option.labelRes()),
                        selected = settings.weekStart == option,
                        onClick = { onWeekStartChange(option) },
                    )
                }
            }
        }

        SettingsSection(title = stringResource(R.string.settings_theme)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ThemeMode.entries.forEach { option ->
                    NeoChip(
                        label = stringResource(option.labelRes()),
                        selected = settings.themeMode == option,
                        onClick = { onThemeChange(option) },
                    )
                }
            }
        }

        SettingsSection(
            title = stringResource(R.string.settings_reminder),
            hint = stringResource(R.string.settings_reminder_hint),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NeoCheckbox(
                    checked = settings.reminderEnabled,
                    contentDescription = stringResource(R.string.settings_reminder_enable),
                    onCheckedChange = onReminderToggle,
                )
                Text(
                    text = stringResource(R.string.settings_reminder_enable),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.ink,
                )
            }

            if (permissionDenied) {
                Text(
                    text = stringResource(R.string.settings_reminder_permission_denied),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.ink,
                )
            }

            if (settings.reminderEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_reminder_time),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkSoft,
                        )
                        Text(
                            text = settings.reminderTime.formatted(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = colors.ink,
                        )
                    }
                    NeoOutlineButton(
                        text = stringResource(R.string.settings_reminder_change),
                        onClick = { picking = true },
                    )
                }

                // Saying when it next goes off is the only feedback that the schedule
                // was actually booked — otherwise a reminder that silently failed to
                // register looks exactly like one that is working.
                if (nextReminder != null) {
                    Text(
                        text = stringResource(
                            if (nextReminder.isToday) {
                                R.string.settings_reminder_next_today
                            } else {
                                R.string.settings_reminder_next_tomorrow
                            },
                            nextReminder.time.formatted(),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSoft,
                    )
                }
            }
        }

        SettingsSection(
            title = stringResource(R.string.settings_archived),
            hint = stringResource(R.string.settings_archived_hint),
        ) {
            NeoOutlineButton(
                text = stringResource(R.string.settings_archived_open),
                onClick = onOpenArchived,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (picking) {
        ReminderTimeDialog(
            initial = settings.reminderTime,
            onPick = {
                picking = false
                onReminderTimeChange(it)
            },
            onDismiss = { picking = false },
        )
    }
}

/**
 * Any time of day, in two taps.
 *
 * A grid rather than a wheel or a stepper: every option is visible, it is built from
 * the same chips the rest of this screen uses, and stepping an hour at a time would
 * have meant eleven taps to get from an evening reminder to a morning one. The five
 * fixed presets it replaces left anyone who wakes at six or sleeps at midnight with
 * no reminder they would actually hear.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderTimeDialog(
    initial: LocalTime,
    onPick: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    var hour by rememberSaveable { mutableIntStateOf(initial.hour) }
    var minute by rememberSaveable { mutableIntStateOf(initial.minute - initial.minute % MINUTE_STEP) }

    NeoDialog(
        // The title is the running selection, so the thing being chosen is never off
        // screen behind the grid.
        title = LocalTime.of(hour, minute).formatted(),
        confirmText = stringResource(R.string.action_save),
        dismissText = stringResource(R.string.action_cancel),
        onConfirm = { onPick(LocalTime.of(hour, minute)) },
        onDismissRequest = onDismiss,
    ) {
        ReminderTimeGrid(
            hour = hour,
            minute = minute,
            onHour = { hour = it },
            onMinute = { minute = it },
        )
    }
}

/** The grid itself, separated so a screenshot can render it without a dialog window. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReminderTimeGrid(
    hour: Int,
    minute: Int,
    onHour: (Int) -> Unit,
    onMinute: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Measured off the screen rather than fixed: a cap tall enough to show both grids
    // in portrait is taller than a landscape screen, and one short enough for
    // landscape cut the minutes in half exactly where the row of them begins.
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxHeight = (screenHeight - DialogChromeHeight).coerceIn(160.dp, 440.dp)

    Column(
        modifier = modifier
            .heightIn(max = maxHeight)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.reminder_picker_hour),
            style = MaterialTheme.typography.labelMedium,
            color = NeoTheme.colors.inkSoft,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            (0..23).forEach { option ->
                NeoChip(
                    label = "%02d".format(option),
                    selected = hour == option,
                    onClick = { onHour(option) },
                    compact = true,
                )
            }
        }

        Text(
            text = stringResource(R.string.reminder_picker_minute),
            style = MaterialTheme.typography.labelMedium,
            color = NeoTheme.colors.inkSoft,
            modifier = Modifier.padding(top = 4.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            (0..59 step MINUTE_STEP).forEach { option ->
                NeoChip(
                    label = "%02d".format(option),
                    selected = minute == option,
                    onClick = { onMinute(option) },
                    compact = true,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    content: @Composable () -> Unit,
) {
    NeoCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = NeoTheme.colors.ink,
            )
            if (hint != null) {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeoTheme.colors.ink,
                )
            }
            content()
        }
    }
}

/** Below API 33 the permission does not exist and is always considered granted. */
private fun needsNotificationPermission(context: android.content.Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED

private fun WeekStart.labelRes() = when (this) {
    WeekStart.MONDAY -> R.string.week_start_monday
    WeekStart.SUNDAY -> R.string.week_start_sunday
}

private fun ThemeMode.labelRes() = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
