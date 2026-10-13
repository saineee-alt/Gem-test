package com.example.signals

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.wifi.WifiManager
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder
import com.example.state.MiniEventType

class QuickToggleMonitor(private val context: Context) {
    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent == null) return

            when (intent.action) {
                AudioManager.RINGER_MODE_CHANGED_ACTION -> {
                    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                    when (audioManager?.ringerMode) {
                        AudioManager.RINGER_MODE_SILENT -> {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MiniQuickToggle(
                                    eventType = MiniEventType.MUTE_ON,
                                    title = "Mute On",
                                    subtitle = "Silent mode active"
                                )
                            )
                        }
                        AudioManager.RINGER_MODE_VIBRATE -> {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MiniQuickToggle(
                                    eventType = MiniEventType.MUTE_ON,
                                    title = "Vibrate",
                                    subtitle = "Haptics only"
                                )
                            )
                        }
                        AudioManager.RINGER_MODE_NORMAL -> {
                            CapsuleStateHolder.postCard(
                                CapsuleCardData.MiniQuickToggle(
                                    eventType = MiniEventType.MUTE_OFF,
                                    title = "Ring Mode",
                                    subtitle = "Sound enabled"
                                )
                            )
                        }
                    }
                }

                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val name = try {
                        device?.name ?: "Bluetooth Device"
                    } catch (_: SecurityException) {
                        "Bluetooth Device"
                    }

                    CapsuleStateHolder.postCard(
                        CapsuleCardData.MiniQuickToggle(
                            eventType = MiniEventType.BLUETOOTH_CONNECTED,
                            title = name,
                            subtitle = "Connected",
                            batteryPercent = 85
                        )
                    )
                }

                WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                    if (state == WifiManager.WIFI_STATE_ENABLED) {
                        CapsuleStateHolder.postCard(
                            CapsuleCardData.MiniQuickToggle(
                                eventType = MiniEventType.WIFI_CONNECTED,
                                title = "Wi-Fi Enabled",
                                subtitle = "Searching networks"
                            )
                        )
                    }
                }
            }
        }
    }

    fun start() {
        if (isRegistered) return
        val filter = IntentFilter().apply {
            addAction(AudioManager.RINGER_MODE_CHANGED_ACTION)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, filter)
        isRegistered = true
    }

    fun stop() {
        if (!isRegistered) return
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
        isRegistered = false
    }
}
