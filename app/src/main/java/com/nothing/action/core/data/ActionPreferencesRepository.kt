package com.nothing.action.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nothing.action.core.model.ActionType
import com.nothing.action.core.model.PressType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nothing_action_prefs")

data class ActionSettings(
    val singleAction: ActionType = ActionType.TORCH,
    val doubleAction: ActionType = ActionType.GEMINI,
    val longAction: ActionType = ActionType.GLYPH,
    val singleCustomPackage: String = "",
    val doubleCustomPackage: String = "",
    val longCustomPackage: String = "",
    val isBezelAnimationEnabled: Boolean = true,
    val isServiceEnabled: Boolean = true,
    val targetKeyCode: Int = 0,
    val targetScanCode: Int = 250, // Default to Essential Key (ScanCode 250)
    val targetKeyName: String = "ESSENTIAL KEY",
    val isOnboardingCompleted: Boolean = false,
    val bezelSide: String = "RIGHT",
    val bezelYPercent: Float = 0.38f,
    val bezelButtonLength: Float = 95f, // Button length in dp
    val bezelButtonWidth: Float = 20f, // Button protrusion width in dp
    val deviceColorTheme: String = "AUTO", // AUTO, DARK, WHITE, BLUE
    val glyphFeedbackEnabled: Boolean = true
)

class ActionPreferencesRepository(private val context: Context) {

    private object Keys {
        val SINGLE_ACTION = stringPreferencesKey("single_action")
        val DOUBLE_ACTION = stringPreferencesKey("double_action")
        val LONG_ACTION = stringPreferencesKey("long_action")
        val SINGLE_PKG = stringPreferencesKey("single_pkg")
        val DOUBLE_PKG = stringPreferencesKey("double_pkg")
        val LONG_PKG = stringPreferencesKey("long_pkg")
        val BEZEL_ANIMATION = booleanPreferencesKey("bezel_animation")
        val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        val TARGET_KEY_CODE = intPreferencesKey("target_key_code")
        val TARGET_SCAN_CODE = intPreferencesKey("target_scan_code")
        val TARGET_KEY_NAME = stringPreferencesKey("target_key_name")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val BEZEL_SIDE = stringPreferencesKey("bezel_side")
        val BEZEL_Y_PERCENT = floatPreferencesKey("bezel_y_percent")
        val BEZEL_BUTTON_LENGTH = floatPreferencesKey("bezel_button_length")
        val BEZEL_BUTTON_WIDTH = floatPreferencesKey("bezel_button_width")
        val DEVICE_COLOR_THEME = stringPreferencesKey("device_color_theme")
        val GLYPH_FEEDBACK = booleanPreferencesKey("glyph_feedback")
    }

    val settingsFlow: Flow<ActionSettings> = context.dataStore.data.map { prefs ->
        ActionSettings(
            singleAction = ActionType.valueOf(prefs[Keys.SINGLE_ACTION] ?: ActionType.TORCH.name),
            doubleAction = ActionType.valueOf(prefs[Keys.DOUBLE_ACTION] ?: ActionType.GEMINI.name),
            longAction = ActionType.valueOf(prefs[Keys.LONG_ACTION] ?: ActionType.GLYPH.name),
            singleCustomPackage = prefs[Keys.SINGLE_PKG] ?: "",
            doubleCustomPackage = prefs[Keys.DOUBLE_PKG] ?: "",
            longCustomPackage = prefs[Keys.LONG_PKG] ?: "",
            isBezelAnimationEnabled = prefs[Keys.BEZEL_ANIMATION] ?: true,
            isServiceEnabled = prefs[Keys.SERVICE_ENABLED] ?: true,
            targetKeyCode = prefs[Keys.TARGET_KEY_CODE] ?: 0,
            targetScanCode = prefs[Keys.TARGET_SCAN_CODE] ?: 250,
            targetKeyName = prefs[Keys.TARGET_KEY_NAME] ?: "ESSENTIAL KEY",
            isOnboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            bezelSide = prefs[Keys.BEZEL_SIDE] ?: "RIGHT",
            bezelYPercent = prefs[Keys.BEZEL_Y_PERCENT] ?: 0.38f,
            bezelButtonLength = prefs[Keys.BEZEL_BUTTON_LENGTH] ?: 95f,
            bezelButtonWidth = prefs[Keys.BEZEL_BUTTON_WIDTH] ?: 20f,
            deviceColorTheme = prefs[Keys.DEVICE_COLOR_THEME] ?: "AUTO",
            glyphFeedbackEnabled = prefs[Keys.GLYPH_FEEDBACK] ?: true
        )
    }

    suspend fun setAction(pressType: PressType, action: ActionType, customPackage: String = "") {
        context.dataStore.edit { prefs ->
            when (pressType) {
                PressType.SINGLE -> {
                    prefs[Keys.SINGLE_ACTION] = action.name
                    if (customPackage.isNotEmpty()) prefs[Keys.SINGLE_PKG] = customPackage
                }
                PressType.DOUBLE -> {
                    prefs[Keys.DOUBLE_ACTION] = action.name
                    if (customPackage.isNotEmpty()) prefs[Keys.DOUBLE_PKG] = customPackage
                }
                PressType.LONG -> {
                    prefs[Keys.LONG_ACTION] = action.name
                    if (customPackage.isNotEmpty()) prefs[Keys.LONG_PKG] = customPackage
                }
            }
        }
    }

    suspend fun setBezelAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BEZEL_ANIMATION] = enabled }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SERVICE_ENABLED] = enabled }
    }

    suspend fun setTargetKey(keyCode: Int, scanCode: Int, keyName: String) {
        context.dataStore.edit {
            it[Keys.TARGET_KEY_CODE] = keyCode
            it[Keys.TARGET_SCAN_CODE] = scanCode
            it[Keys.TARGET_KEY_NAME] = keyName
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setBezelCalibration(side: String, yPercent: Float, buttonLength: Float, buttonWidth: Float) {
        context.dataStore.edit {
            it[Keys.BEZEL_SIDE] = side
            it[Keys.BEZEL_Y_PERCENT] = yPercent
            it[Keys.BEZEL_BUTTON_LENGTH] = buttonLength
            it[Keys.BEZEL_BUTTON_WIDTH] = buttonWidth
        }
    }

    suspend fun setDeviceColorTheme(theme: String) {
        context.dataStore.edit { it[Keys.DEVICE_COLOR_THEME] = theme }
    }

    suspend fun setGlyphFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GLYPH_FEEDBACK] = enabled }
    }
}
