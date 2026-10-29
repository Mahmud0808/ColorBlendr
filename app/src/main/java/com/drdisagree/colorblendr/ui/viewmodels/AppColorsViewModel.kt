package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Utilities.getAppColorOverrides
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setAppColorOverrides
import com.drdisagree.colorblendr.data.common.Utilities.setSelectedFabricatedApps
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.models.AppThemedColors
import com.drdisagree.colorblendr.data.models.AppColorResource
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.theme.overlay.ThemeOverlays
import com.drdisagree.colorblendr.theme.override.ColorOverride
import com.drdisagree.colorblendr.theme.override.ResourceOverrides
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.utils.app.AppColorResources
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.onPerAppSelectionChanged
import com.drdisagree.colorblendr.utils.manager.OverlayManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class AppColorsViewModel(val packageName: String) : ViewModel() {

    private val _colors = MutableStateFlow<List<AppColorResource>?>(null)
    val colors: StateFlow<List<AppColorResource>?> = _colors.asStateFlow()

    private val _themed = MutableStateFlow(AppThemedColors())
    val themed: StateFlow<AppThemedColors> = _themed.asStateFlow()

    private val _theme = MutableStateFlow<ResolvedTheme?>(null)
    val theme: StateFlow<ResolvedTheme?> = _theme.asStateFlow()

    private val _overrides = MutableStateFlow(ResourceOverrides())
    val overrides: StateFlow<ResourceOverrides> = _overrides.asStateFlow()

    private val _themingEnabled = MutableStateFlow(false)
    val themingEnabled: StateFlow<Boolean> = _themingEnabled.asStateFlow()

    private val _label = MutableStateFlow<String?>(null)
    val label: StateFlow<String?> = _label.asStateFlow()

    private val _messages = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val messages: SharedFlow<Int> = _messages.asSharedFlow()

    private val _applied = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val applied: SharedFlow<Unit> = _applied.asSharedFlow()

    private var applyJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _overrides.value = getAppColorOverrides(packageName)
            _themingEnabled.value = runCatching {
                OverlayManager.isOverlayEnabled(String.format(FABRICATED_OVERLAY_NAME_APPS, packageName)) == true
            }.getOrDefault(false)
            _label.value = AppColorResources.label(packageName)
            val theme = runCatching {
                PreviewController.previewTheme.value ?: ThemePrefs.resolve().precompute()
            }.getOrNull()
            _theme.value = theme
            if (theme != null) {
                _themed.value = runCatching {
                    AppThemedColors(
                        light = ThemeOverlays.themedColors(packageName, theme, isDarkMode = false),
                        dark = ThemeOverlays.themedColors(packageName, theme, isDarkMode = true),
                        presetNames = ThemeOverlays.presetColors(packageName, theme, isDarkMode = false).keys +
                                ThemeOverlays.presetColors(packageName, theme, isDarkMode = true).keys
                    )
                }.getOrDefault(AppThemedColors())
            }
            _colors.value = AppColorResources.load(packageName)
        }
    }

    fun setOverride(isDark: Boolean, name: String, override: ColorOverride?) {
        update(_overrides.value.with(isDark, name, override))
    }

    fun clearOverrides(isDark: Boolean) {
        update(_overrides.value.cleared(isDark))
    }

    private fun update(overrides: ResourceOverrides) {
        if (overrides == _overrides.value) return
        _overrides.value = overrides
        setAppColorOverrides(packageName, overrides)
        scheduleApply()
    }

    private fun scheduleApply() {
        applyJob?.cancel()
        applyJob = viewModelScope.launch(Dispatchers.IO) {
            delay(APPLY_DELAY)
            if (PreviewController.isPreviewActive) {
                _messages.tryEmit(R.string.preview_pending_apply_first)
                return@launch
            }
            val selected = getSelectedFabricatedApps()
            val wasSelected = selected[packageName] == true
            if (!wasSelected) {
                selected[packageName] = true
                setSelectedFabricatedApps(selected)
            }
            val applied = runCatching { OverlayManager.applyFabricatedColorsPerApp(packageName) }
                .getOrDefault(false)
            _themingEnabled.value = applied || wasSelected
            if (applied) onPerAppSelectionChanged(packageName, true)
            if (applied && isThemingEnabled()) _applied.tryEmit(Unit)
            if (!applied) {
                if (!wasSelected) {
                    selected[packageName] = false
                    setSelectedFabricatedApps(selected)
                }
                _messages.tryEmit(R.string.app_colors_apply_failed)
            }
        }
    }

    fun restartApp() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!OverlayManager.restartApp(packageName)) {
                _messages.tryEmit(R.string.app_restart_failed)
            }
        }
    }

    private companion object {
        val APPLY_DELAY = 400.milliseconds
    }
}
