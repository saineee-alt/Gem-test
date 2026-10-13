package com.example.signals

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.data.AppSettingsRepository
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationRelay : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var repository: AppSettingsRepository

    override fun onCreate() {
        super.onCreate()
        repository = AppSettingsRepository(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Skip our own service foreground notification to prevent feedback loop
        if (sbn.packageName == packageName) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        scope.launch {
            val settings = repository.settingsFlow.first()
            if (!settings.notificationCardEnabled) return@launch

            // Filter package allowlist
            val isAllowed = settings.notificationAllowedPackages.isEmpty() ||
                    settings.notificationAllowedPackages.contains(sbn.packageName)

            if (!isAllowed) return@launch

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
                ?: "New Notification"
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                ?: ""

            if (text.isBlank()) return@launch

            val packageManager = packageManager
            val appName = try {
                val appInfo = packageManager.getApplicationInfo(sbn.packageName, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                sbn.packageName
            }

            // Look for inline reply RemoteInput in notification actions
            var replyActionPendingIntent: android.app.PendingIntent? = null
            var replyRemoteInput: RemoteInput? = null

            notification.actions?.forEach { action ->
                action.remoteInputs?.forEach { remoteInput ->
                    if (replyRemoteInput == null && remoteInput.resultKey != null) {
                        replyRemoteInput = remoteInput
                        replyActionPendingIntent = action.actionIntent
                    }
                }
            }

            val card = CapsuleCardData.MessageCard(
                senderName = title,
                messageText = text,
                appName = appName,
                packageName = sbn.packageName,
                timestamp = sbn.postTime,
                replyAction = if (replyActionPendingIntent != null && replyRemoteInput != null) {
                    { replyText ->
                        try {
                            val intent = Intent()
                            val bundle = Bundle()
                            bundle.putCharSequence(replyRemoteInput!!.resultKey, replyText)
                            RemoteInput.addResultsToIntent(arrayOf(replyRemoteInput!!), intent, bundle)
                            replyActionPendingIntent!!.send(applicationContext, 0, intent)
                        } catch (_: Exception) {}
                    }
                } else null,
                dismissAction = {
                    try {
                        cancelNotification(sbn.key)
                    } catch (_: Exception) {}
                }
            )

            // Post to Capsule and show expanded card
            CapsuleStateHolder.postCard(card, autoExpand = true)
        }
    }
}
