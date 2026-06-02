package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

class CompressedImage(val bytes: ByteArray, val qualityUsed: Int, val sizeKb: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompressedImage) return false
        return qualityUsed == other.qualityUsed &&
            sizeKb == other.sizeKb &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + qualityUsed
        result = 31 * result + sizeKb
        return result
    }
}

@Singleton
class FileSizeCompressor @Inject constructor() {

    fun compressToTarget(bitmap: Bitmap, minKb: Int, maxKb: Int): CompressedImage {
        var current = bitmap
        var attempts = 0

        while (attempts < 5) {
            val result = searchQuality(current, minKb, maxKb)
            if (result != null) {
                return result
            }
            // Too large even at quality 30 -> downscale 10%, retry.
            val w = (current.width * 9 / 10).coerceAtLeast(1)
            val h = (current.height * 9 / 10).coerceAtLeast(1)
            val next = Bitmap.createScaledBitmap(current, w, h, true)
            if (current !== bitmap) current.recycle()
            current = next
            attempts++
        }

        // Final fallback: lowest quality, accept whatever size.
        val out = ByteArrayOutputStream()
        current.compress(Bitmap.CompressFormat.JPEG, 30, out)
        val bytes = out.toByteArray()
        return CompressedImage(bytes, 30, bytes.size.toKbRounded())
    }

    private fun searchQuality(bitmap: Bitmap, minKb: Int, maxKb: Int): CompressedImage? {
        var lo = 30
        var hi = 100
        var best: CompressedImage? = null

        while (lo <= hi) {
            val mid = (lo + hi) / 2
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, mid, out)
            val bytes = out.toByteArray()
            val sizeKb = bytes.size.toKbRounded()
            when {
                sizeKb in minKb..maxKb -> {
                    best = CompressedImage(bytes, mid, sizeKb)
                    lo = mid + 1
                }
                sizeKb < minKb -> lo = mid + 1
                else -> hi = mid - 1
            }
        }
        return best
    }

    private fun Int.toKbRounded(): Int = (this + 512) / 1024
}
