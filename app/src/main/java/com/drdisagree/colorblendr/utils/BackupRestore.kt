package com.drdisagree.colorblendr.utils

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Const
import com.drdisagree.colorblendr.data.common.Const.DATABASE_NAME
import com.drdisagree.colorblendr.data.common.Const.EXCLUDED_PREFS_FROM_BACKUP
import com.drdisagree.colorblendr.data.common.Const.MONET_SEED_COLOR
import com.drdisagree.colorblendr.data.common.Const.MONET_SEED_COLOR_ENABLED
import com.drdisagree.colorblendr.data.common.Const.SAVED_CUSTOM_MONET_STYLES
import com.drdisagree.colorblendr.data.common.Const.THEMING_ENABLED
import com.drdisagree.colorblendr.data.common.Const.WALLPAPER_COLOR_LIST
import com.drdisagree.colorblendr.data.common.Const.workingMethod
import com.drdisagree.colorblendr.data.config.Prefs.getAllPrefs
import com.drdisagree.colorblendr.data.config.Prefs.preferenceEditor
import com.drdisagree.colorblendr.data.database.appDatabase
import com.drdisagree.colorblendr.data.models.CustomStyleModel
import com.drdisagree.colorblendr.data.repository.CustomStyleRepository
import com.google.gson.reflect.TypeToken
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

object BackupRestore {

    private val TAG = BackupRestore::class.java.simpleName
    private const val PREFERENCE_BACKUP_FILE_NAME = "preference_backup"
    private const val DATABASE_BACKUP_FILE_NAME = "database_backup"

    suspend fun Uri.backupDatabaseAndPrefs(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = appContext.contentResolver
                    .openOutputStream(this@backupDatabaseAndPrefs)
                    ?: throw IOException("Failed to open output stream")

                val tempZipFile = File(appContext.cacheDir, "backup.zip")

                if (tempZipFile.exists()) tempZipFile.delete()

                val zipFile = ZipFile(tempZipFile)
                val zipParameters = ZipParameters().apply {
                    compressionMethod = CompressionMethod.DEFLATE
                    compressionLevel = CompressionLevel.NORMAL
                }

                val prefsBackupStream = ByteArrayOutputStream().also { backupPrefs(it) }
                zipParameters.fileNameInZip = PREFERENCE_BACKUP_FILE_NAME
                zipFile.addStream(
                    ByteArrayInputStream(prefsBackupStream.toByteArray()),
                    zipParameters
                )

                val dbPath = appContext.getDatabasePath(DATABASE_NAME).absolutePath
                val databaseFiles = listOf(
                    File(dbPath) to DATABASE_BACKUP_FILE_NAME,
                    File("$dbPath-sh") to "$DATABASE_BACKUP_FILE_NAME-sh",
                    File("$dbPath-shm") to "$DATABASE_BACKUP_FILE_NAME-shm",
                    File("$dbPath-wal") to "$DATABASE_BACKUP_FILE_NAME-wal"
                )

                databaseFiles.forEach { (dbFile, dbFileName) ->
                    if (dbFile.exists()) {
                        zipParameters.fileNameInZip = dbFileName
                        zipFile.addFile(dbFile, zipParameters)
                    }
                }

                tempZipFile.inputStream().use { it.copyTo(outputStream) }

                tempZipFile.delete()

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error during backup", e)
                false
            }
        }
    }

    suspend fun Uri.restoreDatabaseAndPrefs(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStreamLegacy = appContext.contentResolver
                    .openInputStream(this@restoreDatabaseAndPrefs)

                /*
                 * Start of backwards compatibility
                 */
                val success = restorePrefs(inputStreamLegacy, true)
                if (success) return@withContext true
                /*
                 * End of backwards compatibility
                 */

                val tempDir = File(appContext.cacheDir, "temp_restore").also {
                    if (it.exists()) {
                        it.deleteRecursively()
                    }
                    it.mkdirs()
                }

                val cacheFile = File(tempDir, "backup.zip")
                appContext.contentResolver.openInputStream(this@restoreDatabaseAndPrefs)
                    ?.use { inputStream ->
                        cacheFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                val zipFile = ZipFile(cacheFile)

                if (!zipFile.isValidZipFile) {
                    throw IOException("Invalid backup file")
                }

                val prefsFile = File(tempDir, PREFERENCE_BACKUP_FILE_NAME)
                zipFile.extractFile(PREFERENCE_BACKUP_FILE_NAME, tempDir.absolutePath)
                restorePrefs(prefsFile.inputStream(), false)

                val dbPath = appContext.getDatabasePath(DATABASE_NAME).absolutePath
                val databaseFiles = listOf(
                    DATABASE_BACKUP_FILE_NAME to File(dbPath),
                    "$DATABASE_BACKUP_FILE_NAME-sh" to File("$dbPath-sh"),
                    "$DATABASE_BACKUP_FILE_NAME-shm" to File("$dbPath-shm"),
                    "$DATABASE_BACKUP_FILE_NAME-wal" to File("$dbPath-wal")
                )

                databaseFiles.forEach { (dbFileName, targetFile) ->
                    try {
                        val extractedFile = File(tempDir, dbFileName)
                        zipFile.extractFile(dbFileName, tempDir.absolutePath)
                        extractedFile.copyTo(targetFile, overwrite = true)
                    } catch (_: IOException) {
                    }
                }

                tempDir.deleteRecursively()

                // Restart app with delay to load new database,
                // otherwise user might think that app crashed
                if (workingMethod == Const.WorkMethod.ROOT) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        ProcessPhoenix.triggerRebirth(appContext)
                    }, 3000)
                }

                true
            } catch (e: Exception) {
                Log.e(TAG, "Error during restore", e)
                false
            }
        }
    }

    private suspend fun backupPrefs(outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            try {
                ObjectOutputStream(outputStream).use { objectOutputStream ->
                    val allPrefs = getAllPrefs().toMutableMap()
                    EXCLUDED_PREFS_FROM_BACKUP.forEach { allPrefs.remove(it) }
                    objectOutputStream.writeObject(allPrefs)
                    objectOutputStream.flush()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error serializing preferences", e)
            }
        }
    }

    private suspend fun restorePrefs(
        inputStream: InputStream?,
        suppressException: Boolean
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("UNCHECKED_CAST")
                val map = ObjectInputStream(inputStream).use {
                    it.readObject() as Map<String, Any>
                }
                restorePrefsMap(map)
                true
            } catch (e: Exception) {
                if (!suppressException) {
                    Log.e(TAG, "Error deserializing preferences", e)
                } else {
                    // Backwards compatibility
                }
                false
            }
        }
    }

    @Suppress("deprecation")
    suspend fun restorePrefsMap(map: Map<String, Any>) {
        withContext(Dispatchers.IO) {
            val allPrefs = getAllPrefs()
            val newPrefs = map.toMutableMap()

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

            // Check if seed color is available in current wallpaper color list
            val seedColor = newPrefs[MONET_SEED_COLOR] as? Int
            val wallpaperColors = allPrefs[WALLPAPER_COLOR_LIST] as? String
            val colorAvailable = if (seedColor != null && wallpaperColors != null) {
                Const.GSON.fromJson<ArrayList<Int?>?>(
                    wallpaperColors,
                    object : TypeToken<ArrayList<Int?>?>() {}.type
                )?.contains(seedColor) ?: false
            } else false

            preferenceEditor.clear()

            /*
             * Migrate previously saved custom styles from preferences to database
             */
            val savedCustomStyles = newPrefs[SAVED_CUSTOM_MONET_STYLES] as? String
            if (!savedCustomStyles.isNullOrEmpty()) {
                val customStyles: ArrayList<CustomStyleModel> = Const.GSON.fromJson(
                    savedCustomStyles,
                    object : TypeToken<ArrayList<CustomStyleModel>>() {}.type
                )

                if (customStyles.isNotEmpty()) {
                    val customStyleRepository = CustomStyleRepository(appDatabase.customStyleDao())

                    // Clear currently saved custom styles
                    customStyleRepository.getCustomStyles().forEach {
                        customStyleRepository.deleteCustomStyle(it)
                    }

                    customStyles.forEach { customStyle ->
                        customStyleRepository.saveCustomStyle(customStyle)
                    }
                }

                // Remove SAVED_CUSTOM_MONET_STYLES key from the map after processing
                newPrefs.remove(SAVED_CUSTOM_MONET_STYLES)
            }

            // Restore excluded prefs
            for ((key, value) in excludedPrefs) {
                putObject(key, value)
            }

            // Restore non-excluded prefs
            for ((key, value) in newPrefs) {
                if (EXCLUDED_PREFS_FROM_BACKUP.contains(key)) continue

                putObject(key, value)
            }

            // Set basic color if seed color is not listed in wallpaper colors
            putObject(MONET_SEED_COLOR_ENABLED, !colorAvailable)

            preferenceEditor.commit()
        }
    }

    private fun putObject(key: String, value: Any) {
        when (value) {
            is Boolean -> preferenceEditor.putBoolean(key, value)
            is String -> preferenceEditor.putString(key, value)
            is Int -> preferenceEditor.putInt(key, value)
            is Long -> preferenceEditor.putLong(key, value)
            // Float and Double are unused in this project
            // is Float -> editor.putFloat(key, value)
            // is Double -> editor.putFloat(key, value.toFloat())
            is Float -> preferenceEditor.putInt(key, value.toInt())
            is Double -> preferenceEditor.putInt(key, value.toInt())
            else -> throw IllegalArgumentException("Type ${value.javaClass.simpleName} is unknown")
        }
    }
}