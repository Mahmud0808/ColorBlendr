package com.drdisagree.colorblendr.utils.wifiadb

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell.clearEnabled
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell.commandOutput
import com.topjohnwu.superuser.Shell
import io.github.muntashirakon.adb.AdbPairingRequiredException
import io.github.muntashirakon.adb.AdbStream
import io.github.muntashirakon.adb.LocalServices
import io.github.muntashirakon.adb.android.AdbMdns
import io.github.muntashirakon.adb.android.AndroidUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.Volatile

object WifiAdbShell {

    private val TAG = WifiAdbShell::class.java.simpleName
    private val executor: ExecutorService = Executors.newFixedThreadPool(3)
    private var adbShellStream: AdbStream? = null
    private val commandOutput = MutableLiveData<CharSequence?>()

    fun watchCommandOutput(): LiveData<CharSequence?> = commandOutput

    fun isMyDeviceConnected(): Boolean {
        return try {
            val manager = AdbConnectionManager.getInstance(appContext)
            manager.isConnected
        } catch (e: Exception) {
            Log.e(TAG, "isMyDeviceConnected: ", e)
            false
        }
    }

    fun clear() {
        executor.submit {
            try {
                adbShellStream?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing adbShellStream", e)
            }
            try {
                AdbConnectionManager.getInstance(appContext).close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing AdbConnectionManager", e)
            }
        }
        executor.shutdown()
    }

    fun connect(ip: String?, port: Int, callback: ConnectionListener? = null) {
        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(appContext)
                val connectionStatus = try {
                    manager.connect(ip ?: AndroidUtils.getHostIpAddress(appContext), port)
                } catch (th: Throwable) {
                    Log.e(TAG, "connect: ", th)
                    false
                }
                callback?.let {
                    if (connectionStatus) it.onConnectionSuccess() else it.onConnectionFailed()
                }
            } catch (th: Throwable) {
                Log.e(TAG, "connect: ", th)
                callback?.onConnectionFailed()
            }
        }
    }

    fun autoConnect(callback: ConnectionListener? = null) {
        executor.submit { autoConnectInternal(callback) }
    }

    fun disconnect() {
        executor.submit {
            try {
                val manager = AdbConnectionManager.getInstance(appContext)
                manager.disconnect()
            } catch (th: Throwable) {
                Log.e(TAG, "disconnect: ", th)
            }
        }
    }

    fun getPairingAddress(callback: IpAddressListener?) {
        executor.submit {
            val atomicPort = AtomicInteger(-1)
            val resolveHostAndPort = CountDownLatch(1)

            val adbMdns = AdbMdns(
                appContext,
                AdbMdns.SERVICE_TYPE_TLS_PAIRING
            ) { abc: InetAddress?, port: Int ->
                atomicPort.set(port)
                resolveHostAndPort.countDown()
                callback?.onPortDetected(abc?.hostAddress, port)
            }
            adbMdns.start()

            try {
                if (!resolveHostAndPort.await(3, TimeUnit.MINUTES)) { // Timeout after 3 minutes
                    callback?.onPortDetected(null, -1)
                    return@submit
                }
            } catch (_: InterruptedException) {
            } finally {
                adbMdns.stop()
            }
        }
    }

    fun pair(
        ip: String?,
        port: Int,
        pairingCode: String,
        pairingListener: PairingListener? = null,
        connectionListener: ConnectionListener? = null
    ) {
        executor.submit {
            try {
                execute("kill-server")
                val manager = AdbConnectionManager.getInstance(appContext)
                val pairingStatus = manager.pair(
                    ip ?: AndroidUtils.getHostIpAddress(appContext),
                    port,
                    pairingCode
                )
                pairingListener?.let {
                    if (pairingStatus) it.onPairingSuccess() else it.onPairingFailed()
                }
                autoConnectInternal(connectionListener)
            } catch (th: Throwable) {
                Log.e(TAG, "pair: ", th)
                pairingListener?.onPairingFailed()
            }
        }
    }

    @WorkerThread
    private fun autoConnectInternal(callback: ConnectionListener? = null) {
        try {
            val manager = AdbConnectionManager.getInstance(appContext)
            var connected = false
            try {
                connected = manager.autoConnect(appContext, 5000)
            } catch (_: AdbPairingRequiredException) {
                callback?.onConnectionFailed()
                return
            } catch (_: Throwable) {
            }
            if (!connected) {
                connected = manager.connect(5555)
            }
            if (connected) {
                callback?.onConnectionSuccess()
            } else {
                callback?.onConnectionFailed()
            }
        } catch (th: Throwable) {
            Log.e(TAG, "autoConnectInternal: ", th)
            callback?.onConnectionFailed()
        }
    }

    @Volatile
    private var clearEnabled = false

    /**
     * Runnable that reads output from the ADB shell stream and posts it to [commandOutput].
     * It continuously reads lines from the input stream, appends them to a StringBuilder,
     * and updates the LiveData. If [clearEnabled] is true, it clears the StringBuilder
     * before appending new output.
     */
    private val outputGenerator = Runnable {
        try {
            BufferedReader(InputStreamReader(adbShellStream!!.openInputStream())).use { reader ->
                val sb = StringBuilder()
                var s: String?
                while (reader.readLine().also { s = it } != null) {
                    if (clearEnabled) {
                        sb.delete(0, sb.length)
                        clearEnabled = false
                    }
                    sb.append(s).append("\n")
                    commandOutput.postValue(sb)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading from adbShellStream", e)
        }
    }

    fun execute(command: String) {
        if (!isMyDeviceConnected()) {
            Log.e(TAG, "execute: Not connected to any device")
            return
        }

        executor.submit {
            try {
                if (adbShellStream == null || adbShellStream!!.isClosed) {
                    val manager = AdbConnectionManager.getInstance(appContext)
                    adbShellStream = manager.openStream(LocalServices.SHELL)
                    Thread(outputGenerator).start()
                }
                if (command == "clear") {
                    clearEnabled = true
                }
                adbShellStream!!.openOutputStream().use { os ->
                    os.write("$command\n".toByteArray(StandardCharsets.UTF_8))
                    os.flush()
                    os.write("\n".toByteArray(StandardCharsets.UTF_8))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error executing command: $command", e)
            }
        }
    }

    fun exec(command: String, callback: (String) -> Unit) {
        if (!isMyDeviceConnected()) {
            Log.e(TAG, "exec: Not connected to any device")
            return
        }

        callback.invoke(Shell.cmd(command).exec().out.joinToString(separator = "; "))
    }

    interface IpAddressListener {
        fun onPortDetected(ip: String?, port: Int)
    }

    interface PairingListener {
        fun onPairingSuccess()
        fun onPairingFailed()
    }

    interface ConnectionListener {
        fun onConnectionSuccess()
        fun onConnectionFailed()
    }
}