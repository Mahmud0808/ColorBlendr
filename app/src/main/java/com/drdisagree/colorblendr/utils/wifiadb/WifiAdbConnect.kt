package com.drdisagree.colorblendr.utils.wifiadb

import android.os.Handler
import android.os.Looper
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell.adbLibPath
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell.configureEnvironment
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility object for connecting to a device via ADB over Wi-Fi.
 */
object WifiAdbConnect {

    /**
     * Connects a device via ADB over Wi-Fi.
     *
     * @param ip The IP address of the device to connect to.
     * @param port The port number for the ADB connection.
     * @param callback A callback to handle success or failure of the connection.
     */
    fun connect(ip: String, port: String, callback: ConnectingCallback) {
        Thread { executeConnection(ip, port, callback) }.start()
    }

    /**
     * Executes the ADB connect command in a separate thread.
     *
     * @param ip The IP address of the device to connect to.
     * @param port The port number for the ADB connection.
     * @param callback A callback to handle success or failure of the connection.
     */
    private fun executeConnection(
        ip: String,
        port: String,
        callback: ConnectingCallback
    ) {
        var connectingProcess: Process? = null
        try {
            // Build the ADB connect command
            val processBuilder = ProcessBuilder(adbLibPath(), "connect", "$ip:$port")
            configureEnvironment(processBuilder)

            // Start the process
            connectingProcess = processBuilder.start()

            // Check if the connection was successful
            val isSuccess = checkSuccess(connectingProcess)
            val errorMessage = readError(connectingProcess)

            // Notify the callback on the main thread
            runOnMainThread {
                if (isSuccess) callback.onSuccess()
                else callback.onFailure(errorMessage)
            }
        } catch (e: Exception) {
            // Handle any exceptions and notify the callback
            runOnMainThread {
                callback.onFailure(e.message)
            }
        } finally {
            // Clean up the process
            connectingProcess?.destroy()
            if (connectingProcess?.isAlive == true) connectingProcess.destroyForcibly()
        }
    }

    /**
     * Checks if the ADB connection was successful by reading the process output.
     *
     * @param process The process running the ADB connect command.
     * @return True if the connection was successful, false otherwise.
     * @throws Exception If an error occurs while reading the process output.
     */
    @Throws(Exception::class)
    private fun checkSuccess(process: Process): Boolean {
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while ((reader.readLine().also { line = it }) != null) {
            if (line!!.contains("connected to")) return true
        }
        return false
    }

    /**
     * Reads the error output from the ADB process.
     *
     * @param process The process running the ADB connect command.
     * @return A string containing the error message, if any.
     * @throws Exception If an error occurs while reading the error stream.
     */
    @Throws(Exception::class)
    private fun readError(process: Process): String {
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))
        val errorMessage = StringBuilder()
        var line: String?
        while ((errorReader.readLine().also { line = it }) != null) {
            errorMessage.append(line).append("\n")
        }
        return errorMessage.toString().trim()
    }

    /**
     * Runs a task on the main thread using a Handler.
     *
     * @param task The task to be executed on the main thread.
     */
    private fun runOnMainThread(task: Runnable) {
        Handler(Looper.getMainLooper()).post(task)
    }

    /**
     * Callback interface for handling the result of the ADB connection.
     */
    interface ConnectingCallback {
        /**
         * Called when the connection is successful.
         */
        fun onSuccess()

        /**
         * Called when the connection fails.
         *
         * @param errorMessage The error message describing the failure.
         */
        fun onFailure(errorMessage: String?)
    }
}