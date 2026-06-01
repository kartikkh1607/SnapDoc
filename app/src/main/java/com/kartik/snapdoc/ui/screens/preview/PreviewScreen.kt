package com.kartik.snapdoc.ui.screens.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kartik.snapdoc.domain.pipeline.ValidationCheck
import com.kartik.snapdoc.ui.components.DocPreviewHero
import com.kartik.snapdoc.ui.theme.Amber
import com.kartik.snapdoc.ui.theme.AmberDark
import com.kartik.snapdoc.ui.theme.AmberSoft
import com.kartik.snapdoc.ui.theme.ErrorRed
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.s3
import com.kartik.snapdoc.ui.theme.sGreen

@Composable
fun PreviewScreen(
    onBack: () -> Unit,
    onExport: (docId: String, imageUri: String) -> Unit,
    onPrintSheet: (docId: String, imageUri: String) -> Unit,
    viewModel: PreviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 56.dp, bottom = 160.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            ) {
                CircleIcon(Icons.AutoMirrored.Outlined.ArrowBack, onBack)
                Text(
                    text = "Your photo",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                CircleIcon(Icons.Outlined.Share, {})
            }

            Spacer(modifier = Modifier.height(12.dp))
            WatermarkedPreview(state = state)

            Spacer(modifier = Modifier.height(22.dp))
            VerificationChecklist(
                checks = state.checks,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))
            LockedPrintSheetCard(
                onClick = { onPrintSheet(viewModel.docId, viewModel.imageUri) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            )
        }

        ExportCta(
            entitled = state.entitlement.canExport,
            onClick = { onExport(viewModel.docId, viewModel.imageUri) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun WatermarkedPreview(state: PreviewUiState) {
    val doc = state.doc
    val aspect = if (doc != null) doc.dimensions.widthPx.toFloat() / doc.dimensions.heightPx else 0.82f
    val frameHeight = 260.dp
    val frameWidth = (frameHeight.value * aspect).dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = frameWidth, height = frameHeight)
                .s3(22.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(10.dp),
        ) {
            if (state.processedUri != null) {
                AsyncImage(
                    model = state.processedUri,
                    contentDescription = "Processed photo",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                DocPreviewHero(modifier = Modifier.fillMaxSize())
            }
            if (state.watermarked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(-22f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "SNAPDOC · SNAPDOC",
                        color = Primary.copy(alpha = 0.10f),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
        val chipColor = if (state.allPassed) Primary else ErrorRed
        val chipLabel = if (state.allPassed) "READY" else "REVIEW"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .sGreen(99.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(chipColor)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = chipLabel,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@Composable
private fun VerificationChecklist(checks: List<ValidationCheck>, modifier: Modifier = Modifier) {
    val items = checks.ifEmpty {
        listOf(ValidationCheck("Processing", "—", "Loading…", true))
    }
    Column(
        modifier = modifier
            .s1(20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
    ) {
        items.forEachIndexed { idx, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (item.passed) Primary else ErrorRed),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Text(
                        text = "${item.actual} · expected ${item.expected}",
                        color = if (item.passed) Ink3 else ErrorRed,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            if (idx != items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Hairline2),
                )
            }
        }
    }
}

@Composable
private fun LockedPrintSheetCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AmberSoft)
            .border(1.dp, AmberDark.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Amber),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Print,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Print sheet · 8 copies on A4",
                    color = AmberDark,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = AmberDark,
                    modifier = Modifier.size(12.dp),
                )
            }
            Text(
                text = "Save ₹40 at your local print shop. Included.",
                color = AmberDark.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ExportCta(entitled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .sGreen(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Primary)
                .clickable(onClick = onClick)
                .padding(horizontal = 22.dp),
        ) {
            Column {
                Text(
                    text = if (entitled) "Save & export" else "Export photo",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = if (entitled) "Save to gallery · share" else "One-time · no subscription",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            if (!entitled) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "₹49",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = Ink4,
                modifier = Modifier.size(13.dp),
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "Secure payment · 1.2M happy customers",
                color = Ink4,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .s1(14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
    }
}
