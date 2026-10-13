package com.example.signals

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder
import com.example.state.MiniEventType
import kotlin.math.abs

class BatteryMonitor(private val context: Context) {
    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return

            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    // Charger plugged in -> expand charging card or show mini capsule
                    val card = readBatteryStats(intent)
                    CapsuleStateHolder.postCard(card, autoExpand = true)
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    CapsuleStateHolder.postCard(
                        CapsuleCardData.MiniQuickToggle(
                            eventType = MiniEventType.CHARGING_STOP,
                            title = "Charger Disconnected",
                            subtitle = "Running on battery"
                        )
                    )
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    // Update stats if charging
                    val isCharging = isBatteryCharging(intent)
                    if (isCharging) {
                        val currentCard = CapsuleStateHolder.uiState.value.currentCard
                        if (currentCard is CapsuleCardData.ChargingCard) {
                            val updated = readBatteryStats(intent)
                            CapsuleStateHolder.postCard(updated, autoExpand = CapsuleStateHolder.uiState.value.isExpanded)
                        }
                    }
                }
            }
        }
    }

    fun start() {
        if (isRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
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

    private fun isBatteryCharging(intent: Intent): Boolean {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun readBatteryStats(intent: Intent? = null): CapsuleCardData.ChargingCard {
        val batteryIntent = intent ?: context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 75) ?: 75
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = (level * 100) / (if (scale > 0) scale else 100)

        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val voltageMv = batteryIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 3850) ?: 3850
        val voltageVolts = voltageMv / 1000f

        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320) ?: 320
        val tempCelsius = tempTenths / 10f

        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val rawCurrentUa = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) ?: 450000
        val currentAmperes = if (rawCurrentUa != 0 && rawCurrentUa != Integer.MIN_VALUE) {
            abs(rawCurrentUa) / 1_000_000f
        } else {
            0.45f
        }

        val wattage = voltageVolts * currentAmperes
        val isFast = wattage > 15f || currentAmperes > 2.0f

        // Estimate time to full in minutes
        val remainingPercent = 100 - percent
        val timeToFull = if (isCharging && remainingPercent > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val computeRemaining = batteryManager?.computeChargeTimeRemaining() ?: -1L
                if (computeRemaining > 0) {
                    (computeRemaining / 60000L).toInt()
                } else {
                    (remainingPercent * 1.2f).toInt()
                }
            } else {
                (remainingPercent * 1.2f).toInt()
            }
        } else {
            0
        }

        return CapsuleCardData.ChargingCard(
            level = percent,
            isCharging = isCharging,
            isFastCharging = isFast,
            voltage = ((voltageVolts * 10).toInt() / 10f),
            temperatureCelsius = ((tempCelsius * 10).toInt() / 10f),
            currentAmperes = ((currentAmperes * 100).toInt() / 100f),
            wattageWatts = ((wattage * 10).toInt() / 10f),
            timeToFullMinutes = timeToFull
        )
    }
}
