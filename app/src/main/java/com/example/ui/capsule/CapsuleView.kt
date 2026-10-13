package com.example.ui.capsule

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.MainActivity
import com.example.data.CapsuleSettings
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder
import com.example.state.CapsuleUiState
import com.example.ui.capsule.cards.ChargingCollapsedContent
import com.example.ui.capsule.cards.ChargingExpandedContent
import com.example.ui.capsule.cards.DownloadCollapsedContent
import com.example.ui.capsule.cards.DownloadExpandedContent
import com.example.ui.capsule.cards.MediaCollapsedContent
import com.example.ui.capsule.cards.MediaExpandedContent
import com.example.ui.capsule.cards.MessageCollapsedContent
import com.example.ui.capsule.cards.MessageExpandedContent
import com.example.ui.capsule.cards.MiniCapsuleContent
import com.example.ui.theme.CapsuleMotionSpec
import com.example.ui.theme.IsletTheme
import kotlin.math.abs

@Composable
fun CapsuleOverlayRoot(
    settings: CapsuleSettings,
    modifier: Modifier = Modifier
) {
    IsletTheme {
        val uiState by CapsuleStateHolder.uiState.collectAsState()
        val context = LocalContext.current

        // Calculate alignment & offsets
        val alignBias = when (settings.alignmentMode) {
            "Left" -> Alignment.TopStart
            "Right" -> Alignment.TopEnd
            else -> Alignment.TopCenter
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .offset { IntOffset(settings.cutoutOffsetX, settings.cutoutOffsetY) },
            contentAlignment = alignBias
        ) {
            CapsuleContainer(
                uiState = uiState,
                settings = settings,
                onTap = {
                    when (settings.gestureTap) {
                        "Expand" -> CapsuleStateHolder.expandCurrent()
                        "Collapse" -> CapsuleStateHolder.collapseCurrent()
                        "Quick Actions" -> launchCompanionApp(context)
                        else -> CapsuleStateHolder.toggleExpandCollapse()
                    }
                },
                onLongPress = {
                    when (settings.gestureLongPress) {
                        "Open App" -> launchCompanionApp(context)
                        "Expand" -> CapsuleStateHolder.expandCurrent()
                        "Quick Actions" -> launchCompanionApp(context)
                        else -> Unit
                    }
                },
                onSwipeDown = {
                    if (settings.gestureSwipeDown == "Open App") {
                        launchCompanionApp(context)
                    } else {
                        CapsuleStateHolder.expandCurrent()
                    }
                },
                onSwipeUp = {
                    if (settings.gestureSwipeUp == "Dismiss") {
                        CapsuleStateHolder.dismissToIdle()
                    } else {
                        CapsuleStateHolder.collapseCurrent()
                    }
                }
            )
        }
    }
}

@Composable
fun CapsuleContainer(
    uiState: CapsuleUiState,
    settings: CapsuleSettings,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onSwipeDown: () -> Unit,
    onSwipeUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpanded = uiState is CapsuleUiState.Expanded
    val isIdle = uiState is CapsuleUiState.Idle

    val targetWidth = when {
        isExpanded -> 330.dp
        isIdle -> 72.dp
        else -> settings.pillWidth.dp
    }

    val targetHeight = when {
        isExpanded -> 210.dp
        isIdle -> 28.dp
        else -> settings.pillHeight.dp
    }

    val targetCornerRadius = when {
        isExpanded -> settings.expandedCornerRadius.dp
        else -> 50.dp
    }

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = if (isExpanded) CapsuleMotionSpec.expandDp else CapsuleMotionSpec.collapseDp,
        label = "capsule_width"
    )

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = if (isExpanded) CapsuleMotionSpec.expandDp else CapsuleMotionSpec.collapseDp,
        label = "capsule_height"
    )

    val animatedCornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        animationSpec = if (isExpanded) CapsuleMotionSpec.expandDp else CapsuleMotionSpec.collapseDp,
        label = "capsule_corner_radius"
    )

    val shape = RoundedCornerShape(animatedCornerRadius)

    // Border styling
    val borderModifier = when (settings.borderWidthMode) {
        "Thin" -> Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape)
        "Filled" -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), shape)
        else -> Modifier
    }

    // Container surface background
    val containerBg = if (isExpanded) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerHighest
    }

    Surface(
        shape = shape,
        color = containerBg.copy(alpha = settings.pillOpacity.coerceIn(0.2f, 1f)),
        tonalElevation = if (isExpanded) 8.dp else 4.dp,
        shadowElevation = if (isExpanded) 12.dp else 3.dp,
        modifier = modifier
            .width(animatedWidth)
            .height(animatedHeight)
            .scale(settings.pillScale)
            .alpha(settings.pillOpacity)
            .then(borderModifier)
            .pointerInput(uiState) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
            .pointerInput(uiState) {
                var totalDragY = 0f
                detectDragGestures(
                    onDragStart = { totalDragY = 0f },
                    onDragEnd = {
                        if (totalDragY > 30f) {
                            onSwipeDown()
                        } else if (totalDragY < -30f) {
                            onSwipeUp()
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount.y
                    }
                )
            }
            .testTag("capsule_container")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = androidx.compose.animation.core.tween(200)) togetherWith
                            fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
                },
                label = "capsule_content"
            ) { state ->
                when (state) {
                    is CapsuleUiState.Idle -> {
                        // Subtle camera cutout hug pill
                        IdlePillContent()
                    }
                    is CapsuleUiState.Collapsed -> {
                        when (val card = state.card) {
                            is CapsuleCardData.ChargingCard -> ChargingCollapsedContent(card)
                            is CapsuleCardData.MediaCard -> MediaCollapsedContent(card)
                            is CapsuleCardData.DownloadCard -> DownloadCollapsedContent(card)
                            is CapsuleCardData.MessageCard -> MessageCollapsedContent(card)
                            is CapsuleCardData.MiniQuickToggle -> MiniCapsuleContent(card)
                        }
                    }
                    is CapsuleUiState.Expanded -> {
                        when (val card = state.card) {
                            is CapsuleCardData.ChargingCard -> ChargingExpandedContent(
                                card = card,
                                onCollapse = { CapsuleStateHolder.collapseCurrent() }
                            )
                            is CapsuleCardData.MediaCard -> MediaExpandedContent(
                                card = card,
                                onPlayPause = { CapsuleStateHolder.toggleMediaPlayback() },
                                onNext = {
                                    // Next track simulation or session command
                                    CapsuleStateHolder.postCard(
                                        card.copy(trackTitle = "Horizon Echoes", artistName = "Solar Fields", positionMs = 0L)
                                    )
                                },
                                onPrevious = {
                                    CapsuleStateHolder.postCard(
                                        card.copy(trackTitle = "Midnight Resonance", artistName = "Kavinsky", positionMs = 0L)
                                    )
                                },
                                onSeek = { pos -> CapsuleStateHolder.updateMediaProgress(pos) },
                                onToggleFavorite = { CapsuleStateHolder.toggleMediaFavorite() }
                            )
                            is CapsuleCardData.DownloadCard -> DownloadExpandedContent(card)
                            is CapsuleCardData.MessageCard -> MessageExpandedContent(
                                card = card,
                                onDismiss = { CapsuleStateHolder.dismissToIdle() }
                            )
                            is CapsuleCardData.MiniQuickToggle -> MiniCapsuleContent(card)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdlePillContent() {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Camera cutout aperture placeholder
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            modifier = Modifier.size(10.dp)
        ) {}
        Spacer(modifier = Modifier.width(8.dp))
        // Subtle active dot
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f),
            modifier = Modifier.size(6.dp)
        ) {}
    }
}

private fun launchCompanionApp(context: Context) {
    try {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
