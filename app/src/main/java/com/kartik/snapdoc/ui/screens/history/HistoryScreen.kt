package com.kartik.snapdoc.ui.screens.history

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kartik.snapdoc.R
import com.kartik.snapdoc.ui.theme.ErrorRed
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import java.io.File

private val Gutter = 22.dp

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(top = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.history_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(horizontal = Gutter, vertical = 12.dp)
                .semantics { heading() },
        )
        if (state.rows.isNotEmpty()) {
            Text(
                text = stringResource(R.string.history_count_template, state.rows.size),
                color = Ink3,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = Gutter, vertical = 4.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = Gutter, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.rows, key = { it.item.uri.toString() }) { row ->
                    HistoryCard(
                        row = row,
                        onShare = { uri ->
                            val shareUri = shareableUri(context, uri) ?: return@HistoryCard
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, shareUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.history_share)))
                        },
                        onDelete = { viewModel.delete(row.item.uri) },
                    )
                }
            }
        } else {
            EmptyState()
        }
    }
}

@Composable
private fun HistoryCard(
    row: HistoryRow,
    onShare: (Uri) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(3.dp)
                .clip(RoundedCornerShape(8.dp)),
        ) {
            AsyncImage(
                model = row.item.uri,
                contentDescription = row.doc?.displayName,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.doc?.displayName ?: row.item.docId,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
            )
            val relative = DateUtils
                .getRelativeTimeSpanString(row.item.createdAtMs)
                .toString()
            Text(
                text = stringResource(
                    R.string.history_item_meta,
                    relative,
                    formatSize(row.item.sizeBytes),
                ),
                color = Ink3,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
        IconButton(
            label = stringResource(R.string.history_share),
            icon = Icons.Outlined.Share,
            tint = Primary,
            onClick = { onShare(row.item.uri) },
        )
        IconButton(
            label = stringResource(R.string.history_delete),
            icon = Icons.Outlined.Delete,
            tint = ErrorRed,
            onClick = onDelete,
        )
    }
}

@Composable
private fun IconButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.10f))
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(PrimaryFaint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.History,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(32.dp),
                )
            }
            Text(
                text = stringResource(R.string.history_empty_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.history_empty_subtitle),
                color = Ink4,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun formatSize(bytes: Long): String {
    val kb = bytes / 1024.0
    return if (kb < 1024) "${kb.toInt()} KB" else "%.1f MB".format(kb / 1024.0)
}

private fun shareableUri(context: android.content.Context, uri: Uri): Uri? {
    // file:// URIs can't cross app boundaries on N+. Route through FileProvider.
    val path = uri.path ?: return null
    val file = File(path)
    if (!file.exists()) return null
    return runCatching {
        FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            file,
        )
    }.getOrNull()
}
