package com.nothing.action.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import com.nothing.action.core.model.ActionType
import com.nothing.action.glyph.NothingGlyphController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActionExecutor(
    private val context: Context,
    private val accessibilityService: AccessibilityService? = null
) {

    private val cameraManager by lazy { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private var isTorchEnabled = false
    val glyphController by lazy { NothingGlyphController(context).apply { bind() } }

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun execute(actionType: ActionType, customPackageName: String = "", triggerGlyphFlash: Boolean = true) {
        playHapticFeedback()

        if (triggerGlyphFlash) {
            glyphController.triggerPulse(cycles = 1, maxBrightness = 3200)
        }

        when (actionType) {
            ActionType.TORCH -> toggleTorch()
            ActionType.CAMERA -> launchCamera()
            ActionType.GEMINI -> launchAssistant()
            ActionType.SCREENSHOT -> captureScreenshot()
            ActionType.GLYPH -> triggerGlyphPulse()
            ActionType.APP_LAUNCH -> launchCustomApp(customPackageName)
            ActionType.NONE -> { /* No-op */ }
        }
    }

    private fun playHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(40)
            }
        } catch (_: Exception) {}
    }

    private fun toggleTorch() {
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull() ?: return

            isTorchEnabled = !isTorchEnabled
            cameraManager.setTorchMode(cameraId, isTorchEnabled)
        } catch (e: CameraAccessException) {
            Toast.makeText(context, "Flashlight unavailable", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {}
    }

    private fun launchCamera() {
        val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val fallbackIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            } catch (_: Exception) {
                Toast.makeText(context, "Camera application not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchAssistant() {
        // Priority 1: Google Voice Search Hands Free / Assistant Listening prompt
        try {
            val intent = Intent(android.speech.RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {}

        // Priority 2: Google QuickSearchBox WEB_SEARCH (HandsFreeActivity)
        try {
            val intent = Intent("android.speech.action.WEB_SEARCH").apply {
                setPackage("com.google.android.googlequicksearchbox")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {}

        // Priority 3: VOICE_ASSIST specifically targeting Google QuickSearchBox
        try {
            val intent = Intent("android.intent.action.VOICE_ASSIST").apply {
                setPackage("com.google.android.googlequicksearchbox")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {}

        // Priority 4: Standard system ACTION_ASSIST
        val assistantIntent = Intent(Intent.ACTION_ASSIST).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            context.startActivity(assistantIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Google Assistant unavailable", Toast.LENGTH_SHORT).show()
        }
    }

    private fun captureScreenshot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && accessibilityService != null) {
            accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
        } else {
            Toast.makeText(context, "Screenshot requires Accessibility Service", Toast.LENGTH_SHORT).show()
        }
    }

    private fun triggerGlyphPulse() {
        // Trigger hardware LEDs via Nothing Binder
        glyphController.triggerPulse(cycles = 3, maxBrightness = 4000)

        // Parallel tactile rhythm
        CoroutineScope(Dispatchers.Main).launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 60, 80, 120), -1))
            }
            Toast.makeText(context, "GLYPH MATRIX // PULSED", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCustomApp(packageName: String) {
        if (packageName.isBlank()) {
            Toast.makeText(context, "No app selected for action", Toast.LENGTH_SHORT).show()
            return
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
        } else {
            Toast.makeText(context, "App not found: $packageName", Toast.LENGTH_SHORT).show()
        }
    }
}
