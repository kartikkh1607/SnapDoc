package com.kartik.snapdoc.domain.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun saveToGallery(jpegBytes: ByteArray, docId: String): Uri = withContext(Dispatchers.IO) {
        val name = "snapdoc_${docId}_${System.currentTimeMillis()}.jpg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SnapDoc")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create MediaStore entry")
            resolver.openOutputStream(uri)?.use { it.write(jpegBytes) } ?: error("Unable to open output stream")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri
        } else {
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SnapDoc")
            if (!dir.exists()) dir.mkdirs()
            val outFile = File(dir, name)
            FileOutputStream(outFile).use { it.write(jpegBytes) }
            Uri.fromFile(outFile)
        }
    }
}
