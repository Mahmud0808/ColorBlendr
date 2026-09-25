package com.drdisagree.colorblendr.utils.nullgate

internal object NullGateResultPolicy {
    const val EXTRA_DECISION = "decision"
    const val EXTRA_LEASE_ID = "leaseId"
    const val EXTRA_EXPIRES_ELAPSED = "expiresAtElapsedMillis"

    enum class Outcome { GRANT, CLEAN, UNKNOWN }

    data class Receipt(val leaseId: String, val expiresElapsed: Long)
    data class Evaluation(val outcome: Outcome, val receipt: Receipt? = null)

    private val decisionKeys = setOf(EXTRA_DECISION)
    private val grantKeys = setOf(EXTRA_DECISION, EXTRA_LEASE_ID, EXTRA_EXPIRES_ELAPSED)
    private val confirmedCleanDenials = setOf(
        "DENIED_BY_USER", "DENIED_CALLER_CHANGED", "DENIED_RECOVERY_RECORD_FAILED",
        "DENIED_INVALID_CLIENT_REQUEST", "INVALID_TIME_WINDOW",
        "DURATION_EXCEEDS_POLICY", "TARGET_CAPABILITY_DENIED", "TARGET_NOT_INSTALLED",
        "ADAPTER_UNAVAILABLE", "ADAPTER_ACTIVATION_FAILED", "NONCE_REPLAY",
        "LEASE_ID_REUSE", "EXPIRED", "CALLER_PACKAGE_MISMATCH",
        "CALLER_CERTIFICATE_MISMATCH", "CAPACITY_EXHAUSTED", "BROKER_CLOSED"
    )

    fun evaluateRequest(
        resultOk: Boolean,
        resultCanceled: Boolean,
        keys: Set<String>?,
        decision: String?,
        leaseId: String?,
        expiresElapsed: Long,
        nowElapsed: Long
    ): Evaluation {
        if (resultOk && decision == "GRANTED" && keys == grantKeys &&
            leaseId?.matches(Regex("[A-Za-z0-9_-]{16,128}")) == true &&
            expiresElapsed > nowElapsed && expiresElapsed - nowElapsed <= 60_000L
        ) return Evaluation(Outcome.GRANT, Receipt(leaseId, expiresElapsed))

        if (resultCanceled && keys == decisionKeys && decision in confirmedCleanDenials)
            return Evaluation(Outcome.CLEAN)
        return Evaluation(Outcome.UNKNOWN)
    }

    fun confirmedRevoke(
        resultOk: Boolean,
        keys: Set<String>?,
        decision: String?
    ): Boolean = resultOk && keys == decisionKeys && decision == "REVOKED"

    fun evaluateReconciliation(
        resultCanceled: Boolean,
        keys: Set<String>?,
        decision: String?
    ): Outcome = if (resultCanceled && keys == decisionKeys &&
        (decision == "NOT_FOUND" || decision == "REVOKED_AFTER_UNCERTAIN_RESULT")
    ) Outcome.CLEAN else Outcome.UNKNOWN
}
