package com.example.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.CapsuleSettings
import com.example.ui.capsule.CapsuleOverlayRoot

class CapsuleWindowController(private val context: Context) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var lifecycleOwner: CapsuleLifecycleOwner? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isAdded = false

    fun show(settings: CapsuleSettings) {
        if (isAdded) {
            update(settings)
            return
        }

        lifecycleOwner = CapsuleLifecycleOwner().apply { onCreate() }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = settings.cutoutOffsetX
            y = settings.cutoutOffsetY
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        layoutParams = params

        val view = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)

            setContent {
                CapsuleOverlayRoot(settings = settings)
            }
        }
        composeView = view

        try {
            windowManager.addView(view, params)
            isAdded = true
        } catch (_: Exception) {
            isAdded = false
        }
    }

    fun update(settings: CapsuleSettings) {
        if (!isAdded) return
        layoutParams?.let { params ->
            params.x = settings.cutoutOffsetX
            params.y = settings.cutoutOffsetY
            try {
                windowManager.updateViewLayout(composeView, params)
            } catch (_: Exception) {}
        }

        composeView?.setContent {
            CapsuleOverlayRoot(settings = settings)
        }
    }

    fun hide() {
        if (!isAdded) return
        try {
            composeView?.let { windowManager.removeView(it) }
        } catch (_: Exception) {}
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        composeView = null
        isAdded = false
    }

    fun isShowing(): Boolean = isAdded
}
