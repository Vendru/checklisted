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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.theme.NeoTheme
import java.time.LocalTime

private val REMINDER_TIMES = listOf(
    LocalTime.of(8, 0),
    LocalTime.of(12, 0),
    LocalTime.of(18, 0),
    LocalTime.of(20, 0),
    LocalTime.of(22, 0),
)

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

    var permissionDenied by remember { mutableStateOf(false) }

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
) {
    val colors = NeoTheme.colors

    val insets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
        .asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
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
                Text(
                    text = stringResource(R.string.settings_reminder_time),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.ink,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    REMINDER_TIMES.forEach { time ->
                        NeoChip(
                            label = "%02d:%02d".format(time.hour, time.minute),
                            selected = settings.reminderTime == time,
                            onClick = { onReminderTimeChange(time) },
                        )
                    }
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
