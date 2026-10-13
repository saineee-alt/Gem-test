package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.state.CapsuleCardData
import com.example.state.CapsuleStateHolder
import com.example.state.CapsuleUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Islet", appName)
    }

    @Test
    fun `test capsule state transitions`() {
        // Initially or reset to idle
        CapsuleStateHolder.dismissToIdle()
        assertTrue(CapsuleStateHolder.uiState.value is CapsuleUiState.Idle)

        // Post a charging card
        val chargingCard = CapsuleCardData.ChargingCard(
            level = 80,
            isCharging = true,
            isFastCharging = true
        )
        CapsuleStateHolder.postCard(chargingCard, autoExpand = false)
        assertTrue(CapsuleStateHolder.uiState.value is CapsuleUiState.Collapsed)

        // Expand current
        CapsuleStateHolder.expandCurrent()
        assertTrue(CapsuleStateHolder.uiState.value is CapsuleUiState.Expanded)

        // Collapse current
        CapsuleStateHolder.collapseCurrent()
        assertTrue(CapsuleStateHolder.uiState.value is CapsuleUiState.Collapsed)
    }
}
