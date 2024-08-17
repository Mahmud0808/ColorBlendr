package com.drdisagree.colorblendr.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    private val booleanStates: MutableLiveData<MutableMap<String, Boolean>> = MutableLiveData()

    fun getBooleanStates(): LiveData<MutableMap<String, Boolean>> {
        return booleanStates
    }

    fun setBooleanState(viewId: String, state: Boolean) {
        val currentStates: MutableMap<String, Boolean> = booleanStates.getValue() ?: HashMap()
        currentStates[viewId] = state
        booleanStates.value = currentStates
    }

    private val visibilityStates: MutableLiveData<MutableMap<String, Int>> = MutableLiveData()

    fun getVisibilityStates(): LiveData<MutableMap<String, Int>> {
        return visibilityStates
    }

    fun setVisibilityState(viewId: String, visibility: Int) {
        val currentStates: MutableMap<String, Int> = visibilityStates.getValue() ?: HashMap()
        currentStates[viewId] = visibility
        visibilityStates.value = currentStates
    }
}
