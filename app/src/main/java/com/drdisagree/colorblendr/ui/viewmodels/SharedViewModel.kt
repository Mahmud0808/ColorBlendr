package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

    private val _booleanStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val booleanStates: StateFlow<Map<String, Boolean>> = _booleanStates.asStateFlow()

    fun setBooleanState(viewId: String, state: Boolean) {
        _booleanStates.value = _booleanStates.value + (viewId to state)
    }

    private val _visibilityStates = MutableStateFlow<Map<String, Int>>(emptyMap())
    val visibilityStates: StateFlow<Map<String, Int>> = _visibilityStates.asStateFlow()

    fun setVisibilityState(viewId: String, visibility: Int) {
        _visibilityStates.value = _visibilityStates.value + (viewId to visibility)
    }
}
