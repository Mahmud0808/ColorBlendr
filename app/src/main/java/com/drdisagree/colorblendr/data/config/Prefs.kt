package com.drdisagree.colorblendr.data.config

import android.content.Context
import android.content.SharedPreferences
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant
import com.google.gson.reflect.TypeToken

@Suppress("unused")
object Prefs {
    private val TAG: String = Prefs::class.java.simpleName

    private var prefs: SharedPreferences = appContext
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(Constant.SHARED_PREFS, Context.MODE_PRIVATE)

    private var editor = prefs.edit()

    val preferenceEditor: SharedPreferences.Editor
        get() = editor

    fun putBoolean(key: String, `val`: Boolean) = editor.putBoolean(key, `val`).apply()

    fun putInt(key: String, `val`: Int) = editor.putInt(key, `val`).apply()

    fun putLong(key: String, `val`: Long) = editor.putLong(key, `val`).apply()

    fun putFloat(key: String, `val`: Float) = editor.putFloat(key, `val`).apply()

    fun putString(key: String, `val`: String) = editor.putString(key, `val`).apply()

    fun getBoolean(key: String): Boolean = prefs.getBoolean(key, false)

    fun getBoolean(key: String, defValue: Boolean): Boolean = prefs.getBoolean(key, defValue)

    fun getInt(key: String): Int = prefs.getInt(key, 0)

    fun getInt(key: String, defValue: Int): Int = prefs.getInt(key, defValue)

    fun getLong(key: String): Long = prefs.getLong(key, 0)

    fun getLong(key: String, defValue: Long): Long = prefs.getLong(key, defValue)

    fun getFloat(key: String): Float = prefs.getFloat(key, 0f)

    fun getFloat(key: String, defValue: Float): Float = prefs.getFloat(key, defValue)

    fun getString(key: String): String? = prefs.getString(key, null)

    fun getString(key: String, defValue: String?): String? = prefs.getString(key, defValue)

    fun getAllPrefs(): MutableMap<String, *> = prefs.all

    fun getAllPrefsAsGson(): String = Constant.GSON.toJson(getAllPrefs() as Map<String, *>)

    fun Map<String, Any>.toGsonString(): String = Constant.GSON.toJson(this)

    fun String.toPrefs(): Map<String, Any> =
        Constant.GSON.fromJson(this, object : TypeToken<Map<String, Any>>() {}.type)

    fun clearPref(key: String) = editor.remove(key).apply()

    fun clearPrefs(vararg keys: String) = keys.forEach { key -> editor.remove(key).apply() }

    fun clearAllPrefs() = editor.clear().apply()
}