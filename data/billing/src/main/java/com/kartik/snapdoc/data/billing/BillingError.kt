package com.kartik.snapdoc.data.billing

/**
 * User-visible billing failures. Emitted from [BillingClientWrapper.errors]
 * and surfaced by screens (typically as a snackbar).
 */
sealed interface BillingError {
    /** Final attempt to reach Play Billing failed. */
    data class ConnectionFailed(val debugMessage: String?) : BillingError

    /** [BillingClient] reported a non-OK / non-cancel response from a purchase flow. */
    data class PurchaseFailed(val responseCode: Int, val debugMessage: String?) : BillingError
}
