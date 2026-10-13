package com.example.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppSettingsRepository
import com.example.data.CapsuleSettings
import com.example.signals.BatteryMonitor
import com.example.signals.DownloadMonitor
import com.example.signals.MediaSessionMonitor
import com.example.signals.QuickToggleMonitor
import com.example.state.CapsuleStateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CapsuleOverlayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var settingsJob: Job? = null

    private lateinit var windowController: CapsuleWindowController
    private lateinit var repository: AppSettingsRepository

    private lateinit var batteryMonitor: BatteryMonitor
    private lateinit var mediaMonitor: MediaSessionMonitor
    private lateinit var quickToggleMonitor: QuickToggleMonitor
    private lateinit var downloadMonitor: DownloadMonitor

    private var currentSettings = CapsuleSettings()

    override fun onCreate() {
        super.onCreate()
        windowController = CapsuleWindowController(this)
        repository = AppSettingsRepository(this)

        batteryMonitor = BatteryMonitor(this)
        mediaMonitor = MediaSessionMonitor(this)
        quickToggleMonitor = QuickToggleMonitor(this)
        downloadMonitor = DownloadMonitor(this)

        startForegroundNotification()

        settingsJob = scope.launch {
            repository.settingsFlow.collectLatest { settings ->
                currentSettings = settings
                CapsuleStateHolder.timeoutSeconds = settings.autoCollapseTimeoutSeconds
                if (settings.overlayEnabled) {
                    windowController.show(settings)
                } else {
                    windowController.hide()
                }
            }
        }

        batteryMonitor.start()
        mediaMonitor.start()
        quickToggleMonitor.start()
        downloadMonitor.start()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        settingsJob?.cancel()
        batteryMonitor.stop()
        mediaMonitor.stop()
        quickToggleMonitor.stop()
        downloadMonitor.stop()
        windowController.hide()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundNotification() {
        val channelId = "islet_overlay_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Islet Capsule Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the camera cutout capsule overlay active"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Islet Active")
            .setContentText("Dynamic Island capsule running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.example.islet.ACTION_STOP"
        var isRunning = false
            private set

        fun start(context: Context) {
            val intent = Intent(context, CapsuleOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CapsuleOverlayService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
