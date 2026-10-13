package com.example.state

sealed class CardType {
    object Charging : CardType()
    object Media : CardType()
    object Download : CardType()
    object Message : CardType()
    object QuickToggle : CardType()
}

enum class MiniEventType {
    CHARGING_START,
    CHARGING_STOP,
    MUTE_ON,
    MUTE_OFF,
    HEADPHONES_CONNECTED,
    BLUETOOTH_CONNECTED,
    WIFI_CONNECTED,
    WIFI_DISCONNECTED,
    FLASHLIGHT_ON,
    FLASHLIGHT_OFF,
    DARK_MODE_TOGGLED,
    VOLUME_CHANGED
}

sealed class CapsuleCardData(val type: CardType) {
    data class ChargingCard(
        val level: Int,
        val isCharging: Boolean,
        val isFastCharging: Boolean = false,
        val voltage: Float = 3.8f,
        val temperatureCelsius: Float = 36.5f,
        val currentAmperes: Float = 0.45f,
        val wattageWatts: Float = 1.71f,
        val timeToFullMinutes: Int = 45
    ) : CapsuleCardData(CardType.Charging)

    data class MediaCard(
        val trackTitle: String,
        val artistName: String,
        val albumArtUri: String? = null,
        val isPlaying: Boolean = true,
        val durationMs: Long = 210000L,
        val positionMs: Long = 65000L,
        val isFavorite: Boolean = false
    ) : CapsuleCardData(CardType.Media)

    data class DownloadCard(
        val appName: String,
        val packageName: String,
        val progressPercent: Int,
        val etaSeconds: Int = 30,
        val isCompleted: Boolean = false
    ) : CapsuleCardData(CardType.Download)

    data class MessageCard(
        val senderName: String,
        val messageText: String,
        val appName: String,
        val packageName: String,
        val timestamp: Long = System.currentTimeMillis(),
        val avatarUrl: String? = null,
        val replyAction: ((String) -> Unit)? = null,
        val dismissAction: (() -> Unit)? = null
    ) : CapsuleCardData(CardType.Message)

    data class MiniQuickToggle(
        val eventType: MiniEventType,
        val title: String,
        val subtitle: String,
        val batteryPercent: Int? = null
    ) : CapsuleCardData(CardType.QuickToggle)
}
