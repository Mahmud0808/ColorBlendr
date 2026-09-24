package com.drdisagree.colorblendr.utils.nullgate

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import java.security.MessageDigest

/**
 * Typed ColorBlendr client for NullGate. This class can request only a temporary
 * system-theme capability; it never receives a shell, file access, or root.
 */
object NullGateThemeClient {
    private const val NULLGATE_PACKAGE = "org.nullprotocol.nullgate"
    private const val NULLGATE_ACTIVITY = "org.nullprotocol.nullgate.ClientRequestActivity"
    private const val NULLGATE_SIGNER =
        "fc122f22e4716ba03cae81893783e437553d4a3ad21e897f4efe3367c415bafe"
    private const val ACTION_REQUEST =
        "org.nullprotocol.nullgate.action.REQUEST_THEME_LEASE"
    private const val ACTION_REVOKE =
        "org.nullprotocol.nullgate.action.REVOKE_LEASE"
    private const val STORE = "nullgate_client"
    private const val DEFAULT_DURATION_MILLIS = 60_000L

    private const val PHASE_IDLE = "IDLE"
    private const val PHASE_REQUESTING = "REQUESTING"
    private const val PHASE_REVOKING = "REVOKING"
    private const val PHASE_REVOKING_FOR_REQUEST = "REVOKING_FOR_REQUEST"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var launcher: ((Intent) -> Unit)? = null
    private var notifier: ((String) -> Unit)? = null
    private var foreground = false
    private lateinit var appContext: Context

    fun bind(
        context: Context,
        launchForResult: (Intent) -> Unit,
        notifyUser: (String) -> Unit
    ) {
        appContext = context.applicationContext
        launcher = launchForResult
        notifier = notifyUser
    }

    fun unbind() {
        launcher = null
        notifier = null
        foreground = false
    }

    fun setForeground(value: Boolean) {
        foreground = value
    }

    fun trustedControllerInstalled(context: Context): Boolean {
        return try {
            val info = context.packageManager.getPackageInfo(
                NULLGATE_PACKAGE,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            val signers = info.signingInfo?.apkContentsSigners ?: return false
            signers.size == 1 && sha256(signers[0].toByteArray()) == NULLGATE_SIGNER
        } catch (_: Exception) {
            false
        }
    }

    fun requestTheme(seedArgb: Int, themeStyle: String): Boolean {
        if (!::appContext.isInitialized || !foreground || launcher == null) return false
        if (!trustedControllerInstalled(appContext)) return false
        if ((seedArgb ushr 24) != 0xff || themeStyle == "CMF") return false

        val prefs = prefs()
        if (prefs.getString("phase", PHASE_IDLE) != PHASE_IDLE) return false
        prefs.edit()
            .putInt("pending_seed", seedArgb)
            .putString("pending_style", themeStyle)
            .putLong("pending_duration", DEFAULT_DURATION_MILLIS)
            .apply()

        return if (prefs.getString("active_lease", null) != null) {
            prefs.edit().putString("phase", PHASE_REVOKING_FOR_REQUEST).commit()
            launch(revokeIntent(prefs.getString("active_lease", null)!!))
        } else {
            prefs.edit().putString("phase", PHASE_REQUESTING).commit()
            launch(pendingRequestIntent())
        }
    }

    fun revokeActiveLease(): Boolean {
        if (!::appContext.isInitialized || !foreground || launcher == null) return false
        if (!trustedControllerInstalled(appContext)) return false
        val prefs = prefs()
        val leaseId = prefs.getString("active_lease", null) ?: return true
        if (prefs.getString("phase", PHASE_IDLE) != PHASE_IDLE) return false
        prefs.edit().putString("phase", PHASE_REVOKING).commit()
        return launch(revokeIntent(leaseId))
    }

    fun handleResult(resultCode: Int, data: Intent?) {
        if (!::appContext.isInitialized) return
        val prefs = prefs()
        val phase = prefs.getString("phase", PHASE_IDLE) ?: PHASE_IDLE
        val decision = data?.getStringExtra("decision") ?: "NO_RESULT"

        when (phase) {
            PHASE_REQUESTING -> {
                if (resultCode == Activity.RESULT_OK && decision == "GRANTED") {
                    val leaseId = data?.getStringExtra("leaseId")
                    if (!leaseId.isNullOrEmpty()) {
                        prefs.edit()
                            .putString("active_lease", leaseId)
                            .putLong(
                                "active_expires_elapsed",
                                data.getLongExtra("expiresAtElapsedMillis", 0L)
                            )
                            .putString("phase", PHASE_IDLE)
                            .commit()
                        notify("NullGate granted the 60-second theme lease.")
                        return
                    }
                }
                prefs.edit().putString("phase", PHASE_IDLE).commit()
                notify("NullGate did not grant the theme lease: $decision")
            }

            PHASE_REVOKING, PHASE_REVOKING_FOR_REQUEST -> {
                if (resultCode == Activity.RESULT_OK && decision == "REVOKED") {
                    prefs.edit()
                        .remove("active_lease")
                        .remove("active_expires_elapsed")
                        .putString(
                            "phase",
                            if (phase == PHASE_REVOKING_FOR_REQUEST) PHASE_REQUESTING else PHASE_IDLE
                        )
                        .commit()
                    if (phase == PHASE_REVOKING_FOR_REQUEST) {
                        launch(pendingRequestIntent())
                    } else {
                        notify("NullGate revoked the active theme lease.")
                    }
                } else {
                    prefs.edit().putString("phase", PHASE_IDLE).commit()
                    notify("NullGate could not confirm revocation: $decision")
                }
            }

            else -> notify("NullGate returned an unexpected result: $decision")
        }
    }

    private fun pendingRequestIntent(): Intent {
        val prefs = prefs()
        return explicit(ACTION_REQUEST)
            .putExtra("protocolVersion", 1)
            .putExtra("seedArgb", prefs.getInt("pending_seed", 0))
            .putExtra("themeStyle", prefs.getString("pending_style", "TONAL_SPOT"))
            .putExtra("durationMillis", prefs.getLong("pending_duration", DEFAULT_DURATION_MILLIS))
    }

    private fun revokeIntent(leaseId: String): Intent = explicit(ACTION_REVOKE)
        .putExtra("protocolVersion", 1)
        .putExtra("leaseId", leaseId)

    private fun explicit(action: String) = Intent(action).setComponent(
        ComponentName(NULLGATE_PACKAGE, NULLGATE_ACTIVITY)
    )

    private fun launch(intent: Intent): Boolean {
        val currentLauncher = launcher ?: return false
        mainHandler.post { currentLauncher(intent) }
        return true
    }

    private fun notify(message: String) {
        mainHandler.post { notifier?.invoke(message) }
    }

    private fun prefs() = appContext.getSharedPreferences(STORE, Context.MODE_PRIVATE)

    private fun sha256(value: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(value)
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
