package com.nothing.action.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.action.core.data.ActionPreferencesRepository
import com.nothing.action.core.data.ActionSettings
import com.nothing.action.core.model.ActionType
import com.nothing.action.core.model.DeviceDetector
import com.nothing.action.core.model.DeviceInfo
import com.nothing.action.core.model.PressType
import com.nothing.action.glyph.NothingGlyphController
import com.nothing.action.overlay.BezelBendOverlayManager
import com.nothing.action.service.NothingForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val settings: ActionSettings = ActionSettings(),
    val deviceInfo: DeviceInfo = DeviceDetector.getDeviceInfo(),
    val activeDialogPressType: PressType? = null,
    val isAppPickerVisible: Boolean = false,
    val pendingAppPickerPressType: PressType? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ActionPreferencesRepository(application)
    private val glyphController by lazy { NothingGlyphController(application).apply { bind() } }
    private val overlayManager by lazy { BezelBendOverlayManager(application) }

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(settings = settings)
            }
        }
    }

    fun openActionPicker(pressType: PressType) {
        _uiState.value = _uiState.value.copy(activeDialogPressType = pressType)
    }

    fun closeActionPicker() {
        _uiState.value = _uiState.value.copy(activeDialogPressType = null)
    }

    fun onActionSelected(pressType: PressType, action: ActionType) {
        closeActionPicker()
        if (action == ActionType.APP_LAUNCH) {
            _uiState.value = _uiState.value.copy(
                isAppPickerVisible = true,
                pendingAppPickerPressType = pressType
            )
        } else {
            viewModelScope.launch {
                repository.setAction(pressType, action)
            }
        }
    }

    fun onAppSelected(packageName: String) {
        val pressType = _uiState.value.pendingAppPickerPressType
        _uiState.value = _uiState.value.copy(
            isAppPickerVisible = false,
            pendingAppPickerPressType = null
        )
        if (pressType != null) {
            viewModelScope.launch {
                repository.setAction(pressType, ActionType.APP_LAUNCH, packageName)
            }
        }
    }

    fun closeAppPicker() {
        _uiState.value = _uiState.value.copy(
            isAppPickerVisible = false,
            pendingAppPickerPressType = null
        )
    }

    fun toggleService() {
        val current = _uiState.value.settings.isServiceEnabled
        val newState = !current
        viewModelScope.launch {
            repository.setServiceEnabled(newState)
            if (newState) {
                NothingForegroundService.start(getApplication())
            }
        }
    }

    fun toggleBezelAnimation() {
        val current = _uiState.value.settings.isBezelAnimationEnabled
        viewModelScope.launch {
            repository.setBezelAnimationEnabled(!current)
        }
    }

    fun toggleColorTheme() {
        val current = _uiState.value.settings.deviceColorTheme
        val nextTheme = when (current.uppercase()) {
            "AUTO" -> "DARK"
            "DARK" -> "WHITE"
            "WHITE" -> "BLUE"
            else -> "AUTO"
        }
        viewModelScope.launch {
            repository.setDeviceColorTheme(nextTheme)
        }
    }

    fun testGlyphPulse() {
        glyphController.triggerPulse(cycles = 2, maxBrightness = 4000)
    }

    fun testBezelBend() {
        val s = _uiState.value.settings
        overlayManager.updateCalibration(
            side = s.bezelSide,
            yPercent = s.bezelYPercent,
            buttonLengthDp = s.bezelButtonLength,
            buttonWidthDp = s.bezelButtonWidth,
            cornerRadiusDp = s.bezelCornerRadius,
            fillColorHex = s.bezelFillColor,
            strokeEnabled = s.bezelStrokeEnabled,
            strokeWidthDp = s.bezelStrokeWidth,
            strokeColorHex = s.bezelStrokeColor
        )
        overlayManager.triggerBezelAnimation(PressType.DOUBLE)
    }

    override fun onCleared() {
        super.onCleared()
        glyphController.unbind()
        overlayManager.detach()
    }
}
