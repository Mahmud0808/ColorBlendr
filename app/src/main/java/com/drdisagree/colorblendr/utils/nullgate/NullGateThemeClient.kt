package com.drdisagree.colorblendr.utils.nullgate

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.security.MessageDigest

/** Typed NullGate client. It never receives a shell, file access, or root. */
object NullGateThemeClient {
    private const val NULLGATE_PACKAGE = "org.nullprotocol.nullgate"
    private const val NULLGATE_ACTIVITY = "org.nullprotocol.nullgate.ClientRequestActivity"
    private const val NULLGATE_SIGNER =
        "fc122f22e4716ba03cae81893783e437553d4a3ad21e897f4efe3367c415bafe"
    private const val ACTION_REQUEST =
        "org.nullprotocol.nullgate.action.REQUEST_THEME_LEASE"
    private const val ACTION_REVOKE =
        "org.nullprotocol.nullgate.action.REVOKE_LEASE"
    private const val ACTION_RECONCILE =
        "org.nullprotocol.nullgate.action.RECONCILE_THEME_LEASE"
    private const val STORE = "nullgate_client"
    private const val DEFAULT_DURATION_MILLIS = 60_000L

    private const val PHASE_IDLE = "IDLE"
    private const val PHASE_REQUESTING = "REQUESTING"
    private const val PHASE_ACTIVE = "ACTIVE"
    private const val PHASE_REVOKING = "REVOKING"
    private const val PHASE_REVOKING_FOR_REQUEST = "REVOKING_FOR_REQUEST"
    private const val PHASE_RECONCILING = "RECONCILING"
    private const val PHASE_UNKNOWN = "UNKNOWN"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var launcher: ((Intent) -> Unit)? = null
    private var notifier: ((String) -> Unit)? = null
    private var foreground = false
    private var inFlightInThisProcess = false
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

    fun setForeground(value: Boolean) { foreground = value }

    /** Called after Activity results have had a chance to run on the main queue. */
    fun reconcileIfNeededOnResume() {
        mainHandler.post {
            if (!::appContext.isInitialized || !foreground) return@post
            if (inFlightInThisProcess) {
                mainHandler.postDelayed({
                    if (foreground && inFlightInThisProcess) {
                        inFlightInThisProcess = false
                        reconcileIfNeededOnResume()
                    }
                }, 1_000L)
                return@post
            }
            val prefs = prefs()
            val phase = prefs.getString("phase", PHASE_IDLE) ?: PHASE_UNKNOWN
            val leaseId = prefs.getString("active_lease", null)
            val expires = prefs.getLong("active_expires_elapsed", 0L)
            val inconsistentActive = phase == PHASE_ACTIVE &&
                (leaseId.isNullOrEmpty() || expires <= SystemClock.elapsedRealtime())
            val inconsistentIdle = phase == PHASE_IDLE && leaseId != null
            if (phase in setOf(
                    PHASE_REQUESTING, PHASE_REVOKING, PHASE_REVOKING_FOR_REQUEST,
                    PHASE_RECONCILING, PHASE_UNKNOWN
                ) || inconsistentActive || inconsistentIdle
            ) {
                val resumeRequest = phase == PHASE_REVOKING_FOR_REQUEST ||
                    prefs.getBoolean("resume_after_reconcile", false)
                if (!prefs.edit()
                        .putString("phase", PHASE_RECONCILING)
                        .putBoolean("resume_after_reconcile", resumeRequest)
                        .commit()
                ) {
                    markUnknown("NullGate reconciliation state could not be saved.")
                    return@post
                }
                launch(reconcileIntent())
            }
        }
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
        if (!readyToLaunch() || (seedArgb ushr 24) != 0xff || themeStyle == "CMF")
            return false
        val prefs = prefs()
        val phase = prefs.getString("phase", PHASE_IDLE) ?: PHASE_UNKNOWN
        val activeLease = prefs.getString("active_lease", null)
        if (phase != PHASE_IDLE && phase != PHASE_ACTIVE) return false
        if ((phase == PHASE_ACTIVE) != (activeLease != null)) {
            markUnknown("NullGate local lease state is inconsistent; reconciliation required.")
            return false
        }

        val nextPhase = if (activeLease != null) PHASE_REVOKING_FOR_REQUEST else PHASE_REQUESTING
        if (!prefs.edit()
                .putInt("pending_seed", seedArgb)
                .putString("pending_style", themeStyle)
                .putLong("pending_duration", DEFAULT_DURATION_MILLIS)
                .putString("phase", nextPhase)
                .commit()
        ) return false
        return launch(if (activeLease != null) revokeIntent(activeLease) else pendingRequestIntent())
    }

    fun revokeActiveLease(): Boolean {
        if (!readyToLaunch()) return false
        val prefs = prefs()
        val leaseId = prefs.getString("active_lease", null)
        val phase = prefs.getString("phase", PHASE_IDLE) ?: PHASE_UNKNOWN
        if (leaseId == null) return phase == PHASE_IDLE
        if (phase != PHASE_ACTIVE) return false
        if (!prefs.edit().putString("phase", PHASE_REVOKING).commit()) return false
        return launch(revokeIntent(leaseId))
    }

    fun handleResult(resultCode: Int, data: Intent?) {
        if (!::appContext.isInitialized) return
        inFlightInThisProcess = false
        val phase = prefs().getString("phase", PHASE_UNKNOWN) ?: PHASE_UNKNOWN
        val keys: Set<String>?
        val decision: String?
        val leaseId: String?
        val expiresElapsed: Long
        try {
            keys = data?.extras?.keySet()
            decision = data?.getStringExtra(NullGateResultPolicy.EXTRA_DECISION)
            leaseId = data?.getStringExtra(NullGateResultPolicy.EXTRA_LEASE_ID)
            expiresElapsed = data?.getLongExtra(
                NullGateResultPolicy.EXTRA_EXPIRES_ELAPSED, -1L
            ) ?: -1L
        } catch (_: RuntimeException) {
            markUnknown("NullGate returned a malformed result; reconciliation is required.")
            return
        }
        when (phase) {
            PHASE_REQUESTING -> handleRequestResult(
                resultCode, keys, decision, leaseId, expiresElapsed
            )
            PHASE_REVOKING, PHASE_REVOKING_FOR_REQUEST ->
                handleRevokeResult(phase, resultCode, keys, decision)
            PHASE_RECONCILING -> handleReconcileResult(resultCode, keys, decision)
            else -> markUnknown("NullGate returned a result for an unexpected operation.")
        }
    }

    private fun handleRequestResult(
        resultCode: Int,
        keys: Set<String>?,
        decision: String?,
        leaseId: String?,
        expiresElapsed: Long
    ) {
        val evaluated = NullGateResultPolicy.evaluateRequest(
            resultCode == Activity.RESULT_OK,
            resultCode == Activity.RESULT_CANCELED,
            keys,
            decision,
            leaseId,
            expiresElapsed,
            SystemClock.elapsedRealtime()
        )
        when (evaluated.outcome) {
            NullGateResultPolicy.Outcome.GRANT -> {
                val receipt = evaluated.receipt
                    ?: return markUnknown("NullGate grant receipt is missing.")
                if (prefs().edit()
                        .putString("active_lease", receipt.leaseId)
                        .putLong("active_expires_elapsed", receipt.expiresElapsed)
                        .remove("pending_seed").remove("pending_style").remove("pending_duration")
                        .remove("resume_after_reconcile")
                        .putString("phase", PHASE_ACTIVE)
                        .commit()
                ) notify("NullGate granted the 60-second theme lease.")
                else markUnknown("NullGate granted a lease, but its receipt could not be saved.")
            }
            NullGateResultPolicy.Outcome.CLEAN -> {
                if (clearCleanState()) notify("NullGate did not grant the theme lease: $decision")
                else markUnknown("NullGate denial was confirmed, but local state could not be saved.")
            }
            NullGateResultPolicy.Outcome.UNKNOWN ->
                markUnknown("NullGate result is uncertain; reconciliation is required.")
        }
    }

    private fun handleRevokeResult(
        previousPhase: String,
        resultCode: Int,
        keys: Set<String>?,
        decision: String?
    ) {
        if (!NullGateResultPolicy.confirmedRevoke(
                resultCode == Activity.RESULT_OK, keys, decision
            )
        ) {
            markUnknown("NullGate could not confirm revocation: ${decision ?: "NO_RESULT"}")
            return
        }
        val replace = previousPhase == PHASE_REVOKING_FOR_REQUEST
        val edit = prefs().edit()
            .remove("active_lease").remove("active_expires_elapsed")
            .putString("phase", if (replace) PHASE_REQUESTING else PHASE_IDLE)
        if (!replace) edit.remove("pending_seed").remove("pending_style")
            .remove("pending_duration").remove("resume_after_reconcile")
        if (!edit.commit()) {
            markUnknown("NullGate cleanup was confirmed, but local state could not be saved.")
            return
        }
        if (replace) launch(pendingRequestIntent())
        else notify("NullGate revoked the active theme lease.")
    }

    private fun handleReconcileResult(
        resultCode: Int,
        keys: Set<String>?,
        decision: String?
    ) {
        if (NullGateResultPolicy.evaluateReconciliation(
                resultCode == Activity.RESULT_CANCELED, keys, decision
            ) != NullGateResultPolicy.Outcome.CLEAN
        ) {
            markUnknown("NullGate reconciliation is still uncertain.")
            return
        }
        val prefs = prefs()
        val resumeRequest = prefs.getBoolean("resume_after_reconcile", false)
        val hasPending = prefs.contains("pending_seed") && prefs.contains("pending_style")
        val edit = prefs.edit()
            .remove("active_lease").remove("active_expires_elapsed")
            .remove("resume_after_reconcile")
            .putString("phase", if (resumeRequest && hasPending) PHASE_REQUESTING else PHASE_IDLE)
        if (!resumeRequest || !hasPending) edit.remove("pending_seed").remove("pending_style")
            .remove("pending_duration")
        if (!edit.commit()) {
            markUnknown("NullGate reconciliation succeeded, but local state could not be saved.")
            return
        }
        if (resumeRequest && hasPending) launch(pendingRequestIntent())
        else notify("NullGate reconciled its previous lease state.")
    }

    private fun clearCleanState(): Boolean = prefs().edit()
        .clear().putString("phase", PHASE_IDLE).commit()

    private fun markUnknown(message: String) {
        val prefs = prefs()
        val replacePending = prefs.getString("phase", PHASE_UNKNOWN) ==
            PHASE_REVOKING_FOR_REQUEST || prefs.getBoolean("resume_after_reconcile", false)
        prefs.edit().putString("phase", PHASE_UNKNOWN)
            .putBoolean("resume_after_reconcile", replacePending).commit()
        notify(message)
    }

    private fun readyToLaunch(): Boolean = ::appContext.isInitialized && foreground &&
        launcher != null && !inFlightInThisProcess && trustedControllerInstalled(appContext)

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

    private fun reconcileIntent(): Intent = explicit(ACTION_RECONCILE)
        .putExtra("protocolVersion", 1)

    private fun explicit(action: String) = Intent(action).setComponent(
        ComponentName(NULLGATE_PACKAGE, NULLGATE_ACTIVITY)
    )

    private fun launch(intent: Intent): Boolean {
        val currentLauncher = launcher ?: return false
        inFlightInThisProcess = true
        mainHandler.post {
            if (!foreground || launcher !== currentLauncher) {
                inFlightInThisProcess = false
                markUnknown("NullGate request could not be launched while ColorBlendr was visible.")
                return@post
            }
            try {
                currentLauncher(intent)
            } catch (_: RuntimeException) {
                inFlightInThisProcess = false
                markUnknown("NullGate request launch failed; reconciliation is required.")
            }
        }
        return true
    }

    private fun notify(message: String) { mainHandler.post { notifier?.invoke(message) } }

    private fun prefs() = appContext.getSharedPreferences(STORE, Context.MODE_PRIVATE)

    private fun sha256(value: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(value)
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
