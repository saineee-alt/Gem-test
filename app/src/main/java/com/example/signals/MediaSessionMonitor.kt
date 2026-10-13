package com.example.signals

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder

class MediaSessionMonitor(private val context: Context) {
    private var activeController: MediaController? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val callback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromActiveController()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromActiveController()
        }

        override fun onSessionDestroyed() {
            activeController = null
        }
    }

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        val newController = controllers?.firstOrNull()
        if (newController != activeController) {
            activeController?.unregisterCallback(callback)
            activeController = newController
            activeController?.registerCallback(callback, mainHandler)
            updateFromActiveController()
        }
    }

    fun start() {
        try {
            val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val notificationListener = ComponentName(context, NotificationRelay::class.java)
            sessionManager?.addOnActiveSessionsChangedListener(sessionsListener, notificationListener)
            val controllers = sessionManager?.getActiveSessions(notificationListener)
            activeController = controllers?.firstOrNull()
            activeController?.registerCallback(callback, mainHandler)
            updateFromActiveController()
        } catch (_: SecurityException) {
            // Notification listener access is required for getActiveSessions.
            // Will fallback or wait until permission is granted.
        }
    }

    fun stop() {
        try {
            val sessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            sessionManager?.removeOnActiveSessionsChangedListener(sessionsListener)
            activeController?.unregisterCallback(callback)
            activeController = null
        } catch (_: Exception) {}
    }

    private fun updateFromActiveController() {
        val controller = activeController ?: return
        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Now Playing"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "Unknown Artist"
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 180000L
        val position = playbackState?.position ?: 0L

        val mediaCard = CapsuleCardData.MediaCard(
            trackTitle = title,
            artistName = artist,
            albumArtUri = null,
            isPlaying = isPlaying,
            durationMs = if (duration > 0) duration else 210000L,
            positionMs = position,
            isFavorite = false
        )

        // Only auto-show if actively playing
        if (isPlaying) {
            CapsuleStateHolder.postCard(mediaCard, autoExpand = false)
        }
    }

    fun sendPlayPause() {
        activeController?.let { controller ->
            val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
            if (isPlaying) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        } ?: run {
            CapsuleStateHolder.toggleMediaPlayback()
        }
    }

    fun sendNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun sendPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun sendSeekTo(positionMs: Long) {
        activeController?.transportControls?.seekTo(positionMs) ?: run {
            CapsuleStateHolder.updateMediaProgress(positionMs)
        }
    }
}
