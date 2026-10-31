package com.nothing.action.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.nothing.action.core.data.ActionPreferencesRepository
import com.nothing.action.core.data.ActionSettings
import com.nothing.action.core.model.PressType
import com.nothing.action.overlay.BezelBendOverlayManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NothingAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_KEY_CALIBRATION_EVENT = "com.nothing.action.KEY_CALIBRATION_EVENT"
        const val EXTRA_KEY_CODE = "extra_key_code"
        const val EXTRA_KEY_SCAN_CODE = "extra_key_scan_code"
        const val EXTRA_KEY_NAME = "extra_key_name"
        var isCalibratingKey = false
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val repository by lazy { ActionPreferencesRepository(this) }
    private lateinit var overlayManager: BezelBendOverlayManager
    private lateinit var actionExecutor: ActionExecutor

    private var currentSettings = ActionSettings()
    private val mainHandler = Handler(Looper.getMainLooper())

    private var clickCount = 0
    private var isLongPressActive = false
    private var pressStartTime = 0L

    private val doubleClickWindowMs = 280L
    private val longPressThresholdMs = 500L

    private var singleClickTimeoutRunnable: Runnable? = null
    private var longPressTimeoutRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = BezelBendOverlayManager(this, isAccessibilityService = true)
        actionExecutor = ActionExecutor(this, this)

        serviceScope.launch {
            repository.settingsFlow.collectLatest { settings ->
                currentSettings = settings
                overlayManager.updateCalibration(
                    side = settings.bezelSide,
                    yPercent = settings.bezelYPercent,
                    buttonLengthDp = settings.bezelButtonLength,
                    buttonWidthDp = settings.bezelButtonWidth
                )
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // If in Calibration Mode, intercept any hardware key and read scanCode AND keyCode!
        if (isCalibratingKey) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val scanCode = event.scanCode
                val keyCode = event.keyCode
                val keyName = when {
                    scanCode == 250 -> "ESSENTIAL KEY"
                    keyCode == KeyEvent.KEYCODE_ASSIST -> "ASSIST KEY"
                    scanCode > 0 -> "KEY (SCANCODE $scanCode)"
                    keyCode != KeyEvent.KEYCODE_UNKNOWN -> KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
                    else -> "KEY #$scanCode"
                }

                val broadcastIntent = Intent(ACTION_KEY_CALIBRATION_EVENT).apply {
                    setPackage(packageName)
                    putExtra(EXTRA_KEY_CODE, keyCode)
                    putExtra(EXTRA_KEY_SCAN_CODE, scanCode)
                    putExtra(EXTRA_KEY_NAME, keyName)
                }
                sendBroadcast(broadcastIntent)
                return true
            }
            return super.onKeyEvent(event)
        }

        if (!currentSettings.isServiceEnabled) {
            return super.onKeyEvent(event)
        }

        // Match targeted physical key (ScanCode 250 or configured key)
        val matchesScanCode = currentSettings.targetScanCode > 0 && event.scanCode == currentSettings.targetScanCode
        val matchesKeyCode = currentSettings.targetKeyCode > 0 && event.keyCode == currentSettings.targetKeyCode
        val isEssential = event.scanCode == 250 || (currentSettings.targetScanCode == 250 && event.scanCode == 250)

        if (!matchesScanCode && !matchesKeyCode && !isEssential) {
            return super.onKeyEvent(event)
        }

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    pressStartTime = System.currentTimeMillis()
                    isLongPressActive = false

                    // Schedule long press check
                    longPressTimeoutRunnable = Runnable {
                        isLongPressActive = true
                        onPressDetected(PressType.LONG)
                    }
                    mainHandler.postDelayed(longPressTimeoutRunnable!!, longPressThresholdMs)
                }
                return true
            }

            KeyEvent.ACTION_UP -> {
                // Cancel scheduled long press if released early
                longPressTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }

                if (isLongPressActive) {
                    if (currentSettings.isBezelAnimationEnabled) {
                        overlayManager.releaseLongPress()
                    }
                    return true
                }

                val pressDuration = System.currentTimeMillis() - pressStartTime
                if (pressDuration < longPressThresholdMs) {
                    clickCount++

                    if (clickCount == 1) {
                        singleClickTimeoutRunnable = Runnable {
                            if (clickCount == 1) {
                                onPressDetected(PressType.SINGLE)
                            }
                            clickCount = 0
                        }
                        mainHandler.postDelayed(singleClickTimeoutRunnable!!, doubleClickWindowMs)
                    } else if (clickCount >= 2) {
                        singleClickTimeoutRunnable?.let { mainHandler.removeCallbacks(it) }
                        clickCount = 0
                        onPressDetected(PressType.DOUBLE)
                    }
                }
                return true
            }
        }

        return super.onKeyEvent(event)
    }

    private fun onPressDetected(type: PressType) {
        if (currentSettings.isBezelAnimationEnabled) {
            overlayManager.triggerBezelAnimation(type)
        }

        val triggerGlyph = currentSettings.glyphFeedbackEnabled
        when (type) {
            PressType.SINGLE -> {
                actionExecutor.execute(
                    currentSettings.singleAction,
                    currentSettings.singleCustomPackage,
                    triggerGlyphFlash = triggerGlyph
                )
            }
            PressType.DOUBLE -> {
                actionExecutor.execute(
                    currentSettings.doubleAction,
                    currentSettings.doubleCustomPackage,
                    triggerGlyphFlash = triggerGlyph
                )
            }
            PressType.LONG -> {
                actionExecutor.execute(
                    currentSettings.longAction,
                    currentSettings.longCustomPackage,
                    triggerGlyphFlash = triggerGlyph
                )
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used
    }

    override fun onInterrupt() {
        overlayManager.detach()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
        serviceScope.cancel()
        overlayManager.detach()
        actionExecutor.glyphController.unbind()
    }
}
