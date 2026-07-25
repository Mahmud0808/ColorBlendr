package com.drdisagree.colorblendr.utils.ai

import android.content.Context
import androidx.core.content.edit
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext

object AiPrefs {

    private const val FILE = "ai_prefs"
    private const val KEY_API = "geminiApiKey"

    private fun prefs() =
        appContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun apiKey(): String = prefs().getString(KEY_API, "")?.trim() ?: ""

    fun setApiKey(value: String) {
        prefs().edit { putString(KEY_API, value.trim()) }
    }

    fun clearApiKey() {
        prefs().edit { remove(KEY_API) }
    }
}