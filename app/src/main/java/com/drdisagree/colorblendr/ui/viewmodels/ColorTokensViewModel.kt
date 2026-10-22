package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.data.common.Utilities.getTokenOverrides
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.common.Utilities.resetCustomStyleIfNotNull
import com.drdisagree.colorblendr.data.common.Utilities.setTokenOverrides
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.theme.ThemePrefs
import com.drdisagree.colorblendr.theme.override.ColorOverride
import com.drdisagree.colorblendr.theme.override.TokenOverrides
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.theme.token.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

class ColorTokensViewModel : ViewModel() {

    private val _theme = MutableStateFlow<ResolvedTheme?>(null)
    val theme: StateFlow<ResolvedTheme?> = _theme.asStateFlow()

    private val _overrides = MutableStateFlow(TokenOverrides())
    val overrides: StateFlow<TokenOverrides> = _overrides.asStateFlow()

    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            merge(PreviewController.previewTheme.map { }, RefreshCoordinator.refreshEvent)
                .collect { load() }
        }
    }

    fun setOverride(isDark: Boolean, token: Token, override: ColorOverride?) {
        update(_overrides.value.with(isDark, token, override))
    }

    fun clearOverrides(isDark: Boolean) {
        update(_overrides.value.cleared(isDark))
    }

    private fun update(overrides: TokenOverrides) {
        if (overrides == _overrides.value) return
        PreviewController.beginPreview()
        resetCustomStyleIfNotNull()
        setTokenOverrides(overrides)
        _overrides.value = overrides
        PreviewController.updatePreview()
    }

    private fun load() {
        if (isFirstRun() || isWorkMethodUnknown()) return

        _overrides.value = getTokenOverrides()
        loadJob?.cancel()
        loadJob = viewModelScope.launch(Dispatchers.Default) {
            _theme.value = PreviewController.previewTheme.value ?: ThemePrefs.resolve().precompute()
        }
    }
}
