package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.colorblendr.utils.ai.AiPrefs
import com.drdisagree.colorblendr.utils.ai.AiThemeHolder
import com.drdisagree.colorblendr.utils.ai.GeminiClient
import com.drdisagree.colorblendr.utils.ai.GeminiError
import com.drdisagree.colorblendr.utils.ai.GeminiResult
import com.drdisagree.colorblendr.utils.ai.KeyCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiThemeViewModel : ViewModel() {

    private val _apiKey = MutableStateFlow(AiPrefs.apiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _verifying = MutableStateFlow(false)
    val verifying: StateFlow<Boolean> = _verifying.asStateFlow()

    private val _keyError = MutableStateFlow<KeyCheckResult?>(null)
    val keyError: StateFlow<KeyCheckResult?> = _keyError.asStateFlow()

    private val _generating = MutableStateFlow(false)
    val generating: StateFlow<Boolean> = _generating.asStateFlow()

    private val _generateError = MutableStateFlow<GeminiError?>(null)
    val generateError: StateFlow<GeminiError?> = _generateError.asStateFlow()

    fun saveKey(rawKey: String) {
        val key = rawKey.filterNot(Char::isISOControl).trim()

        if (key.isEmpty() || _verifying.value) return

        _verifying.value = true
        _keyError.value = null

        viewModelScope.launch {
            val result = GeminiClient.validateKey(key)
            _verifying.value = false

            if (result == KeyCheckResult.VALID) {
                AiPrefs.setApiKey(key)
                _apiKey.value = key
            } else {
                _keyError.value = result
            }
        }
    }

    fun clearKeyError() {
        _keyError.value = null
    }

    fun resetKey() {
        AiPrefs.clearApiKey()
        AiThemeHolder.clear()
        _apiKey.value = ""
    }

    fun generate(prompt: String) {
        if (prompt.isBlank() || _generating.value) return

        _generating.value = true
        _generateError.value = null

        viewModelScope.launch {
            when (val result = GeminiClient.generateTheme(_apiKey.value, prompt)) {
                is GeminiResult.Success -> AiThemeHolder.push(result.theme)
                is GeminiResult.Failure -> _generateError.value = result.error
            }
            _generating.value = false
        }
    }
}