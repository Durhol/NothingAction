package com.nothing.action.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import com.nothing.action.core.model.PressType

class BezelBendOverlayManager(private val context: Context, private val isAccessibilityService: Boolean = false) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: BezelBendView? = null
    private var isAttached = false

    private var currentSide = "RIGHT"
    private var currentYPercent = 0.38f
    private var currentButtonLengthDp = 95f
    private var currentButtonWidthDp = 24f
    private var currentCornerRadiusDp = 12f
    private var currentFillColorHex = "#000000"
    private var currentStrokeEnabled = false
    private var currentStrokeWidthDp = 1.5f
    private var currentStrokeColorHex = "#333333"

    fun updateCalibration(
        side: String,
        yPercent: Float,
        buttonLengthDp: Float,
        buttonWidthDp: Float,
        cornerRadiusDp: Float = 12f,
        fillColorHex: String = "#000000",
        strokeEnabled: Boolean = false,
        strokeWidthDp: Float = 1.5f,
        strokeColorHex: String = "#333333"
    ) {
        currentSide = side
        currentYPercent = yPercent
        currentButtonLengthDp = buttonLengthDp
        currentButtonWidthDp = buttonWidthDp
        currentCornerRadiusDp = cornerRadiusDp
        currentFillColorHex = fillColorHex
        currentStrokeEnabled = strokeEnabled
        currentStrokeWidthDp = strokeWidthDp
        currentStrokeColorHex = strokeColorHex

        overlayView?.updateCalibration(
            side = side,
            buttonLengthDp = buttonLengthDp,
            buttonWidthDp = buttonWidthDp,
            cornerRadiusDp = cornerRadiusDp,
            fillColorHex = fillColorHex,
            strokeEnabled = strokeEnabled,
            strokeWidthDp = strokeWidthDp,
            strokeColorHex = strokeColorHex
        )
    }

    private fun attachOverlay() {
        if (isAttached) return
        if (!Settings.canDrawOverlays(context) && !isAccessibilityService) return

        val density = context.resources.displayMetrics.density
        val displayHeight = context.resources.displayMetrics.heightPixels

        overlayView = BezelBendView(context).apply {
            updateCalibration(
                side = currentSide,
                buttonLengthDp = currentButtonLengthDp,
                buttonWidthDp = currentButtonWidthDp,
                cornerRadiusDp = currentCornerRadiusDp,
                fillColorHex = currentFillColorHex,
                strokeEnabled = currentStrokeEnabled,
                strokeWidthDp = currentStrokeWidthDp,
                strokeColorHex = currentStrokeColorHex
            )
            onAnimationFinished = {
                detach()
            }
        }

        val overlayWidthPx = ((currentButtonWidthDp + 45f) * density).toInt().coerceAtLeast(120)
        val overlayHeightPx = ((currentButtonLengthDp + (currentCornerRadiusDp * 2f) + 40f) * density).toInt().coerceAtLeast(180)
        val isRight = currentSide.equals("RIGHT", ignoreCase = true)
        val gravity = (if (isRight) Gravity.END else Gravity.START) or Gravity.TOP
        val targetY = (displayHeight * currentYPercent - (overlayHeightPx / 2)).toInt().coerceAtLeast(0)

        // TYPE_ACCESSIBILITY_OVERLAY is a trusted system window on Android 14/15.
        // It bypasses the system's "untrusted overlay security opacity filter" on Lockscreen & Homescreen.
        @Suppress("DEPRECATION")
        val windowType = if (isAccessibilityService) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val layoutParams = WindowManager.LayoutParams(
            overlayWidthPx,
            overlayHeightPx,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            this.y = targetY
            this.dimAmount = 0.0f
            this.alpha = 1.0f
            this.flags = this.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                this.blurBehindRadius = 0
            }
        }

        try {
            windowManager.addView(overlayView, layoutParams)
            isAttached = true
        } catch (_: Exception) {
            isAttached = false
        }
    }

    fun triggerBezelAnimation(type: PressType) {
        if (!isAttached) {
            attachOverlay()
        }
        overlayView?.animatePress(type)
    }

    fun releaseLongPress() {
        overlayView?.releaseLongPress()
    }

    fun detach() {
        if (isAttached && overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (_: Exception) {}
            isAttached = false
            overlayView = null
        }
    }
}
