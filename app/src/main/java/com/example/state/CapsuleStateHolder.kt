package com.example.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object CapsuleStateHolder {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var autoCollapseJob: Job? = null
    private var autoDismissJob: Job? = null

    private val _uiState = MutableStateFlow<CapsuleUiState>(CapsuleUiState.Idle)
    val uiState: StateFlow<CapsuleUiState> = _uiState.asStateFlow()

    // Flag indicating user is currently typing inline reply so we don't auto-collapse
    var isReplyingInline: Boolean = false

    var timeoutSeconds: Int = 5

    fun postCard(card: CapsuleCardData, autoExpand: Boolean = false) {
        autoCollapseJob?.cancel()
        autoDismissJob?.cancel()

        if (autoExpand) {
            _uiState.value = CapsuleUiState.Expanded(card)
            scheduleAutoCollapse(timeoutSeconds)
        } else {
            _uiState.value = CapsuleUiState.Collapsed(card)
            if (card is CapsuleCardData.MiniQuickToggle) {
                scheduleAutoDismiss(2)
            }
        }
    }

    fun expandCurrent() {
        val current = _uiState.value
        if (current is CapsuleUiState.Collapsed) {
            autoCollapseJob?.cancel()
            autoDismissJob?.cancel()
            _uiState.value = CapsuleUiState.Expanded(current.card)
            if (current.card !is CapsuleCardData.MediaCard) {
                scheduleAutoCollapse(timeoutSeconds)
            }
        }
    }

    fun collapseCurrent() {
        val current = _uiState.value
        if (current is CapsuleUiState.Expanded) {
            autoCollapseJob?.cancel()
            _uiState.value = CapsuleUiState.Collapsed(current.card)
            if (current.card is CapsuleCardData.MiniQuickToggle) {
                scheduleAutoDismiss(2)
            }
        }
    }

    fun toggleExpandCollapse() {
        when (_uiState.value) {
            is CapsuleUiState.Collapsed -> expandCurrent()
            is CapsuleUiState.Expanded -> collapseCurrent()
            is CapsuleUiState.Idle -> {
                // If idle, show sample charging or media card to give user immediate feedback
                postCard(
                    CapsuleCardData.ChargingCard(
                        level = 82,
                        isCharging = true,
                        isFastCharging = true,
                        voltage = 4.1f,
                        temperatureCelsius = 31.4f,
                        currentAmperes = 1.62f,
                        wattageWatts = 6.64f,
                        timeToFullMinutes = 24
                    ),
                    autoExpand = true
                )
            }
        }
    }

    fun dismissToIdle() {
        autoCollapseJob?.cancel()
        autoDismissJob?.cancel()
        _uiState.value = CapsuleUiState.Idle
    }

    fun updateMediaProgress(positionMs: Long) {
        val current = _uiState.value
        val card = current.currentCard
        if (card is CapsuleCardData.MediaCard) {
            val updated = card.copy(positionMs = positionMs)
            when (current) {
                is CapsuleUiState.Expanded -> _uiState.value = CapsuleUiState.Expanded(updated)
                is CapsuleUiState.Collapsed -> _uiState.value = CapsuleUiState.Collapsed(updated)
                is CapsuleUiState.Idle -> Unit
            }
        }
    }

    fun toggleMediaPlayback() {
        val current = _uiState.value
        val card = current.currentCard
        if (card is CapsuleCardData.MediaCard) {
            val updated = card.copy(isPlaying = !card.isPlaying)
            when (current) {
                is CapsuleUiState.Expanded -> _uiState.value = CapsuleUiState.Expanded(updated)
                is CapsuleUiState.Collapsed -> _uiState.value = CapsuleUiState.Collapsed(updated)
                is CapsuleUiState.Idle -> Unit
            }
        }
    }

    fun toggleMediaFavorite() {
        val current = _uiState.value
        val card = current.currentCard
        if (card is CapsuleCardData.MediaCard) {
            val updated = card.copy(isFavorite = !card.isFavorite)
            if (current is CapsuleUiState.Expanded) {
                _uiState.value = CapsuleUiState.Expanded(updated)
            }
        }
    }

    private fun scheduleAutoCollapse(seconds: Int) {
        autoCollapseJob?.cancel()
        autoCollapseJob = scope.launch {
            delay(seconds * 1000L)
            if (!isReplyingInline) {
                val current = _uiState.value
                if (current is CapsuleUiState.Expanded && current.card !is CapsuleCardData.MediaCard) {
                    _uiState.value = CapsuleUiState.Collapsed(current.card)
                }
            }
        }
    }

    private fun scheduleAutoDismiss(seconds: Int) {
        autoDismissJob?.cancel()
        autoDismissJob = scope.launch {
            delay(seconds * 1000L)
            _uiState.value = CapsuleUiState.Idle
        }
    }
}
