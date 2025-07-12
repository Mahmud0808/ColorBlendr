package com.drdisagree.colorblendr.utils.wifiadb

import android.util.Log
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Utilities
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * Utility object for executing ADB shell commands.
 */
object WifiAdbShell {

    /**
     * Executes an ADB command and returns its success status.
     *
     * @param command The ADB command to execute.
     * @return True if the command executed successfully, false otherwise.
     */
    fun exec(command: String): AdbResult {
        var process: Process? = null

        try {
            var deviceTargetedCommand: String = command

            if (command !in listOf("devices", "start-server", "kill-server")) {
                val deviceIdentifier = Utilities.getMyDeviceIdentifier() ?: return AdbResult(
                    false,
                    "No device identifier found. Please ensure a device is connected."
                )
                deviceTargetedCommand = "-s $deviceIdentifier $command"
            }

            val commandArray: Array<String> = deviceTargetedCommand.split(" ".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val fullCommand = arrayOfNulls<String>(commandArray.size + 1)

            fullCommand[0] = adbLibPath()
            System.arraycopy(commandArray, 0, fullCommand, 1, commandArray.size)

            val processBuilder = ProcessBuilder(*fullCommand)
            configureEnvironment(processBuilder)
            process = processBuilder.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val output = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            while (errorReader.readLine().also { line = it } != null) {
                output.append("ERROR: ").append(line).append("\n")
            }

            val success = process.waitFor() == 0 && !output.contains("ERROR:")
            return AdbResult(success, output.toString().trim())
        } catch (e: Exception) {
            return AdbResult(false, "Exception: ${e.message}")
        } finally {
            process?.destroy()
            if (process?.isAlive == true) process.destroyForcibly()
        }
    }

    /**
     * Restarts the ADB server.
     */
    fun restartAdbServer() {
        exec("kill-server")
        exec("start-server")
    }

    /**
     * Starts the ADB server.
     */
    fun startServer() {
        exec("start-server")
    }

    /**
     * Kills the ADB server.
     */
    fun killServer() {
        exec("kill-server")
    }

    /**
     * Configures environment variables for ADB execution.
     *
     * @param processBuilder The process builder to configure.
     */
    fun configureEnvironment(processBuilder: ProcessBuilder) {
        processBuilder.environment().apply {
            put("HOME", appContext.filesDir.absolutePath)
            put("ADB_VENDOR_KEYS", appContext.filesDir.absolutePath)
            put("TMPDIR", ensureTmpDir())
        }
    }

    /**
     * Ensures the TMP directory exists and returns its path.
     *
     * @return The path to the TMP directory.
     */
    fun ensureTmpDir(): String {
        val tmpDir = File(appContext.filesDir, "tmp")
        if (!tmpDir.exists()) {
            Log.d("WifiAdbShell", "TMPDIR created: " + tmpDir.mkdirs())
        }
        return tmpDir.absolutePath
    }

    /**
     * Returns the ADB binary path.
     *
     * @return The path to the ADB binary.
     */
    fun adbLibPath(): String {
        return "${appContext.applicationInfo.nativeLibraryDir}/libadb.so"
    }

    /**
     * Data class representing the result of an ADB command execution.
     *
     * @property success Indicates if the command was executed successfully.
     * @property output The output of the command execution.
     */
    data class AdbResult(
        val success: Boolean,
        val output: String
    )
}