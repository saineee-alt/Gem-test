package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Animated Squiggly Scrubber track replicating the modern dynamic media seekbar.
 * Draws a flowing sine wave for the elapsed portion, a draggable seek thumb dot, and a muted straight or wavy track for remaining.
 */
@Composable
fun SquigglyWaveform(
    progress: Float, // 0.0f to 1.0f
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    waveAmplitude: Dp = 4.dp,
    wavelength: Dp = 24.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) (2 * PI).toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(fraction)
                }
            }
            .testTag("squiggly_waveform_scrubber"),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val centerY = size.height / 2f
            val seekX = width * progress.coerceIn(0f, 1f)
            val amp = waveAmplitude.toPx()
            val wl = wavelength.toPx()

            // 1. Inactive track (remaining duration) - straight line with subtle opacity
            drawLine(
                color = inactiveColor,
                start = Offset(seekX, centerY),
                end = Offset(width, centerY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 2. Active squiggly track (elapsed duration)
            if (seekX > 0f) {
                val path = Path()
                path.moveTo(0f, centerY)

                val step = 2f
                var x = 0f
                while (x <= seekX) {
                    val angle = (x / wl) * 2 * PI.toFloat() + phase
                    val y = centerY + sin(angle) * amp
                    path.lineTo(x, y)
                    x += step
                }

                drawPath(
                    path = path,
                    color = activeColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // 3. Thumb dot at current position
                val currentAngle = (seekX / wl) * 2 * PI.toFloat() + phase
                val thumbY = centerY + sin(currentAngle) * amp
                drawCircle(
                    color = thumbColor,
                    radius = 6.dp.toPx(),
                    center = Offset(seekX, thumbY)
                )
            }
        }
    }
}

/**
 * Animated 4-bar Equalizer / Audio Waveform glyph for collapsed music pill.
 */
@Composable
fun AudioEqualizerGlyph(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq_bars")
    val animVal by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq_anim"
    )

    Canvas(
        modifier = modifier
            .size(width = 16.dp, height = 14.dp)
            .testTag("eq_glyph")
    ) {
        val barWidth = 2.5.dp.toPx()
        val spacing = 1.8.dp.toPx()
        val maxHeight = size.height

        val heights = if (isPlaying) {
            listOf(
                maxHeight * (0.35f + 0.55f * animVal),
                maxHeight * (0.85f - 0.45f * animVal),
                maxHeight * (0.45f + 0.50f * animVal),
                maxHeight * (0.90f - 0.60f * animVal)
            )
        } else {
            listOf(maxHeight * 0.35f, maxHeight * 0.55f, maxHeight * 0.4f, maxHeight * 0.25f)
        }

        heights.forEachIndexed { i, h ->
            val startX = i * (barWidth + spacing)
            val topY = maxHeight - h
            drawRoundRect(
                color = color,
                topLeft = Offset(startX, topY),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}

/**
 * Circular battery ring gauge with dynamic charging bolt.
 */
@Composable
fun BatteryRingGauge(
    level: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.tertiary,
    trackColor: Color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
) {
    Box(
        modifier = modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(52.dp)) {
            val strokeWidth = 4.5.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background circle track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(strokeWidth)
            )

            // Progress sweep arc
            val sweepAngle = (level.coerceIn(0, 100) / 100f) * 360f
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$level%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Reusable M3 ListItem row for settings with consistent leading icon in tonal circle, headline, and trailing control.
 */
@Composable
fun M3SettingRow(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(onClick) {
                if (onClick != null) {
                    detectTapGestures { onClick() }
                }
            }
            .testTag("setting_row_${title.lowercase().replace(" ", "_")}")
    )
}

/**
 * Category Card for Home Hub featuring 4 tonal circular icon buttons and trailing chevron.
 */
@Composable
fun CategoryCard(
    title: String,
    guideText: String = "Guide",
    icons: List<Pair<ImageVector, String>>,
    onCardClick: () -> Unit,
    onGuideClick: () -> Unit = {}
) {
    Card(
        onClick = onCardClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("category_card_${title.lowercase().replace(" ", "_")}")
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row: Title + Guide text button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onGuideClick) {
                    Text(
                        text = guideText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 4 Icon button row + trailing chevron
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                icons.take(4).forEach { (icon, desc) ->
                    FilledTonalIconButton(
                        onClick = onCardClick,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = desc,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Trailing chevron indicator
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "›",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
