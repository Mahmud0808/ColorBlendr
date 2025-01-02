package com.drdisagree.colorblendr.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.EXCLUDED_PREFS_FROM_BACKUP
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.common.Const.SAVED_CUSTOM_MONET_STYLES
import com.drdisagree.colorblendr.common.Const.THEMING_ENABLED
import com.drdisagree.colorblendr.common.Const.WALLPAPER_COLOR_LIST
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

@Suppress("unused")
object RPrefs {
    private val TAG: String = RPrefs::class.java.simpleName

    private var prefs: SharedPreferences = appContext
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(Const.SHARED_PREFS, Context.MODE_PRIVATE)
    private var editor: SharedPreferences.Editor = prefs.edit()

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

    fun getAllPrefsAsGson(): String = Const.GSON.toJson(getAllPrefs() as Map<String, *>)

    fun Map<String, Any>.toGsonString(): String = Const.GSON.toJson(this)

    fun String.toPrefs(): Map<String, Any> =
        Const.GSON.fromJson(this, object : TypeToken<Map<String, Any>>() {}.type)

    fun clearPref(key: String) = editor.remove(key).apply()

    fun clearPrefs(vararg keys: String) = keys.forEach { key -> editor.remove(key).apply() }

    fun clearAllPrefs() = editor.clear().apply()

    fun backupPrefs(outputStream: OutputStream) {
        try {
            outputStream.use {
                ObjectOutputStream(outputStream).use { objectOutputStream ->
                    val allPrefs = getAllPrefs()
                    EXCLUDED_PREFS_FROM_BACKUP.forEach { excludedPref ->
                        allPrefs.remove(excludedPref)
                    }
                    objectOutputStream.writeObject(allPrefs)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error serializing preferences", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun restorePrefs(inputStream: InputStream) {
        var map: Map<String, Any>
        try {
            inputStream.use {
                ObjectInputStream(inputStream).use { objectInputStream ->
                    map = objectInputStream.readObject() as Map<String, Any>
                }
            }
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "Error deserializing preferences", e)
            return
        } catch (e: IOException) {
            Log.e(TAG, "Error deserializing preferences", e)
            return
        }

        restorePrefsMap(map)
    }

    fun restorePrefsMap(map: Map<String, Any>, isApplyingTheme: Boolean = false) {
        val allPrefs = getAllPrefs()

        // Retrieve excluded prefs from current prefs
        val excludedPrefs: MutableMap<String, Any> = HashMap()

        // Restoring config will enable theming service
        excludedPrefs[THEMING_ENABLED] = true

        EXCLUDED_PREFS_FROM_BACKUP.forEach { excludedPref ->
            val prefValue = allPrefs[excludedPref]
            if (prefValue != null) {
                excludedPrefs[excludedPref] = prefValue
            }
        }

        // Restore saved themes when enabling a theme otherwise it will be
        // overwritten by the new preference of the theme
        val savedThemes = allPrefs[SAVED_CUSTOM_MONET_STYLES]
        if (savedThemes != null && isApplyingTheme) {
            excludedPrefs[SAVED_CUSTOM_MONET_STYLES] = allPrefs[SAVED_CUSTOM_MONET_STYLES] as String
        }

        // Check if seed color is available in current wallpaper color list
        val seedColor = map[MONET_SEED_COLOR] as? Int
        val wallpaperColors = allPrefs[WALLPAPER_COLOR_LIST] as? String
        val colorAvailable = if (seedColor != null && wallpaperColors != null) {
            Const.GSON.fromJson<ArrayList<Int?>?>(
                wallpaperColors,
                object : TypeToken<ArrayList<Int?>?>() {}.type
            )?.contains(seedColor) ?: false
        } else false

        editor.clear()

        // Restore excluded prefs
        for ((key, value) in excludedPrefs) {
            putObject(key, value)
        }

        // Restore non-excluded prefs
        for ((key, value) in map) {
            if (EXCLUDED_PREFS_FROM_BACKUP.contains(key) ||
                (isApplyingTheme && key == SAVED_CUSTOM_MONET_STYLES)
            ) {
                continue
            }

            putObject(key, value)
        }

        // Set basic color if seed color is not listed in wallpaper colors
        putObject(MONET_SEED_COLOR_ENABLED, !colorAvailable)

        editor.apply()
    }

    private fun putObject(key: String, value: Any) {
        when (value) {
            is Boolean -> editor.putBoolean(key, value)
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Long -> editor.putLong(key, value)
            // Float and Double are unused in this project
            // is Float -> editor.putFloat(key, value)
            // is Double -> editor.putFloat(key, value.toFloat())
            is Float -> editor.putInt(key, value.toInt())
            is Double -> editor.putInt(key, value.toInt())
            else -> throw IllegalArgumentException("Type ${value.javaClass.simpleName} is unknown")
        }
    }
}