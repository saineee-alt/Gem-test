package com.example.ui.settings

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwipeDown
import androidx.compose.material.icons.filled.SwipeVertical
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AppSettingsRepository
import com.example.data.CapsuleSettings
import com.example.overlay.CapsuleOverlayService
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder
import com.example.ui.components.CategoryCard
import kotlinx.coroutines.launch

@Composable
fun IsletHomeScreen(
    settings: CapsuleSettings,
    repository: AppSettingsRepository,
    onOpenPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hasOverlay = Settings.canDrawOverlays(context)

    // Category detail modal states
    var activeDialog by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Overlay Permission Banner if not granted
        if (!hasOverlay) {
            item {
                Card(
                    onClick = onOpenPermissions,
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("permission_alert_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Overlay Permission Required",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Tap here to allow 'Display over other apps' so Islet can render near your camera cutout.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        // Hero Master Switch Card
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.overlayEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("hero_master_switch_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (settings.overlayEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (settings.overlayEnabled) "Capsule Overlay Active" else "Capsule Disabled",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (settings.overlayEnabled)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Anchored near center front camera cutout",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (settings.overlayEnabled)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = settings.overlayEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    repository.updateOverlayEnabled(enabled)
                                    if (enabled) {
                                        CapsuleOverlayService.start(context)
                                    } else {
                                        CapsuleOverlayService.stop(context)
                                    }
                                }
                            },
                            modifier = Modifier.testTag("master_overlay_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                CapsuleOverlayService.stop(context)
                                CapsuleOverlayService.start(context)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("restart_service_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restart Overlay")
                        }

                        FilledTonalButton(
                            onClick = {
                                CapsuleStateHolder.toggleExpandCollapse()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("preview_capsule_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Toggle")
                        }
                    }
                }
            }
        }

        // Section Title: Categories
        item {
            Text(
                text = "Capsule Controls & Events",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        // 1. Mini Capsule Events
        item {
            CategoryCard(
                title = "Mini Capsule Events",
                guideText = "Configure",
                icons = listOf(
                    Icons.Default.Bolt to "Charging",
                    Icons.Default.Bluetooth to "Bluetooth",
                    Icons.Default.Wifi to "Wi-Fi",
                    Icons.Default.VolumeMute to "Mute"
                ),
                onCardClick = { activeDialog = "mini_events" },
                onGuideClick = { activeDialog = "mini_events" }
            )
        }

        // 2. Dynamic Cards
        item {
            CategoryCard(
                title = "Dynamic Cards",
                guideText = "Configure",
                icons = listOf(
                    Icons.Default.MusicNote to "Media",
                    Icons.Default.Bolt to "Charging Stats",
                    Icons.Default.Download to "Download",
                    Icons.Default.VolumeUp to "Sound"
                ),
                onCardClick = { activeDialog = "dynamic_cards" },
                onGuideClick = { activeDialog = "dynamic_cards" }
            )
        }

        // 3. Gestures & Cards
        item {
            CategoryCard(
                title = "Gestures & Cards",
                guideText = "Remap",
                icons = listOf(
                    Icons.Default.TouchApp to "Tap",
                    Icons.Default.Fingerprint to "Long Press",
                    Icons.Default.SwipeDown to "Swipe Down",
                    Icons.Default.SwipeVertical to "Swipe Up"
                ),
                onCardClick = { activeDialog = "gestures" },
                onGuideClick = { activeDialog = "gestures" }
            )
        }

        // 4. Notifications
        item {
            CategoryCard(
                title = "Notifications",
                guideText = "Per-App",
                icons = listOf(
                    Icons.Default.Notifications to "WhatsApp",
                    Icons.Default.Notifications to "Telegram",
                    Icons.Default.Notifications to "SMS",
                    Icons.Default.Check to "Filter"
                ),
                onCardClick = { activeDialog = "notifications" },
                onGuideClick = { activeDialog = "notifications" }
            )
        }
    }

    // Modal dialogs for the 4 categories
    when (activeDialog) {
        "mini_events" -> {
            MiniEventsDialog(
                settings = settings,
                repository = repository,
                onDismiss = { activeDialog = null }
            )
        }
        "dynamic_cards" -> {
            DynamicCardsDialog(
                settings = settings,
                repository = repository,
                onDismiss = { activeDialog = null }
            )
        }
        "gestures" -> {
            GesturesDialog(
                settings = settings,
                repository = repository,
                onDismiss = { activeDialog = null }
            )
        }
        "notifications" -> {
            NotificationAppsDialog(
                settings = settings,
                repository = repository,
                onDismiss = { activeDialog = null }
            )
        }
    }
}

@Composable
private fun MiniEventsDialog(
    settings: CapsuleSettings,
    repository: AppSettingsRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mini Capsule Events") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Instantaneous subtle pill notifications that appear for 1.5–2s when a system event happens.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Quick Toggles", modifier = Modifier.weight(1f))
                    Switch(
                        checked = settings.quickTogglesEnabled,
                        onCheckedChange = {
                            scope.launch { repository.updateCardEnabled("quick_toggles", it) }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun DynamicCardsDialog(
    settings: CapsuleSettings,
    repository: AppSettingsRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dynamic Cards") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CardToggleRow(
                    label = "Charging & Battery Stats Card",
                    checked = settings.chargingCardEnabled,
                    onCheckedChange = { scope.launch { repository.updateCardEnabled("charging", it) } }
                )
                CardToggleRow(
                    label = "Media & Squiggly Scrubber Card",
                    checked = settings.mediaCardEnabled,
                    onCheckedChange = { scope.launch { repository.updateCardEnabled("media", it) } }
                )
                CardToggleRow(
                    label = "Download & Install Card",
                    checked = settings.downloadCardEnabled,
                    onCheckedChange = { scope.launch { repository.updateCardEnabled("download", it) } }
                )
                CardToggleRow(
                    label = "Message & Inline Reply Card",
                    checked = settings.notificationCardEnabled,
                    onCheckedChange = { scope.launch { repository.updateCardEnabled("notification", it) } }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun CardToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun GesturesDialog(
    settings: CapsuleSettings,
    repository: AppSettingsRepository,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestures & Actions") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tap: ${settings.gestureTap}", style = MaterialTheme.typography.bodyMedium)
                Text("Long Press: ${settings.gestureLongPress}", style = MaterialTheme.typography.bodyMedium)
                Text("Swipe Down: ${settings.gestureSwipeDown}", style = MaterialTheme.typography.bodyMedium)
                Text("Swipe Up: ${settings.gestureSwipeUp}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "You can remap these gestures to any custom action in the Settings tab.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("OK") }
        }
    )
}

@Composable
private fun NotificationAppsDialog(
    settings: CapsuleSettings,
    repository: AppSettingsRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val popularApps = listOf(
        "com.whatsapp" to "WhatsApp",
        "org.telegram.messenger" to "Telegram",
        "com.google.android.apps.messaging" to "Google Messages",
        "com.twitter.android" to "X / Twitter",
        "com.slack" to "Slack",
        "com.spotify.music" to "Spotify"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Notification Passthrough") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select which apps are allowed to push rich glanceable cards into the capsule:",
                    style = MaterialTheme.typography.bodySmall
                )
                popularApps.forEach { (pkg, name) ->
                    val isChecked = settings.notificationAllowedPackages.contains(pkg)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                scope.launch { repository.toggleNotificationPackage(pkg, checked) }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Done") }
        }
    )
}
