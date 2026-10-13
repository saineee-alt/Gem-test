package com.example.ui.capsule.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.state.CapsuleCardData
import com.example.ui.components.BatteryRingGauge

@Composable
fun ChargingCollapsedContent(
    card: CapsuleCardData.ChargingCard,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .testTag("charging_collapsed"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Charging bolt glyph
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Charging",
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Center space reserved for hardware camera hole
        Spacer(modifier = Modifier.width(36.dp))

        // Right: Battery percentage
        Text(
            text = "${card.level}%",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ChargingExpandedContent(
    card: CapsuleCardData.ChargingCard,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("charging_expanded")
    ) {
        // Top header: Battery Ring Gauge + Status + Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BatteryRingGauge(
                level = card.level,
                isCharging = card.isCharging,
                ringColor = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.tertiaryContainer
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (card.isFastCharging) "Fast Charging" else "Charging",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = if (card.timeToFullMinutes > 0) "${card.timeToFullMinutes}m until full" else "Battery full",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Trailing circular collapse button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(onClick = onCollapse, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Collapse",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3-column stats grid: Level & Time | Voltage & Temp | Current & Wattage
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatColumn(
                label1 = "Level",
                value1 = "${card.level}%",
                label2 = "Time Left",
                value2 = if (card.timeToFullMinutes > 0) "${card.timeToFullMinutes}m" else "Full"
            )
            StatColumn(
                label1 = "Voltage",
                value1 = "${card.voltage} V",
                label2 = "Temp",
                value2 = "${card.temperatureCelsius}°C"
            )
            StatColumn(
                label1 = "Current",
                value1 = "≈${card.currentAmperes} A",
                label2 = "Power",
                value2 = "${card.wattageWatts} W"
            )
        }
    }
}

@Composable
private fun StatColumn(
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label1,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value1,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label2,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value2,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
