package com.drdisagree.colorblendr.provider

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.service.IShizukuConnection
import com.drdisagree.colorblendr.service.LegacyShizukuConnection
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.hasShizukuPermission
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.isShizukuAvailable
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.unbindUserService
import rikka.shizuku.Shizuku
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object ShizukuConnectionProvider {

    private const val TAG = "ShizukuConnectionProvider"
    private const val CONNECT_TIMEOUT_MS = 8000L
    private const val REBIND_DELAY_MS = 1500L
    private const val MAX_REBIND_ATTEMPTS = 3

    private val lock = ReentrantLock()
    private val connected = lock.newCondition()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val legacyConnection by lazy { LegacyShizukuConnection() }

    @Volatile
    private var rebindAttempts = 0

    @Volatile
    private var serviceProvider: IShizukuConnection? = null

    @Volatile
    private var binding = false

    @Volatile
    private var helperUnavailable = false

    @Volatile
    private var listenersRegistered = false

    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder?) {
            if (binder == null || !binder.pingBinder()) {
                Log.w(TAG, "Service binder is null or not alive")
                lock.withLock {
                    binding = false
                    connected.signalAll()
                }
                return
            }

            lock.withLock {
                serviceProvider = IShizukuConnection.Stub.asInterface(binder)
                binding = false
                rebindAttempts = 0
                helperUnavailable = false
                connected.signalAll()
            }
            Log.i(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            lock.withLock {
                serviceProvider = null
                binding = false
                connected.signalAll()
            }
            if (rebindAttempts >= MAX_REBIND_ATTEMPTS) {
                Log.e(TAG, "Service disconnected; giving up after $rebindAttempts rebinds")
                helperUnavailable = true
                return
            }
            rebindAttempts++
            Log.w(TAG, "Service disconnected; rebinding (attempt $rebindAttempts)")
            mainHandler.postDelayed({ bind() }, REBIND_DELAY_MS)
        }
    }

    val isConnected: Boolean
        get() = serviceProvider != null || (helperUnavailable && isShizukuAvailable)

    val isNotConnected: Boolean
        get() = !isConnected

    val getServiceProvider: IShizukuConnection?
        get() = serviceProvider

    fun registerBinderListeners() {
        if (listenersRegistered) return
        listenersRegistered = true

        Shizuku.addBinderReceivedListenerSticky {
            Log.i(TAG, "Shizuku binder received, version ${shizukuVersion()}")
            rebindAttempts = 0
            helperUnavailable = false
            if (isShizukuMode() && hasShizukuPermission()) bind()
        }
        Shizuku.addBinderDeadListener {
            lock.withLock {
                serviceProvider = null
                binding = false
                connected.signalAll()
            }
            Log.w(TAG, "Shizuku binder died")
        }
    }

    fun bind() {
        if (helperUnavailable) return

        lock.withLock {
            if (serviceProvider != null || binding) return
            binding = true
        }

        try {
            Log.i(TAG, "Binding user service (Shizuku v${shizukuVersion()}, uid ${Shizuku.getUid()})")
            bindUserService(
                getUserServiceArgs(ShizukuConnection::class.java),
                serviceConnection
            )
        } catch (e: Exception) {
            Log.e(TAG, "bind: ", e)
            lock.withLock {
                binding = false
                connected.signalAll()
            }
        }
    }

    fun isServiceRunning(): Boolean = try {
        Shizuku.peekUserService(
            getUserServiceArgs(ShizukuConnection::class.java),
            serviceConnection
        ) != -1
    } catch (e: Exception) {
        Log.w(TAG, "peekUserService: ", e)
        false
    }

    fun connect(timeoutMs: Long = CONNECT_TIMEOUT_MS): IShizukuConnection? {
        serviceProvider?.let { return it }
        if (helperUnavailable) return fallback()

        bind()

        if (Looper.myLooper() == Looper.getMainLooper()) return serviceProvider

        val deadline = SystemClock.uptimeMillis() + timeoutMs
        lock.withLock {
            while (serviceProvider == null && binding) {
                val remaining = deadline - SystemClock.uptimeMillis()
                if (remaining <= 0) break
                try {
                    connected.await(remaining, TimeUnit.MILLISECONDS)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }

        serviceProvider?.let { return it }

        Log.e(
            TAG,
            "User service not connected after ${timeoutMs}ms " +
                    "(binding=$binding, running=${isServiceRunning()})"
        )
        markHelperUnavailable()
        return fallback()
    }

    private fun markHelperUnavailable() {
        lock.withLock {
            helperUnavailable = true
            binding = false
            connected.signalAll()
        }
        try {
            unbindUserService(getUserServiceArgs(ShizukuConnection::class.java), serviceConnection)
        } catch (e: Exception) {
            Log.w(TAG, "unbindUserService: ", e)
        }
    }

    private fun fallback(): IShizukuConnection? {
        if (!isShizukuAvailable) return null
        Log.i(TAG, "Using Shizuku server newProcess fallback")
        return legacyConnection
    }

    private fun shizukuVersion(): Int = try {
        Shizuku.getVersion()
    } catch (_: Exception) {
        -1
    }
}
