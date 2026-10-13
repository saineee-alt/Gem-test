package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AppSettingsRepository
import com.example.data.CapsuleSettings
import kotlinx.coroutines.launch

@Composable
fun StyleScreen(
    settings: CapsuleSettings,
    repository: AppSettingsRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Preview Canvas
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Live Capsule Calibration",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated screen top with camera notch & pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // Notch dot
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(12.dp)
                                .background(MaterialTheme.colorScheme.onSurface, CircleShape)
                        )

                        // Sample Pill Preview
                        val previewBorder = when (settings.borderWidthMode) {
                            "Thin" -> Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50.dp))
                            "Filled" -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50.dp))
                            else -> Modifier
                        }

                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .padding(top = (settings.cutoutOffsetY / 2).coerceIn(0, 24).dp)
                                .width((settings.pillWidth * 0.9f).dp)
                                .height((settings.pillHeight * 0.9f).dp)
                                .scale(settings.pillScale)
                                .alpha(settings.pillOpacity)
                                .then(previewBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) {}
                                Text(
                                    text = "Preview",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp)) {}
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Collapsed Pill Settings
        item {
            SectionHeader(title = "Collapsed Pill Appearance", icon = Icons.Default.AspectRatio)
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Vertical Offset
                    Text(
                        text = "Vertical Notch Offset: ${settings.cutoutOffsetY} px",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = settings.cutoutOffsetY.toFloat(),
                        onValueChange = {
                            scope.launch { repository.updateCutoutOffsets(settings.cutoutOffsetX, it.toInt()) }
                        },
                        valueRange = 0f..50f,
                        modifier = Modifier.testTag("slider_offset_y")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Offset
                    Text(
                        text = "Horizontal Offset: ${settings.cutoutOffsetX} px",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = settings.cutoutOffsetX.toFloat(),
                        onValueChange = {
                            scope.launch { repository.updateCutoutOffsets(it.toInt(), settings.cutoutOffsetY) }
                        },
                        valueRange = -60f..60f,
                        modifier = Modifier.testTag("slider_offset_x")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Width & Height
                    Text(
                        text = "Pill Width: ${settings.pillWidth} dp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = settings.pillWidth.toFloat(),
                        onValueChange = {
                            scope.launch { repository.updateDimensions(it.toInt(), settings.pillHeight) }
                        },
                        valueRange = 90f..180f,
                        modifier = Modifier.testTag("slider_width")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Opacity
                    Text(
                        text = "Opacity: ${(settings.pillOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = settings.pillOpacity,
                        onValueChange = {
                            scope.launch { repository.updateOpacity(it) }
                        },
                        valueRange = 0.2f..1.0f,
                        modifier = Modifier.testTag("slider_opacity")
                    )
                    if (settings.pillOpacity < 0.7f) {
                        Text(
                            text = "Low opacity is not recommended (affects legibility over wallpapers)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Border width mode
                    Text(
                        text = "Border Width",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None", "Thin", "Filled").forEach { mode ->
                            FilterChip(
                                selected = settings.borderWidthMode == mode,
                                onClick = { scope.launch { repository.updateBorderWidthMode(mode) } },
                                label = { Text(mode) },
                                colors = FilterChipDefaults.filterChipColors()
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Mini & Alignment Settings
        item {
            SectionHeader(title = "Alignment & Scaling", icon = Icons.Default.CropFree)
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Capsule Alignment",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Left", "Center", "Right").forEach { align ->
                            FilterChip(
                                selected = settings.alignmentMode == align,
                                onClick = { scope.launch { repository.updateAlignmentMode(align) } },
                                label = { Text(align) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Scale: ${(settings.pillScale * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = settings.pillScale,
                        onValueChange = {
                            scope.launch { repository.updateScale(it) }
                        },
                        valueRange = 0.7f..1.3f,
                        modifier = Modifier.testTag("slider_scale")
                    )
                }
            }
        }

        // Section 3: Expanded Card Settings
        item {
            SectionHeader(title = "Expanded Card Appearance", icon = Icons.Default.Layers)
        }

        item {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Display Mode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Part of screen", "Floating", "Fullscreen").forEach { mode ->
                            FilterChip(
                                selected = settings.expandedMode == mode,
                                onClick = { scope.launch { repository.updateExpandedMode(mode) } },
                                label = { Text(mode) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Corner Radius: ${settings.expandedCornerRadius} dp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = settings.expandedCornerRadius.toFloat(),
                        onValueChange = {
                            scope.launch { repository.updateExpandedCornerRadius(it.toInt()) }
                        },
                        valueRange = 16f..40f,
                        modifier = Modifier.testTag("slider_corner_radius")
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
