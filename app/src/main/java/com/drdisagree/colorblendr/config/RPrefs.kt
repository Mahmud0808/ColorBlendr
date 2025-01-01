package com.drdisagree.colorblendr.config

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.common.Const
import com.drdisagree.colorblendr.common.Const.EXCLUDED_PREFS_FROM_BACKUP
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR
import com.drdisagree.colorblendr.common.Const.MONET_SEED_COLOR_ENABLED
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
        .getSharedPreferences(
            Const.SHARED_PREFS, Context.MODE_PRIVATE
        )
    private var editor: SharedPreferences.Editor = prefs.edit()

    fun putBoolean(key: String, `val`: Boolean) {
        editor.putBoolean(key, `val`).apply()
    }

    fun putInt(key: String, `val`: Int) {
        editor.putInt(key, `val`).apply()
    }

    fun putLong(key: String, `val`: Long) {
        editor.putLong(key, `val`).apply()
    }

    fun putFloat(key: String, `val`: Float) {
        editor.putFloat(key, `val`).apply()
    }

    fun putString(key: String, `val`: String) {
        editor.putString(key, `val`).apply()
    }

    fun getBoolean(key: String): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    fun getInt(key: String): Int {
        return prefs.getInt(key, 0)
    }

    fun getInt(key: String, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    fun getLong(key: String): Long {
        return prefs.getLong(key, 0)
    }

    fun getLong(key: String, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    fun getFloat(key: String): Float {
        return prefs.getFloat(key, 0f)
    }

    fun getFloat(key: String, defValue: Float): Float {
        return prefs.getFloat(key, defValue)
    }

    fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    fun getString(key: String, defValue: String?): String? {
        return prefs.getString(key, defValue)
    }

    fun clearPref(key: String) {
        editor.remove(key).apply()
    }

    fun clearPrefs(vararg keys: String) {
        for (key in keys) {
            editor.remove(key).apply()
        }
    }

    fun clearAllPrefs() {
        editor.clear().apply()
    }

    fun backupPrefs(outputStream: OutputStream) {
        try {
            outputStream.use {
                ObjectOutputStream(outputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(
                        prefs.all
                    )
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error serializing preferences", e)
        }

        try {
            outputStream.use {
                ObjectOutputStream(outputStream).use { objectOutputStream ->
                    val allPrefs = prefs.all
                    for (excludedPref in EXCLUDED_PREFS_FROM_BACKUP) {
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

        // Retrieve excluded prefs from current prefs
        val excludedPrefs: MutableMap<String, Any> = HashMap()

        // Restoring config will enable theming service
        excludedPrefs[THEMING_ENABLED] = true

        for (excludedPref in EXCLUDED_PREFS_FROM_BACKUP) {
            val prefValue = prefs.all[excludedPref]
            if (prefValue != null) {
                excludedPrefs[excludedPref] = prefValue
            }
        }

        // Check if seed color is available in current wallpaper color list
        val seedColor = map[MONET_SEED_COLOR] as? Int
        val wallpaperColors = prefs.all[WALLPAPER_COLOR_LIST] as? String
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
            if (EXCLUDED_PREFS_FROM_BACKUP.contains(key)) {
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
            is Boolean -> {
                editor.putBoolean(key, value)
            }

            is String -> {
                editor.putString(key, value)
            }

            is Int -> {
                editor.putInt(key, value)
            }

            is Float -> {
                editor.putFloat(key, value)
            }

            is Long -> {
                editor.putLong(key, value)
            }

            else -> {
                throw IllegalArgumentException("Type " + value.javaClass.name + " is unknown")
            }
        }
    }
}
