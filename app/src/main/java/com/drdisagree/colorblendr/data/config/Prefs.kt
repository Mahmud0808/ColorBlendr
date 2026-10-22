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

    private val prefs: SharedPreferences by lazy {
        val context = runCatching { appContext.createDeviceProtectedStorageContext() }
            .getOrNull() ?: appContext
        context.getSharedPreferences(Constant.SHARED_PREFS, Context.MODE_PRIVATE).apply {
            registerOnSharedPreferenceChangeListener(storageListener)
        }
    }

    fun newEditor(): SharedPreferences.Editor = prefs.edit()

    private val stagingLock = Any()

    private val unstagedKeys = setOf(
        Constant.WALLPAPER_COLOR_LIST,
        Constant.MONET_LAST_UPDATED,
        Constant.COMMUNITY_LAST_FETCH,
        Constant.COMMUNITY_VOTED_IDS,
        Constant.PER_APP_DEFAULTS_SEEDED,
        Constant.SAMSUNG_PALETTE_ORIGINAL_STATE,
        Constant.SHOW_PALETTE_ROOT_HINT,
        Constant.SHOW_PALETTE_ROOTLESS_HINT,
        Constant.APP_COLOR_OVERRIDES
    )

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


    val isStagingActive: Boolean
        get() = stagingActive

    fun beginStaging() {
        synchronized(stagingLock) {
            if (stagingActive) return
            staged.clear()
            stagedClearAll = false
            stagingActive = true
        }
    }

    fun commitStaged() {
        synchronized(stagingLock) {
            if (!stagingActive) return

            val editor = prefs.edit()
            if (stagedClearAll) editor.clear()
            staged.forEach { (key, value) ->
                if (value === REMOVED) {
                    editor.remove(key)
                } else {
                    editor.putValue(key, value)
                }
            }
            editor.commit()

            stagingActive = false
            stagedClearAll = false
            staged.clear()
        }
    }

    fun discardStaged() {
        synchronized(stagingLock) {
            if (!stagingActive) return

            stagingActive = false
            stagedClearAll = false
            staged.clear()
        }

        // Null key: observers re-read everything from storage.
        dispatchChange(null)
    }

    private fun stagedValue(key: String): Any? {
        if (!stagingActive) return null
        staged[key]?.let { return it }
        return if (stagedClearAll) REMOVED else null
    }

    private fun dispatchChange(key: String?) {
        changeListeners.forEach { it.onSharedPreferenceChanged(prefs, key) }
    }

    private fun SharedPreferences.Editor.putValue(key: String, value: Any) {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is String -> putString(key, value)
            is Double -> putInt(key, value.toInt())
            else -> throw IllegalArgumentException(
                "Type ${value.javaClass.simpleName} is unknown"
            )
        }
    }

    private fun stageOrWrite(key: String, value: Any) {
        val stagedNow = synchronized(stagingLock) {
            if (stagingActive && key !in unstagedKeys) {
                staged[key] = value
                true
            } else {
                prefs.edit().apply {
                    if (value === REMOVED) remove(key) else putValue(key, value)
                }.apply()
                false
            }
        }
        if (stagedNow) dispatchChange(key)
    }

    fun putAny(key: String, value: Any) = stageOrWrite(key, value)

    fun putBoolean(key: String, `val`: Boolean) = putAny(key, `val`)

    fun putInt(key: String, `val`: Int) = putAny(key, `val`)

    fun putLong(key: String, `val`: Long) = putAny(key, `val`)

    fun putFloat(key: String, `val`: Float) = putAny(key, `val`)

    fun putString(key: String, `val`: String) = putAny(key, `val`)

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        val staged = stagedValue(key) ?: return prefs.getBoolean(key, defValue)
        return if (staged === REMOVED) defValue else staged as Boolean
    }

    fun getInt(key: String): Int = getInt(key, 0)

    fun getInt(key: String, defValue: Int): Int {
        val staged = stagedValue(key) ?: return prefs.getInt(key, defValue)
        return if (staged === REMOVED) defValue else (staged as Number).toInt()
    }

    fun getLong(key: String): Long = getLong(key, 0)

    fun getLong(key: String, defValue: Long): Long {
        val staged = stagedValue(key) ?: return prefs.getLong(key, defValue)
        return if (staged === REMOVED) defValue else (staged as Number).toLong()
    }

    fun getFloat(key: String): Float = getFloat(key, 0f)

    fun getFloat(key: String, defValue: Float): Float {
        val staged = stagedValue(key) ?: return prefs.getFloat(key, defValue)
        return if (staged === REMOVED) defValue else (staged as Number).toFloat()
    }

    fun getCommittedString(key: String, defValue: String? = null): String? =
        prefs.getString(key, defValue)

    fun getString(key: String): String? = getString(key, null)

    fun getString(key: String, defValue: String?): String? {
        val staged = stagedValue(key) ?: return prefs.getString(key, defValue)
        return if (staged === REMOVED) defValue else staged as String
    }

    fun getAllPrefs(): MutableMap<String, *> = synchronized(stagingLock) {
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
        all
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

    fun clearPref(key: String) = stageOrWrite(key, REMOVED)

    fun clearPrefs(vararg keys: String) = keys.forEach { key -> clearPref(key) }

    fun clearAllPrefs() {
        val stagedNow = synchronized(stagingLock) {
            if (stagingActive) {
                stagedClearAll = true
                staged.clear()
                true
            } else {
                prefs.edit().clear().apply()
                false
            }
        }
        if (stagedNow) dispatchChange(null)
    }
}
