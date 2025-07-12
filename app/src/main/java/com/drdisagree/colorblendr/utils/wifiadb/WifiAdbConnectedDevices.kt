package com.drdisagree.colorblendr.utils.wifiadb

import android.os.Handler
import android.os.Looper
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell.adbLibPath
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell.configureEnvironment
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Utility class for fetching connected wireless ADB devices.
 * This runs `adb devices` and filters only devices with an IP:Port format.
 */
object WifiAdbConnectedDevices {

    /**
     * Fetches the list of connected wireless ADB devices.
     *
     * @param callback The callback to receive the results.
     */
    fun getConnectedDevices(callback: ConnectedDevicesCallback) {
        Thread {
            val connectedDevices = fetchConnectedDevices()
            if (connectedDevices != null && !connectedDevices.isEmpty()) {
                postResult { callback.onDevicesListed(connectedDevices) }
            } else {
                postResult { callback.onFailure("No wireless devices connected") }
            }
        }.start()
    }

    /**
     * Checks if the device specified in preferences is connected via wireless ADB.
     *
     * @return True if the device is connected, false otherwise.
     */
    fun isMyDeviceConnected(): Boolean {
        val deviceIdentifier = Utilities.getMyDeviceIdentifier() ?: return false
        val connectedDevices = fetchConnectedDevices() ?: return false

        return connectedDevices.any { it == deviceIdentifier }
    }

    /**
     * Executes the `adb devices` command and filters wireless ADB devices.
     *
     * @return A list of connected wireless ADB devices.
     */
    private fun fetchConnectedDevices(): MutableList<String?>? {
        val deviceList: MutableList<String?> = ArrayList()
        var listProcess: Process? = null

        try {
            val processBuilder = ProcessBuilder(adbLibPath(), "devices")
            configureEnvironment(processBuilder)

            listProcess = processBuilder.start()

            BufferedReader(InputStreamReader(listProcess.inputStream)).use { reader ->
                BufferedReader(InputStreamReader(listProcess.errorStream)).use { errorReader ->

                    // Ignore the first line (header)
                    reader.readLine()

                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        if (isWirelessDevice(line!!)) {
                            deviceList.add(
                                line.split("\\s+".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray()[0]) // Extract only IP:Port
                        }
                    }

                    if (listProcess.waitFor() != 0) return null
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return null
        } finally {
            listProcess?.destroy()
            if (listProcess?.isAlive == true) listProcess.destroyForcibly()
        }

        return deviceList
    }

    /**
     * Checks if a device entry represents a wireless ADB connection.
     *
     * @param deviceLine The line from `adb devices` output.
     * @return True if the line contains an IP:Port, false otherwise.
     */
    private fun isWirelessDevice(deviceLine: String): Boolean {
        return deviceLine.contains("device")
    }

    /**
     * Posts a result to the main thread.
     *
     * @param runnable The runnable to execute on the main thread.
     */
    private fun postResult(runnable: Runnable) {
        Handler(Looper.getMainLooper()).post(runnable)
    }

    /**
     * Callback interface to return the list of connected devices.
     */
    interface ConnectedDevicesCallback {
        fun onDevicesListed(devices: MutableList<String?>?)
        fun onFailure(errorMessage: String?)
    }
}