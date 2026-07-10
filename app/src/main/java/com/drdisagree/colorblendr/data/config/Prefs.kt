package com.drdisagree.colorblendr.data.config

import android.content.Context
import android.content.SharedPreferences
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

@Suppress("unused")
object Prefs {
    private val TAG: String = Prefs::class.java.simpleName

    private var prefs: SharedPreferences = appContext
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(Constant.SHARED_PREFS, Context.MODE_PRIVATE)

    private var editor = prefs.edit()

    val preferenceEditor: SharedPreferences.Editor
        get() = editor

    // Staging layer: while active, writes are kept in memory only (still
    // visible to every getter and change listener) until they are either
    // committed to storage or discarded. Used by the color preview mode.
    private val REMOVED = Any()

    @Volatile
    private var stagingActive = false

    @Volatile
    private var stagedClearAll = false
    private val staged = ConcurrentHashMap<String, Any>()

    private val changeListeners =
        CopyOnWriteArraySet<SharedPreferences.OnSharedPreferenceChangeListener>()
    private val storageListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            changeListeners.forEach { it.onSharedPreferenceChanged(sharedPrefs, key) }
        }

    init {
        prefs.registerOnSharedPreferenceChangeListener(storageListener)
    }

    val isStagingActive: Boolean
        get() = stagingActive

    fun beginStaging() {
        stagingActive = true
    }

    fun commitStaged() {
        if (!stagingActive) return

        if (stagedClearAll) editor.clear()
        staged.forEach { (key, value) ->
            if (value === REMOVED) {
                editor.remove(key)
            } else {
                putIntoEditor(key, value)
            }
        }
        editor.commit()

        stagingActive = false
        stagedClearAll = false
        staged.clear()
    }

    fun discardStaged() {
        if (!stagingActive) return

        stagingActive = false
        stagedClearAll = false
        staged.clear()

        // Null key: observers re-read everything from storage.
        dispatchChange(null)
    }

    private fun stagedValue(key: String): Any? {
        if (!stagingActive) return null
        staged[key]?.let { return it }
        return if (stagedClearAll) REMOVED else null
    }

    private fun stage(key: String, value: Any) {
        staged[key] = value
        dispatchChange(key)
    }

    private fun dispatchChange(key: String?) {
        changeListeners.forEach { it.onSharedPreferenceChanged(prefs, key) }
    }

    private fun putIntoEditor(key: String, value: Any) {
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            is Float -> editor.putFloat(key, value)
            is String -> editor.putString(key, value)
            is Double -> editor.putInt(key, value.toInt())
            else -> throw IllegalArgumentException(
                "Type ${value.javaClass.simpleName} is unknown"
            )
        }
    }

    fun putAny(key: String, value: Any) {
        if (stagingActive) {
            stage(key, value)
        } else {
            putIntoEditor(key, value)
            editor.apply()
        }
    }

    fun putBoolean(key: String, `val`: Boolean) = putAny(key, `val`)

    fun putInt(key: String, `val`: Int) = putAny(key, `val`)

    fun putLong(key: String, `val`: Long) = putAny(key, `val`)

    fun putFloat(key: String, `val`: Float) = putAny(key, `val`)

    fun putString(key: String, `val`: String) = putAny(key, `val`)

    fun getBoolean(key: String): Boolean = getBoolean(key, false)

    fun getBoolean(key: String, defValue: Boolean): Boolean =
        stagedValue(key)?.let { if (it === REMOVED) defValue else it as Boolean }
            ?: prefs.getBoolean(key, defValue)

    fun getInt(key: String): Int = getInt(key, 0)

    fun getInt(key: String, defValue: Int): Int =
        stagedValue(key)?.let { if (it === REMOVED) defValue else it as Int }
            ?: prefs.getInt(key, defValue)

    fun getLong(key: String): Long = getLong(key, 0)

    fun getLong(key: String, defValue: Long): Long =
        stagedValue(key)?.let { if (it === REMOVED) defValue else it as Long }
            ?: prefs.getLong(key, defValue)

    fun getFloat(key: String): Float = getFloat(key, 0f)

    fun getFloat(key: String, defValue: Float): Float =
        stagedValue(key)?.let { if (it === REMOVED) defValue else it as Float }
            ?: prefs.getFloat(key, defValue)

    fun getString(key: String): String? = getString(key, null)

    fun getString(key: String, defValue: String?): String? =
        stagedValue(key)?.let { if (it === REMOVED) defValue else it as String }
            ?: prefs.getString(key, defValue)

    fun getAllPrefs(): MutableMap<String, *> {
        val all: MutableMap<String, Any?> = if (stagingActive && stagedClearAll) {
            HashMap()
        } else {
            HashMap(prefs.all)
        }
        if (stagingActive) {
            staged.forEach { (key, value) ->
                if (value === REMOVED) all.remove(key) else all[key] = value
            }
        }
        return all
    }

    fun getAllPrefsAsGson(): String = Constant.GSON.toJson(getAllPrefs() as Map<String, *>)

    fun Map<String, Any>.toGsonString(): String = Constant.GSON.toJson(this)

    fun String.toPrefs(): Map<String, Any> =
        Constant.GSON.fromJson(this, object : TypeToken<Map<String, Any>>() {}.type)

    fun registerChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        changeListeners.add(listener)
    }

    fun unregisterChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        changeListeners.remove(listener)
    }

    fun clearPref(key: String) {
        if (stagingActive) {
            stage(key, REMOVED)
        } else {
            editor.remove(key).apply()
        }
    }

    fun clearPrefs(vararg keys: String) = keys.forEach { key -> clearPref(key) }

    fun clearAllPrefs() {
        if (stagingActive) {
            stagedClearAll = true
            staged.clear()
            dispatchChange(null)
        } else {
            editor.clear().apply()
        }
    }
}
