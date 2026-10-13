package com.example.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AppSettingsRepository
import com.example.data.CapsuleSettings
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder
import com.example.ui.components.M3SettingRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    settings: CapsuleSettings,
    repository: AppSettingsRepository,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Behavior & Durations",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Lock Screen toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Show on Lock Screen", style = MaterialTheme.typography.titleSmall)
                            Text("Keep capsule visible when device is locked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.showOnLockScreen,
                            onCheckedChange = { scope.launch { repository.updateShowOnLockScreen(it) } },
                            modifier = Modifier.testTag("switch_lock_screen")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto-collapse duration slider
                    Text(
                        text = "Auto-Collapse Duration: ${settings.autoCollapseTimeoutSeconds}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = settings.autoCollapseTimeoutSeconds.toFloat(),
                        onValueChange = { scope.launch { repository.updateAutoCollapseTimeout(it.toInt()) } },
                        valueRange = 2f..15f,
                        modifier = Modifier.testTag("slider_auto_collapse")
                    )
                }
            }
        }

        // Gesture Remapping Section
        item {
            Text(
                text = "Gesture Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GestureDropdownRow(
                        label = "Single Tap",
                        currentAction = settings.gestureTap,
                        options = listOf("Expand", "Collapse", "Quick Actions", "None"),
                        onSelect = { scope.launch { repository.updateGesture("tap", it) } }
                    )

                    GestureDropdownRow(
                        label = "Long Press",
                        currentAction = settings.gestureLongPress,
                        options = listOf("Open App", "Quick Actions", "Expand", "None"),
                        onSelect = { scope.launch { repository.updateGesture("long_press", it) } }
                    )

                    GestureDropdownRow(
                        label = "Swipe Down",
                        currentAction = settings.gestureSwipeDown,
                        options = listOf("Expand", "Open App", "None"),
                        onSelect = { scope.launch { repository.updateGesture("swipe_down", it) } }
                    )

                    GestureDropdownRow(
                        label = "Swipe Up",
                        currentAction = settings.gestureSwipeUp,
                        options = listOf("Dismiss", "Collapse", "None"),
                        onSelect = { scope.launch { repository.updateGesture("swipe_up", it) } }
                    )
                }
            }
        }

        // Automation / Power User
        item {
            Text(
                text = "Integrations & Automations",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    M3SettingRow(
                        title = "Broadcast Intents",
                        subtitle = "Support Tasker and MacroDroid automation triggers",
                        leadingIcon = Icons.Default.Widgets
                    )
                    M3SettingRow(
                        title = "Service Notification",
                        subtitle = "Persistent notification ensures zero background killing",
                        leadingIcon = Icons.Default.NotificationsActive
                    )
                }
            }
        }

        // Actions: Test Gesture & Reset Defaults
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        CapsuleStateHolder.toggleExpandCollapse()
                        scope.launch {
                            snackbarHostState.showSnackbar("Gesture simulated on capsule overlay")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("test_gesture_btn")
                ) {
                    Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Gesture")
                }

                FilledTonalButton(
                    onClick = {
                        scope.launch {
                            repository.resetDefaults()
                            snackbarHostState.showSnackbar("All settings reset to defaults")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.testTag("reset_defaults_btn")
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GestureDropdownRow(
    label: String,
    currentAction: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = currentAction,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(14.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .menuAnchor()
                    .width(160.dp)
                    .testTag("dropdown_${label.lowercase().replace(" ", "_")}")
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            onSelect(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
