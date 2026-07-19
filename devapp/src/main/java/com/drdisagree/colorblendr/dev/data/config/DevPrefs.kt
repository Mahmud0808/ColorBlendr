package com.drdisagree.colorblendr.dev.data.config

import android.content.Context
import androidx.core.content.edit

object DevPrefs {

    private const val FILE = "dev_prefs"
    private const val KEY_ADMIN = "adminKey"

    fun adminKey(context: Context): String =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getString(KEY_ADMIN, "")?.trim() ?: ""

    fun setAdminKey(context: Context, value: String) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit { putString(KEY_ADMIN, value.trim()) }
    }

    fun clearAdminKey(context: Context) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .edit { remove(KEY_ADMIN) }
    }
}