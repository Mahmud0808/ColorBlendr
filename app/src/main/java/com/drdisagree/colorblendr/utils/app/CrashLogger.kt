package com.drdisagree.colorblendr.utils.app

import android.content.Context
import com.drdisagree.colorblendr.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Local-only crash capture for the dev-mode viewer; nothing leaves the
// device. Installed in Application, chains to the system handler.
object CrashLogger {

    private const val FILE_NAME = "crash_log.txt"
    private const val MAX_BYTES = 200 * 1024L

    fun install(context: Context) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                append(context, thread, throwable)
            } catch (_: Exception) {
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun append(context: Context, thread: Thread, throwable: Throwable) {
        val file = logFile(context)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val entry = buildString {
            append("---- $timestamp | v${BuildConfig.VERSION_NAME} | thread ${thread.name} ----\n")
            append(throwable.stackTraceToString())
            append("\n\n")
        }

        // Newest first; trim tail past the cap.
        val existing = if (file.exists()) file.readText() else ""
        file.writeText((entry + existing).take(MAX_BYTES.toInt()))
    }

    fun read(context: Context): String =
        logFile(context).takeIf { it.exists() }?.readText().orEmpty()

    fun clear(context: Context) {
        logFile(context).delete()
    }

    private fun logFile(context: Context): File = File(context.filesDir, FILE_NAME)
}
