package com.drdisagree.colorblendr.utils.wifiadb

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell.adbLibPath
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell.configureEnvironment
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Utility object for pairing with ADB over Wi-Fi.
 */
object WifiAdbPair {

    private const val TIMEOUT = 5 // Timeout in seconds

    /**
     * Initiates pairing with the given IP, port, and pairing code.
     *
     * @param context The application context.
     * @param ip The IP address of the device to pair with.
     * @param port The port number of the device to pair with.
     * @param pairingCode The pairing code for authentication.
     * @param callback The callback to handle success or failure of the pairing process.
     */
    fun pair(
        context: Context,
        ip: String,
        port: String,
        pairingCode: String,
        callback: PairingCallback
    ) {
        Thread {
            var pairingProcess: Process? = null
            try {
                // Restart ADB server before pairing
                WifiAdbShell.killServer()
                Thread.sleep(500)

                // Start the pairing process
                pairingProcess = startPairingProcess(context, ip, port, pairingCode)
                val isSuccess = checkPairingSuccess(pairingProcess)
                val errorOutput = readErrorOutput(pairingProcess)

                // Handle timeout
                if (!pairingProcess.waitFor(TIMEOUT.toLong(), TimeUnit.SECONDS)) {
                    pairingProcess.destroy()
                }

                // Post the result to the callback
                postResult(callback, isSuccess, errorOutput)
            } catch (e: Exception) {
                // Post failure to the callback
                postFailure(callback, e.message)
            } finally {
                // Ensure the process is terminated
                destroyProcess(pairingProcess)
            }
        }.start()
    }

    /**
     * Starts the ADB pairing process.
     *
     * @param context The application context.
     * @param ip The IP address of the device to pair with.
     * @param port The port number of the device to pair with.
     * @param pairingCode The pairing code for authentication.
     * @return The process object representing the pairing process.
     * @throws Exception If an error occurs while starting the process.
     */
    @Throws(Exception::class)
    private fun startPairingProcess(
        context: Context,
        ip: String,
        port: String,
        pairingCode: String
    ): Process {
        val processBuilder = ProcessBuilder(adbLibPath(), "pair", "$ip:$port", pairingCode)

        // Configure the environment for the process
        configureEnvironment(processBuilder)
        return processBuilder.start()
    }

    /**
     * Checks if the pairing process was successful.
     *
     * @param process The process object representing the pairing process.
     * @return True if pairing was successful, false otherwise.
     * @throws Exception If an error occurs while reading the process output.
     */
    @Throws(Exception::class)
    private fun checkPairingSuccess(process: Process): Boolean {
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while ((reader.readLine().also { line = it }) != null) {
            if (line!!.contains("Successfully paired")) {
                return true
            }
        }
        return false
    }

    /**
     * Reads error output from the pairing process.
     *
     * @param process The process object representing the pairing process.
     * @return A string containing the error output.
     * @throws Exception If an error occurs while reading the error stream.
     */
    @Throws(Exception::class)
    private fun readErrorOutput(process: Process): String {
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))
        val errorMessage = StringBuilder()
        var line: String?
        while ((errorReader.readLine().also { line = it }) != null) {
            errorMessage.append(line).append("\n")
        }
        return errorMessage.toString().trim()
    }

    /**
     * Posts the result of the pairing process on the main thread.
     *
     * @param callback The callback to handle success or failure.
     * @param isSuccess True if pairing was successful, false otherwise.
     * @param errorMessage The error message if pairing failed.
     */
    private fun postResult(callback: PairingCallback, isSuccess: Boolean, errorMessage: String) {
        Handler(Looper.getMainLooper()).post {
            if (isSuccess) {
                callback.onSuccess()
            } else {
                callback.onFailure(errorMessage.ifEmpty { "Pairing failed" })
            }
        }
    }

    /**
     * Posts a failure result on the main thread.
     *
     * @param callback The callback to handle failure.
     * @param errorMessage The error message describing the failure.
     */
    private fun postFailure(callback: PairingCallback?, errorMessage: String?) {
        Handler(Looper.getMainLooper()).post {
            callback?.onFailure(errorMessage)
        }
    }

    /**
     * Ensures the process is properly terminated.
     *
     * @param process The process object to terminate.
     */
    private fun destroyProcess(process: Process?) {
        process?.destroy()
        if (process?.isAlive == true) process.destroyForcibly()
    }

    /**
     * Callback interface for handling the result of the pairing process.
     */
    interface PairingCallback {
        /**
         * Called when pairing is successful.
         */
        fun onSuccess()

        /**
         * Called when pairing fails.
         *
         * @param errorMessage The error message describing the failure.
         */
        fun onFailure(errorMessage: String?)
    }
}