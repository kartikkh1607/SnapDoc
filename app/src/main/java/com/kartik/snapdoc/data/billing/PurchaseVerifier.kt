package com.kartik.snapdoc.data.billing

import android.util.Base64
import android.util.Log
import com.kartik.snapdoc.BuildConfig
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates that a Play Billing purchase JSON was actually signed by Google
 * Play, using the base64 RSA public key from the Play Console.
 *
 * The key comes from `local.properties` (`billing.licenseKey=...`) via the
 * [BuildConfig.BILLING_LICENSE_KEY] field. When the key is absent — fresh
 * clones, CI without secrets, debug-only builds — the verifier returns
 * permissive so the app stays usable, but [logsKeyMissingOnce] makes it
 * obvious in logcat that we aren't actually checking signatures.
 *
 * This is a defense-in-depth client check, not a substitute for server-side
 * verification: a determined attacker with control of the device can still
 * fake the result by patching the APK. It does, however, raise the bar past
 * trivial purchase-spoofing scripts.
 */
@Singleton
class PurchaseVerifier @Inject constructor() {

    @Volatile
    private var warned = false

    private val publicKey: PublicKey? by lazy {
        val raw = BuildConfig.BILLING_LICENSE_KEY
        if (raw.isBlank()) return@lazy null
        runCatching {
            val decoded = Base64.decode(raw, Base64.DEFAULT)
            KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
                .generatePublic(X509EncodedKeySpec(decoded))
        }.getOrElse {
            Log.e(TAG, "Failed to decode billing license key — purchases won't be verified", it)
            null
        }
    }

    fun verify(signedData: String, signature: String): Boolean {
        if (signedData.isBlank() || signature.isBlank()) return false
        val key = publicKey ?: run {
            logsKeyMissingOnce()
            return true
        }
        return runCatching {
            val sig = Signature.getInstance(SIGNATURE_ALGORITHM).apply {
                initVerify(key)
                update(signedData.toByteArray(Charsets.UTF_8))
            }
            sig.verify(Base64.decode(signature, Base64.DEFAULT))
        }.getOrElse {
            Log.w(TAG, "Signature verification threw", it)
            false
        }
    }

    private fun logsKeyMissingOnce() {
        if (warned) return
        warned = true
        Log.w(TAG, "billing.licenseKey absent — accepting purchases unverified. Set it in local.properties.")
    }

    private companion object {
        const val TAG = "PurchaseVerifier"
        const val KEY_FACTORY_ALGORITHM = "RSA"
        const val SIGNATURE_ALGORITHM = "SHA1withRSA"
    }
}
