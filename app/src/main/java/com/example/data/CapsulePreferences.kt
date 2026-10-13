package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "islet_settings")

data class CapsuleSettings(
    val overlayEnabled: Boolean = true,
    val cutoutOffsetX: Int = 0,
    val cutoutOffsetY: Int = 10,
    val pillWidth: Int = 126,
    val pillHeight: Int = 36,
    val pillScale: Float = 1.0f,
    val pillOpacity: Float = 1.0f,
    val expandedCornerRadius: Int = 28,
    val autoCollapseTimeoutSeconds: Int = 5,
    val showOnLockScreen: Boolean = false,
    val borderWidthMode: String = "None", // None, Thin, Filled
    val alignmentMode: String = "Center", // Left, Center, Right
    val expandedMode: String = "Part of screen", // Part of screen, Floating, Fullscreen
    val chargingCardEnabled: Boolean = true,
    val mediaCardEnabled: Boolean = true,
    val downloadCardEnabled: Boolean = true,
    val notificationCardEnabled: Boolean = true,
    val quickTogglesEnabled: Boolean = true,
    val gestureTap: String = "Expand",
    val gestureLongPress: String = "Open App",
    val gestureSwipeDown: String = "Expand",
    val gestureSwipeUp: String = "Dismiss",
    val notificationAllowedPackages: Set<String> = setOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.google.android.apps.messaging",
        "com.twitter.android",
        "com.slack"
    )
)

class AppSettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
        val CUTOUT_OFFSET_X = intPreferencesKey("cutout_offset_x")
        val CUTOUT_OFFSET_Y = intPreferencesKey("cutout_offset_y")
        val PILL_WIDTH = intPreferencesKey("pill_width")
        val PILL_HEIGHT = intPreferencesKey("pill_height")
        val PILL_SCALE = floatPreferencesKey("pill_scale")
        val PILL_OPACITY = floatPreferencesKey("pill_opacity")
        val EXPANDED_CORNER_RADIUS = intPreferencesKey("expanded_corner_radius")
        val AUTO_COLLAPSE_TIMEOUT = intPreferencesKey("auto_collapse_timeout")
        val SHOW_ON_LOCK_SCREEN = booleanPreferencesKey("show_on_lock_screen")
        val BORDER_WIDTH_MODE = stringPreferencesKey("border_width_mode")
        val ALIGNMENT_MODE = stringPreferencesKey("alignment_mode")
        val EXPANDED_MODE = stringPreferencesKey("expanded_mode")
        val CHARGING_CARD_ENABLED = booleanPreferencesKey("charging_card_enabled")
        val MEDIA_CARD_ENABLED = booleanPreferencesKey("media_card_enabled")
        val DOWNLOAD_CARD_ENABLED = booleanPreferencesKey("download_card_enabled")
        val NOTIFICATION_CARD_ENABLED = booleanPreferencesKey("notification_card_enabled")
        val QUICK_TOGGLES_ENABLED = booleanPreferencesKey("quick_toggles_enabled")
        val GESTURE_TAP = stringPreferencesKey("gesture_tap")
        val GESTURE_LONG_PRESS = stringPreferencesKey("gesture_long_press")
        val GESTURE_SWIPE_DOWN = stringPreferencesKey("gesture_swipe_down")
        val GESTURE_SWIPE_UP = stringPreferencesKey("gesture_swipe_up")
        val NOTIFICATION_PACKAGES = stringSetPreferencesKey("notification_packages")
    }

    val settingsFlow: Flow<CapsuleSettings> = context.dataStore.data.map { pref ->
        CapsuleSettings(
            overlayEnabled = pref[PreferencesKeys.OVERLAY_ENABLED] ?: true,
            cutoutOffsetX = pref[PreferencesKeys.CUTOUT_OFFSET_X] ?: 0,
            cutoutOffsetY = pref[PreferencesKeys.CUTOUT_OFFSET_Y] ?: 10,
            pillWidth = pref[PreferencesKeys.PILL_WIDTH] ?: 126,
            pillHeight = pref[PreferencesKeys.PILL_HEIGHT] ?: 36,
            pillScale = pref[PreferencesKeys.PILL_SCALE] ?: 1.0f,
            pillOpacity = pref[PreferencesKeys.PILL_OPACITY] ?: 1.0f,
            expandedCornerRadius = pref[PreferencesKeys.EXPANDED_CORNER_RADIUS] ?: 28,
            autoCollapseTimeoutSeconds = pref[PreferencesKeys.AUTO_COLLAPSE_TIMEOUT] ?: 5,
            showOnLockScreen = pref[PreferencesKeys.SHOW_ON_LOCK_SCREEN] ?: false,
            borderWidthMode = pref[PreferencesKeys.BORDER_WIDTH_MODE] ?: "None",
            alignmentMode = pref[PreferencesKeys.ALIGNMENT_MODE] ?: "Center",
            expandedMode = pref[PreferencesKeys.EXPANDED_MODE] ?: "Part of screen",
            chargingCardEnabled = pref[PreferencesKeys.CHARGING_CARD_ENABLED] ?: true,
            mediaCardEnabled = pref[PreferencesKeys.MEDIA_CARD_ENABLED] ?: true,
            downloadCardEnabled = pref[PreferencesKeys.DOWNLOAD_CARD_ENABLED] ?: true,
            notificationCardEnabled = pref[PreferencesKeys.NOTIFICATION_CARD_ENABLED] ?: true,
            quickTogglesEnabled = pref[PreferencesKeys.QUICK_TOGGLES_ENABLED] ?: true,
            gestureTap = pref[PreferencesKeys.GESTURE_TAP] ?: "Expand",
            gestureLongPress = pref[PreferencesKeys.GESTURE_LONG_PRESS] ?: "Open App",
            gestureSwipeDown = pref[PreferencesKeys.GESTURE_SWIPE_DOWN] ?: "Expand",
            gestureSwipeUp = pref[PreferencesKeys.GESTURE_SWIPE_UP] ?: "Dismiss",
            notificationAllowedPackages = pref[PreferencesKeys.NOTIFICATION_PACKAGES] ?: setOf(
                "com.whatsapp",
                "org.telegram.messenger",
                "com.google.android.apps.messaging",
                "com.twitter.android",
                "com.slack"
            )
        )
    }

    suspend fun updateOverlayEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.OVERLAY_ENABLED] = enabled }
    }

    suspend fun updateCutoutOffsets(offsetX: Int, offsetY: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.CUTOUT_OFFSET_X] = offsetX
            it[PreferencesKeys.CUTOUT_OFFSET_Y] = offsetY
        }
    }

    suspend fun updateDimensions(width: Int, height: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.PILL_WIDTH] = width
            it[PreferencesKeys.PILL_HEIGHT] = height
        }
    }

    suspend fun updateScale(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.PILL_SCALE] = scale }
    }

    suspend fun updateOpacity(opacity: Float) {
        context.dataStore.edit { it[PreferencesKeys.PILL_OPACITY] = opacity }
    }

    suspend fun updateExpandedCornerRadius(radius: Int) {
        context.dataStore.edit { it[PreferencesKeys.EXPANDED_CORNER_RADIUS] = radius }
    }

    suspend fun updateAutoCollapseTimeout(seconds: Int) {
        context.dataStore.edit { it[PreferencesKeys.AUTO_COLLAPSE_TIMEOUT] = seconds }
    }

    suspend fun updateShowOnLockScreen(show: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_ON_LOCK_SCREEN] = show }
    }

    suspend fun updateBorderWidthMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.BORDER_WIDTH_MODE] = mode }
    }

    suspend fun updateAlignmentMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.ALIGNMENT_MODE] = mode }
    }

    suspend fun updateExpandedMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.EXPANDED_MODE] = mode }
    }

    suspend fun updateCardEnabled(cardType: String, enabled: Boolean) {
        context.dataStore.edit {
            when (cardType) {
                "charging" -> it[PreferencesKeys.CHARGING_CARD_ENABLED] = enabled
                "media" -> it[PreferencesKeys.MEDIA_CARD_ENABLED] = enabled
                "download" -> it[PreferencesKeys.DOWNLOAD_CARD_ENABLED] = enabled
                "notification" -> it[PreferencesKeys.NOTIFICATION_CARD_ENABLED] = enabled
                "quick_toggles" -> it[PreferencesKeys.QUICK_TOGGLES_ENABLED] = enabled
            }
        }
    }

    suspend fun updateGesture(gestureType: String, action: String) {
        context.dataStore.edit {
            when (gestureType) {
                "tap" -> it[PreferencesKeys.GESTURE_TAP] = action
                "long_press" -> it[PreferencesKeys.GESTURE_LONG_PRESS] = action
                "swipe_down" -> it[PreferencesKeys.GESTURE_SWIPE_DOWN] = action
                "swipe_up" -> it[PreferencesKeys.GESTURE_SWIPE_UP] = action
            }
        }
    }

    suspend fun toggleNotificationPackage(packageName: String, allowed: Boolean) {
        context.dataStore.edit { pref ->
            val current = pref[PreferencesKeys.NOTIFICATION_PACKAGES]?.toMutableSet()
                ?: mutableSetOf("com.whatsapp", "org.telegram.messenger", "com.google.android.apps.messaging")
            if (allowed) {
                current.add(packageName)
            } else {
                current.remove(packageName)
            }
            pref[PreferencesKeys.NOTIFICATION_PACKAGES] = current
        }
    }

    suspend fun resetDefaults() {
        context.dataStore.edit { it.clear() }
    }
}
