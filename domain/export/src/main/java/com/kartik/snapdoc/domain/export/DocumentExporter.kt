package com.kartik.snapdoc.domain.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Saves PDFs (and other large files) to public Downloads, plus produces
 * FileProvider-backed URIs for sharing files that live in cache.
 */
@Singleton
class DocumentExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun savePdfToDownloads(source: File, displayName: String): Uri = withContext(Dispatchers.IO) {
        val bytes = source.readBytes()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/SnapDoc")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("Unable to create Downloads entry")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: error("Unable to open output stream")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri
        } else {
            @Suppress("DEPRECATION")
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SnapDoc")
            if (!dir.exists()) dir.mkdirs()
            val outFile = File(dir, displayName)
            FileOutputStream(outFile).use { it.write(bytes) }
            Uri.fromFile(outFile)
        }
    }

    /** FileProvider URI usable in ACTION_SEND intents. */
    fun shareUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
