package com.drdisagree.colorblendr.ui.compose.utils

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.drdisagree.colorblendr.data.config.Prefs

// Snapshot state backed by shared prefs: re-reads when any given key
// changes, or whole store rewritten (null key on clear, e.g. backup
// restore).
@Composable
fun <T> rememberPrefState(vararg keys: String, read: () -> T): MutableState<T> {
    val state = remember { mutableStateOf(read()) }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == null || changedKey in keys) {
                state.value = read()
            }
        }
        Prefs.registerChangeListener(listener)
        onDispose { Prefs.unregisterChangeListener(listener) }
    }
    return state
}
