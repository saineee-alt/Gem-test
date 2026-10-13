package com.example.state

sealed class CapsuleUiState {
    object Idle : CapsuleUiState()
    data class Collapsed(val card: CapsuleCardData) : CapsuleUiState()
    data class Expanded(val card: CapsuleCardData) : CapsuleUiState()

    val currentCard: CapsuleCardData?
        get() = when (this) {
            is Idle -> null
            is Collapsed -> card
            is Expanded -> card
        }

    val isExpanded: Boolean
        get() = this is Expanded

    val isIdle: Boolean
        get() = this is Idle
}
