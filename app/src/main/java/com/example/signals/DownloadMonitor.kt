package com.example.signals

import android.content.Context
import android.content.pm.PackageInstaller
import android.os.Handler
import android.os.Looper
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder

class DownloadMonitor(private val context: Context) {
    private var installer: PackageInstaller? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val sessionCallback = object : PackageInstaller.SessionCallback() {
        override fun onCreated(sessionId: Int) {}
        override fun onBadgingChanged(sessionId: Int) {}
        override fun onActiveChanged(sessionId: Int, active: Boolean) {}

        override fun onProgressChanged(sessionId: Int, progress: Float) {
            val sessionInfo = installer?.getSessionInfo(sessionId) ?: return
            val appLabel = sessionInfo.appLabel?.toString() ?: "App Update"
            val pkg = sessionInfo.appPackageName ?: "com.android.vending"
            val percent = (progress * 100).toInt().coerceIn(0, 100)

            val card = CapsuleCardData.DownloadCard(
                appName = appLabel,
                packageName = pkg,
                progressPercent = percent,
                etaSeconds = ((100 - percent) * 0.4f).toInt().coerceAtLeast(1),
                isCompleted = percent >= 100
            )
            CapsuleStateHolder.postCard(card, autoExpand = percent < 100)
        }

        override fun onFinished(sessionId: Int, success: Boolean) {
            val sessionInfo = installer?.getSessionInfo(sessionId)
            val appLabel = sessionInfo?.appLabel?.toString() ?: "App Installed"
            if (success) {
                CapsuleStateHolder.postCard(
                    CapsuleCardData.DownloadCard(
                        appName = appLabel,
                        packageName = sessionInfo?.appPackageName ?: "",
                        progressPercent = 100,
                        etaSeconds = 0,
                        isCompleted = true
                    ),
                    autoExpand = false
                )
            }
        }
    }

    fun start() {
        try {
            installer = context.packageManager.packageInstaller
            installer?.registerSessionCallback(sessionCallback, mainHandler)
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            installer?.unregisterSessionCallback(sessionCallback)
            installer = null
        } catch (_: Exception) {}
    }
}
