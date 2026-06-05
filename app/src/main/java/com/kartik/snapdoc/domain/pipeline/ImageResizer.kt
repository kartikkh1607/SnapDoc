package com.kartik.snapdoc.domain.pipeline

import android.graphics.Bitmap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageResizer @Inject constructor() {

    fun resize(source: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        if (source.width == targetWidth && source.height == targetHeight) return source
        var current = source
        var w = source.width
        var h = source.height

        while (w > targetWidth * 2 && h > targetHeight * 2) {
            val nextW = w / 2
            val nextH = h / 2
            val next = Bitmap.createScaledBitmap(current, nextW, nextH, true)
            if (current !== source) current.recycle()
            current = next
            w = nextW
            h = nextH
        }

        val out = Bitmap.createScaledBitmap(current, targetWidth, targetHeight, true)
        if (current !== source && current !== out) current.recycle()
        return out
    }
}
