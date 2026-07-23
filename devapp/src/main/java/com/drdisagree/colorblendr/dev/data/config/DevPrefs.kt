package com.drdisagree.colorblendr.dev.data.config

import android.content.Context
import androidx.core.content.edit

object DevPrefs {

    private const val FILE = "dev_prefs"
    private const val KEY_ADMIN = "adminKey"
    private const val KEY_NOTIFY_ENABLED = "notifyEnabled"
    private const val KEY_NOTIFY_INTERVAL = "notifyIntervalHours"
    private const val KEY_LAST_CHECK = "lastCheck"

    const val DEFAULT_NOTIFY_INTERVAL_HOURS = 24

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun adminKey(context: Context): String =
        prefs(context).getString(KEY_ADMIN, "")?.trim() ?: ""

    fun setAdminKey(context: Context, value: String) {
        prefs(context).edit { putString(KEY_ADMIN, value.trim()) }
    }

    fun clearAdminKey(context: Context) {
        prefs(context).edit { remove(KEY_ADMIN) }
    }

    fun notifyEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NOTIFY_ENABLED, false)

    fun setNotifyEnabled(context: Context, value: Boolean) {
        prefs(context).edit { putBoolean(KEY_NOTIFY_ENABLED, value) }
    }

    fun notifyIntervalHours(context: Context): Int =
        prefs(context).getInt(KEY_NOTIFY_INTERVAL, DEFAULT_NOTIFY_INTERVAL_HOURS)

    fun setNotifyIntervalHours(context: Context, hours: Int) {
        prefs(context).edit { putInt(KEY_NOTIFY_INTERVAL, hours) }
    }

    fun lastCheck(context: Context): Long =
        prefs(context).getLong(KEY_LAST_CHECK, 0L)

    fun setLastCheck(context: Context, millis: Long) {
        prefs(context).edit { putLong(KEY_LAST_CHECK, millis) }
    }
}