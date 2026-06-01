package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class CompressedImage(val bytes: ByteArray, val qualityUsed: Int, val sizeKb: Int)

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
        return CompressedImage(bytes, 30, bytes.size / 1024)
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
            val sizeKb = bytes.size / 1024
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
}
