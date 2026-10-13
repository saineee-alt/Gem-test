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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder
import com.example.state.MiniEventType

@Composable
fun TestStudioScreen(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Interactive Test Studio",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Tap any scenario below to immediately inject live card states into the capsule overlay without waiting for physical hardware events.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // State control row (Expand / Collapse / Dismiss)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { CapsuleStateHolder.expandCurrent() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("test_expand_btn")
                ) {
                    Icon(imageVector = Icons.Default.OpenInFull, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expand")
                }

                FilledTonalButton(
                    onClick = { CapsuleStateHolder.collapseCurrent() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("test_collapse_btn")
                ) {
                    Text("Collapse")
                }

                OutlinedButton(
                    onClick = { CapsuleStateHolder.dismissToIdle() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).testTag("test_dismiss_btn")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Idle")
                }
            }
        }

        // Section: Full Dynamic Cards
        item {
            Text(
                text = "Full Interactive Cards",
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Charging card test
                    ScenarioTestRow(
                        title = "Charging (65W Fast Charge)",
                        subtitle = "84% · 4.1V · 33.2°C · 1.62A · 6.64W",
                        icon = Icons.Default.Bolt,
                        onTrigger = {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.ChargingCard(
                                    level = 84,
                                    isCharging = true,
                                    isFastCharging = true,
                                    voltage = 4.1f,
                                    temperatureCelsius = 33.2f,
                                    currentAmperes = 1.62f,
                                    wattageWatts = 6.64f,
                                    timeToFullMinutes = 22
                                ),
                                autoExpand = true
                            )
                        }
                    )

                    // Media playback test
                    ScenarioTestRow(
                        title = "Media Player (Synthwave)",
                        subtitle = "'Sunset Neon' by Kavinsky · Squiggly scrubber",
                        icon = Icons.Default.MusicNote,
                        onTrigger = {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MediaCard(
                                    trackTitle = "Sunset Neon",
                                    artistName = "Kavinsky",
                                    isPlaying = true,
                                    durationMs = 240000L,
                                    positionMs = 78000L,
                                    isFavorite = true
                                ),
                                autoExpand = true
                            )
                        }
                    )

                    // Download card test
                    ScenarioTestRow(
                        title = "Package Install Progress",
                        subtitle = "Telegram Update · 72% · 14s remaining",
                        icon = Icons.Default.Download,
                        onTrigger = {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.DownloadCard(
                                    appName = "Telegram Messenger",
                                    packageName = "org.telegram.messenger",
                                    progressPercent = 72,
                                    etaSeconds = 14,
                                    isCompleted = false
                                ),
                                autoExpand = true
                            )
                        }
                    )

                    // Notification message test
                    ScenarioTestRow(
                        title = "Incoming WhatsApp Message",
                        subtitle = "Sarah: 'Are you ready for the meeting?' · Inline reply",
                        icon = Icons.Default.Chat,
                        onTrigger = {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MessageCard(
                                    senderName = "Sarah Connor",
                                    messageText = "Hey! Let's review the designs at 4:30 PM.",
                                    appName = "WhatsApp",
                                    packageName = "com.whatsapp",
                                    replyAction = { replyText ->
                                        // Simulate reply sent confirmation
                                        CapsuleStateHolder.postCard(
                                            CapsuleCardData.MiniQuickToggle(
                                                eventType = MiniEventType.MUTE_OFF,
                                                title = "Reply Sent",
                                                subtitle = replyText
                                            )
                                        )
                                    },
                                    dismissAction = { CapsuleStateHolder.dismissToIdle() }
                                ),
                                autoExpand = true
                            )
                        }
                    )
                }
            }
        }

        // Section: Instant Mini Capsules
        item {
            Text(
                text = "Instant Mini Capsule Events",
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ScenarioTestRow(
                        title = "Headphones Connected",
                        subtitle = "AirPods Pro · 85% battery",
                        icon = Icons.Default.Headphones,
                        onTrigger = {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MiniQuickToggle(
                                    eventType = MiniEventType.HEADPHONES_CONNECTED,
                                    title = "AirPods Pro",
                                    subtitle = "Connected",
                                    batteryPercent = 85
                                )
                            )
                        }
                    )

                    ScenarioTestRow(
                        title = "Mute Switched On",
                        subtitle = "Silent mode activated",
                        icon = Icons.Default.VolumeMute,
                        onTrigger = {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MiniQuickToggle(
                                    eventType = MiniEventType.MUTE_ON,
                                    title = "Mute On",
                                    subtitle = "Silent"
                                )
                            )
                        }
                    )

                    ScenarioTestRow(
                        title = "Wi-Fi Connected",
                        subtitle = "Home-5G Network",
                        icon = Icons.Default.Wifi,
                        onTrigger = {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MiniQuickToggle(
                                    eventType = MiniEventType.WIFI_CONNECTED,
                                    title = "Wi-Fi Connected",
                                    subtitle = "Home-5G"
                                )
                            )
                        }
                    )

                    ScenarioTestRow(
                        title = "Flashlight On",
                        subtitle = "Rear torch active",
                        icon = Icons.Default.FlashlightOn,
                        onTrigger = {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MiniQuickToggle(
                                    eventType = MiniEventType.FLASHLIGHT_ON,
                                    title = "Torch Active",
                                    subtitle = "On"
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScenarioTestRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onTrigger: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalButton(
            onClick = onTrigger,
            shape = RoundedCornerShape(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("trigger_${title.lowercase().take(10).replace(" ", "_")}")
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Fire")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
