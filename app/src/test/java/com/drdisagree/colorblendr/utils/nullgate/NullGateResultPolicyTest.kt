package com.drdisagree.colorblendr.utils.nullgate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NullGateResultPolicyTest {
    private val decision = setOf(NullGateResultPolicy.EXTRA_DECISION)
    private val grant = setOf(
        NullGateResultPolicy.EXTRA_DECISION,
        NullGateResultPolicy.EXTRA_LEASE_ID,
        NullGateResultPolicy.EXTRA_EXPIRES_ELAPSED
    )
    private val lease = "lease_0123456789abcdef"

    @Test fun acceptsOnlyFreshExactGrant() {
        val accepted = NullGateResultPolicy.evaluateRequest(
            true, false, grant, "GRANTED", lease, 70_000L, 10_000L
        )
        assertEquals(NullGateResultPolicy.Outcome.GRANT, accepted.outcome)
        assertEquals(lease, accepted.receipt?.leaseId)
        assertEquals(70_000L, accepted.receipt?.expiresElapsed)

        assertEquals(NullGateResultPolicy.Outcome.UNKNOWN,
            NullGateResultPolicy.evaluateRequest(
                true, false, decision, "GRANTED", lease, 70_000L, 10_000L
            ).outcome)
        assertEquals(NullGateResultPolicy.Outcome.UNKNOWN,
            NullGateResultPolicy.evaluateRequest(
                true, false, grant, "GRANTED", lease, 70_001L, 10_000L
            ).outcome)
    }

    @Test fun clearsOnlyExactConfirmedDenial() {
        assertEquals(NullGateResultPolicy.Outcome.CLEAN,
            NullGateResultPolicy.evaluateRequest(
                false, true, decision, "DENIED_BY_USER", null, -1L, 10_000L
            ).outcome)
        assertEquals(NullGateResultPolicy.Outcome.UNKNOWN,
            NullGateResultPolicy.evaluateRequest(
                false, true, decision, "CLEANUP_FAILED", null, -1L, 10_000L
            ).outcome)
        assertEquals(NullGateResultPolicy.Outcome.UNKNOWN,
            NullGateResultPolicy.evaluateRequest(
                false, true, decision, "FUTURE_CODE", null, -1L, 10_000L
            ).outcome)
    }

    @Test fun revokeAndReconciliationAreOperationSpecific() {
        assertTrue(NullGateResultPolicy.confirmedRevoke(true, decision, "REVOKED"))
        assertFalse(NullGateResultPolicy.confirmedRevoke(false, decision, "REVOKED"))
        assertEquals(NullGateResultPolicy.Outcome.CLEAN,
            NullGateResultPolicy.evaluateReconciliation(true, decision, "NOT_FOUND"))
        assertEquals(NullGateResultPolicy.Outcome.CLEAN,
            NullGateResultPolicy.evaluateReconciliation(
                true, decision, "REVOKED_AFTER_UNCERTAIN_RESULT"
            ))
        assertEquals(NullGateResultPolicy.Outcome.UNKNOWN,
            NullGateResultPolicy.evaluateReconciliation(true, decision, "GRANTED"))
    }
}
