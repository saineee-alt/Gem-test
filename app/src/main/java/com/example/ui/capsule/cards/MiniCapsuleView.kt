package com.example.ui.capsule.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.state.CapsuleCardData
import com.example.state.MiniEventType

@Composable
fun MiniCapsuleContent(
    card: CapsuleCardData.MiniQuickToggle,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor, statusText) = getMiniEventProps(card.eventType, card.batteryPercent)

    Row(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .testTag("mini_capsule_content"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Leading glyph
        Surface(
            shape = CircleShape,
            color = iconColor.copy(alpha = 0.2f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = card.title,
                    tint = iconColor,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Space for center camera cutout
        Spacer(modifier = Modifier.width(36.dp))

        // Trailing status glyph / text
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = iconColor
        )
    }
}

@Composable
private fun getMiniEventProps(
    eventType: MiniEventType,
    batteryPercent: Int?
): Triple<ImageVector, Color, String> {
    return when (eventType) {
        MiniEventType.CHARGING_START -> Triple(Icons.Default.Bolt, MaterialTheme.colorScheme.tertiary, "Charging")
        MiniEventType.CHARGING_STOP -> Triple(Icons.Default.Bolt, MaterialTheme.colorScheme.onSurfaceVariant, "Unplugged")
        MiniEventType.MUTE_ON -> Triple(Icons.Default.VolumeMute, MaterialTheme.colorScheme.error, "Mute")
        MiniEventType.MUTE_OFF -> Triple(Icons.Default.VolumeUp, MaterialTheme.colorScheme.primary, "Ring")
        MiniEventType.HEADPHONES_CONNECTED -> Triple(
            Icons.Default.Headphones,
            MaterialTheme.colorScheme.secondary,
            batteryPercent?.let { "$it%" } ?: "Connected"
        )
        MiniEventType.BLUETOOTH_CONNECTED -> Triple(
            Icons.Default.Bluetooth,
            MaterialTheme.colorScheme.secondary,
            batteryPercent?.let { "$it%" } ?: "Ready"
        )
        MiniEventType.WIFI_CONNECTED -> Triple(Icons.Default.Wifi, MaterialTheme.colorScheme.tertiary, "Connected")
        MiniEventType.WIFI_DISCONNECTED -> Triple(Icons.Default.Wifi, MaterialTheme.colorScheme.error, "Offline")
        MiniEventType.FLASHLIGHT_ON -> Triple(Icons.Default.FlashlightOn, MaterialTheme.colorScheme.tertiary, "Torch On")
        MiniEventType.FLASHLIGHT_OFF -> Triple(Icons.Default.FlashlightOn, MaterialTheme.colorScheme.onSurfaceVariant, "Torch Off")
        MiniEventType.DARK_MODE_TOGGLED -> Triple(Icons.Default.DarkMode, MaterialTheme.colorScheme.primary, "Dark Mode")
        MiniEventType.VOLUME_CHANGED -> Triple(Icons.Default.VolumeUp, MaterialTheme.colorScheme.primary, "Volume")
    }
}
