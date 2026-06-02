package com.kartik.snapdoc.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kartik.snapdoc.data.specs.model.DocumentSpec

@Composable
fun DocumentCard(
    doc: DocumentSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = doc.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = doc.shortSpecSummary(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun PopularDocumentCard(
    doc: DocumentSpec,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
            )
            Text(
                text = doc.shortName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${doc.dimensions.widthMm.toInt()}×${doc.dimensions.heightMm.toInt()}mm",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            )
        }
    }
}

fun DocumentSpec.shortSpecSummary(): String {
    val w = dimensions.widthMm.toInt()
    val h = dimensions.heightMm.toInt()
    return "${w}×${h}mm · ${background.displayName} · ${file.minSizeKb}–${file.maxSizeKb} KB"
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun DocumentCardPreview() {
    com.kartik.snapdoc.ui.theme.SnapDocTheme {
        DocumentCard(doc = previewSpec(), onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PopularDocumentCardPreview() {
    com.kartik.snapdoc.ui.theme.SnapDocTheme {
        PopularDocumentCard(doc = previewSpec(), onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

private fun previewSpec(): DocumentSpec = DocumentSpec(
    id = "preview_passport",
    displayName = "Indian Passport",
    shortName = "Passport",
    categoryId = "in_government",
    popularity = 100,
    dimensions = com.kartik.snapdoc.data.specs.model.DimensionsSpec(35f, 45f, 413, 531, 300),
    background = com.kartik.snapdoc.data.specs.model.BackgroundSpec("#FFFFFF", "White", 5),
    face = com.kartik.snapdoc.data.specs.model.FaceSpec(70, 80, 50, 70),
    file = com.kartik.snapdoc.data.specs.model.FileSpec("JPG", 10, 100),
    rules = com.kartik.snapdoc.data.specs.model.RulesSpec(),
)
