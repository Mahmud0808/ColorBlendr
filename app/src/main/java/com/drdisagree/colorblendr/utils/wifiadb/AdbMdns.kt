package com.drdisagree.colorblendr.utils.wifiadb

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.util.Collections

/**
 * Class for managing ADB over mDNS (Multicast DNS) service discovery.
 *
 * @param context The application context.
 * @param callback The callback interface to handle detected ADB services.
 */
@Suppress("DEPRECATION")
class AdbMdns(context: Context, private val callback: AdbFoundCallback) {

    private val resolvedServices: MutableSet<String?> = HashSet()

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var running = false
    private var stopResolving = false
    private val handler: Handler = Handler(Looper.getMainLooper())

    private val pairingListener = DiscoveryListener(TLS_PAIRING)
    private val connectListener = DiscoveryListener(TLS_CONNECT)

    /**
     * Callback interface for handling detected ADB services.
     */
    interface AdbFoundCallback {
        /**
         * Called when a pairing code service is detected.
         *
         * @param ipAddress The IP address of the service.
         * @param port The port number of the service.
         */
        fun onPairingCodeDetected(ipAddress: String, port: Int)

        /**
         * Called when a connect code service is detected.
         *
         * @param ipAddress The IP address of the service.
         * @param port The port number of the service.
         */
        fun onConnectCodeDetected(ipAddress: String, port: Int)
    }

    /**
     * Starts the mDNS service discovery for ADB services.
     */
    fun start() {
        if (running) return

        try {
            nsdManager.discoverServices(TLS_PAIRING, NsdManager.PROTOCOL_DNS_SD, pairingListener)
            nsdManager.discoverServices(TLS_CONNECT, NsdManager.PROTOCOL_DNS_SD, connectListener)
            running = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service discovery: ", e)
            running = false
        }
    }

    /**
     * Stops the mDNS service discovery for ADB services.
     */
    fun stop() {
        if (!running) return

        try {
            nsdManager.stopServiceDiscovery(pairingListener)
            nsdManager.stopServiceDiscovery(connectListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping service discovery: ", e)
        }

        resolvedServices.clear()
        handler.removeCallbacksAndMessages(null)
        stopResolving = false
        running = false
    }

    /**
     * Checks if the resolved service matches the current network.
     *
     * @param resolvedService The resolved service information.
     * @return True if the service matches the current network, false otherwise.
     */
    private fun isMatchingNetwork(resolvedService: NsdServiceInfo): Boolean {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces)) {
                if (networkInterface.isUp()) {
                    for (inetAddress in Collections.list(networkInterface.inetAddresses)) {
                        if (resolvedService.host.hostAddress == inetAddress.hostAddress) {
                            return true
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * Checks if the specified port is available.
     *
     * @param port The port number to check.
     * @return True if the port is available, false otherwise.
     */
    private fun isPortAvailable(port: Int): Boolean {
        try {
            ServerSocket().use { socket ->
                socket.bind(InetSocketAddress("127.0.0.1", port), 1)
                return false
            }
        } catch (_: IOException) {
            return true
        }
    }

    /**
     * Listener for mDNS service discovery events.
     *
     * @param serviceType The type of service to discover.
     */
    private inner class DiscoveryListener(
        private val serviceType: String
    ) : NsdManager.DiscoveryListener {

        override fun onDiscoveryStarted(serviceType: String?) {
            Log.v(TAG, "Discovery started: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.v(TAG, "Start discovery failed: $serviceType, $errorCode")
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Log.v(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Log.v(TAG, "Stop discovery failed: $serviceType, $errorCode")
        }

        override fun onServiceFound(info: NsdServiceInfo) {
            if (!running) return

            val serviceKey = "${info.host}:${info.port}"

            if (!resolvedServices.contains(serviceKey)) {
                resolvedServices.add(serviceKey)
                nsdManager.resolveService(info, ResolveListener(serviceType))

                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({ stopResolving = true }, 3 * 60 * 1000)
            } else if (!stopResolving) {
                nsdManager.resolveService(info, ResolveListener(serviceType))
            }
        }

        override fun onServiceLost(info: NsdServiceInfo?) {
            if (info == null || info.host == null) return

            val serviceKey = "${info.host.hostAddress}:${info.port}"
            resolvedServices.remove(serviceKey)
            Log.d(TAG, "Service lost: $serviceKey")
        }
    }

    /**
     * Listener for resolving mDNS services.
     *
     * @param serviceType The type of service being resolved.
     */
    private inner class ResolveListener(
        private val serviceType: String
    ) : NsdManager.ResolveListener {

        override fun onResolveFailed(nsdServiceInfo: NsdServiceInfo, errorCode: Int) {
            Log.v(
                TAG,
                "Resolve failed: " + nsdServiceInfo.serviceName + ", errorCode: " + errorCode
            )
        }

        override fun onServiceResolved(resolvedService: NsdServiceInfo) {
            if (running
                && isMatchingNetwork(resolvedService)
                && isPortAvailable(resolvedService.port)
            ) {
                val ipAddress = resolvedService.host.hostAddress
                val portNumber = resolvedService.port

                if (serviceType == TLS_PAIRING) {
                    callback.onPairingCodeDetected(ipAddress, portNumber)
                } else if (serviceType == TLS_CONNECT) {
                    callback.onConnectCodeDetected(ipAddress, portNumber)
                }
            }
        }
    }

    companion object {
        private const val TAG = "AdbMdns"
        const val TLS_CONNECT: String = "_adb-tls-connect._tcp"
        const val TLS_PAIRING: String = "_adb-tls-pairing._tcp"
    }
}