package com.kartik.snapdoc.data.history

import android.content.Context
import android.net.Uri
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class HistoryItem(
    val uri: Uri,
    val docId: String,
    val createdAtMs: Long,
    val sizeBytes: Long,
)

/**
 * Lists processed photos that PhotoProcessor wrote to the app cache directory.
 * Files are named `snapdoc_processed_<docId>_<ts>.jpg`. Newest first.
 *
 * The cache dir can be wiped by the OS at any time — that's expected, and the
 * History screen will just go back to empty in that case.
 */
interface HistoryRepository {
    suspend fun list(): List<HistoryItem>
    suspend fun delete(uri: Uri): Boolean
}

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryRepositoryModule {
    @Binds
    @Singleton
    abstract fun bind(impl: DefaultHistoryRepository): HistoryRepository
}

@Singleton
class DefaultHistoryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : HistoryRepository {

    override suspend fun list(): List<HistoryItem> = withContext(Dispatchers.IO) {
        val files = context.cacheDir.listFiles { f -> f.isFile && f.name.startsWith(PREFIX) && f.name.endsWith(SUFFIX) }
            ?: return@withContext emptyList()
        files
            .mapNotNull { f -> toItem(f) }
            .sortedByDescending { it.createdAtMs }
    }

    override suspend fun delete(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val path = uri.path ?: return@withContext false
        runCatching { File(path).delete() }.getOrDefault(false)
    }

    private fun toItem(file: File): HistoryItem? {
        // Filename: snapdoc_processed_<docId>_<ts>.jpg. The docId itself can
        // contain underscores (e.g. in_passport, schengen_visa) so we parse
        // by trimming the known prefix and splitting from the right.
        val core = file.name.removePrefix(PREFIX).removeSuffix(SUFFIX)
        val lastUnderscore = core.lastIndexOf('_')
        if (lastUnderscore <= 0) return null
        val docId = core.substring(0, lastUnderscore)
        val ts = core.substring(lastUnderscore + 1).toLongOrNull() ?: file.lastModified()
        return HistoryItem(
            uri = Uri.fromFile(file),
            docId = docId,
            createdAtMs = ts,
            sizeBytes = file.length(),
        )
    }

    private companion object {
        const val PREFIX = "snapdoc_processed_"
        const val SUFFIX = ".jpg"
    }
}
