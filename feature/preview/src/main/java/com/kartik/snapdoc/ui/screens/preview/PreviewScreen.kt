package com.kartik.snapdoc.ui.screens.preview

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kartik.snapdoc.feature.preview.R
import com.kartik.snapdoc.core.designsystem.R as DesignR
import com.kartik.snapdoc.domain.pipeline.ValidationCheck
import com.kartik.snapdoc.domain.pipeline.ValidationCheckKind
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import com.kartik.snapdoc.ui.components.CircleIconButton
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PreviewScreen(
    onBack: () -> Unit,
    onExport: (docId: String, imageUri: String) -> Unit,
    onPrintSheet: (docId: String, imageUri: String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: PreviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareChooserTitle = stringResource(R.string.preview_share_chooser)
    val shareProcessed: () -> Unit = {
        val uri = state.processedUri
        if (uri != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, shareChooserTitle))
        }
    }
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
                CircleIcon(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    onClick = onBack,
                    contentDescription = stringResource(DesignR.string.cd_back),
                )
                Text(
                    text = stringResource(R.string.preview_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                CircleIcon(
                    icon = Icons.Outlined.Share,
                    onClick = shareProcessed,
                    contentDescription = stringResource(DesignR.string.cd_share),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            WatermarkedPreview(
                state = state,
                docId = viewModel.docId,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onRemoveWatermark = { onExport(viewModel.docId, viewModel.imageUri) },
            )

            Spacer(modifier = Modifier.height(22.dp))
            VerificationChecklist(
                checks = state.checks,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            )

            if (state.entitlement.canExport) {
                Spacer(modifier = Modifier.height(14.dp))
                PrintSheetCard(
                    unlocked = state.entitlement.canPrintSheet,
                    onClick = { onPrintSheet(viewModel.docId, viewModel.imageUri) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp),
                )
            }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun WatermarkedPreview(
    state: PreviewUiState,
    docId: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onRemoveWatermark: () -> Unit,
) {
    val doc = state.doc
    val aspect = if (doc != null) doc.dimensions.widthPx.toFloat() / doc.dimensions.heightPx else 0.82f
    val frameHeight = 260.dp
    val frameWidth = (frameHeight.value * aspect).dp

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = with(sharedTransitionScope) {
                Modifier
                    .size(width = frameWidth, height = frameHeight)
                    .sharedElement(
                        rememberSharedContentState(key = "photo-$docId"),
                        animatedVisibilityScope = animatedContentScope,
                    )
                    .s3(22.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(10.dp)
            },
        ) {
            if (state.processedUri != null) {
                AsyncImage(
                    model = state.processedUri,
                    contentDescription = stringResource(R.string.preview_cd_processed),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                DocPreviewHero(modifier = Modifier.fillMaxSize())
            }
            if (state.watermarked) {
                WatermarkOverlay(label = stringResource(R.string.preview_watermark_label))
            }
        }
        if (state.watermarked) {
            val ctaLabel = stringResource(
                R.string.preview_watermark_cta,
                stringResource(R.string.preview_export_price),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.Black.copy(alpha = 0.78f))
                    .clickable(
                        role = Role.Button,
                        onClickLabel = ctaLabel,
                        onClick = onRemoveWatermark,
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = ctaLabel,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
        val chipColor = if (state.allPassed) Primary else ErrorRed
        val chipLabel = stringResource(
            if (state.allPassed) R.string.preview_chip_ready else R.string.preview_chip_review,
        )
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
private fun WatermarkOverlay(label: String) {
    // Tiled diagonal watermark. Strong enough that users can see it on the
    // preview (and won't mistake it for a usable export) but uses theme primary
    // so it still feels like an in-product treatment rather than an alert.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .rotate(-22f),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            repeat(4) {
                Text(
                    text = "$label · $label · $label",
                    color = Primary.copy(alpha = 0.32f),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun VerificationChecklist(checks: List<ValidationCheck>, modifier: Modifier = Modifier) {
    val processingLabel = stringResource(R.string.preview_processing)
    val loadingLabel = stringResource(R.string.preview_loading)
    Column(
        modifier = modifier
            .s1(20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
    ) {
        if (checks.isEmpty()) {
            ChecklistRow(
                label = processingLabel,
                detail = loadingLabel,
                passed = true,
                showDivider = false,
            )
        } else {
            checks.forEachIndexed { idx, item ->
                ChecklistRow(
                    label = stringResource(item.kind.labelRes()),
                    detail = stringResource(R.string.preview_actual_expected, item.actual, item.expected),
                    passed = item.passed,
                    showDivider = idx != checks.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun ChecklistRow(label: String, detail: String, passed: Boolean, showDivider: Boolean) {
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
                .background(if (passed) Primary else ErrorRed),
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
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = detail,
                color = if (passed) Ink3 else ErrorRed,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
    if (showDivider) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Hairline2),
        )
    }
}

private fun ValidationCheckKind.labelRes(): Int = when (this) {
    ValidationCheckKind.Dimensions -> R.string.preview_check_dimensions
    ValidationCheckKind.FileSize -> R.string.preview_check_file_size
    ValidationCheckKind.Background -> R.string.preview_check_background
    ValidationCheckKind.FaceDetected -> R.string.preview_check_face
    ValidationCheckKind.HeadRatio -> R.string.preview_check_head_ratio
    ValidationCheckKind.EyeLine -> R.string.preview_check_eye_line
}

@Composable
private fun PrintSheetCard(
    unlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (unlocked) Primary else AmberDark
    val background = if (unlocked) MaterialTheme.colorScheme.surface else AmberSoft
    val border = if (unlocked) Hairline2 else AmberDark.copy(alpha = 0.45f)
    val label = stringResource(
        if (unlocked) R.string.preview_print_sheet_title_unlocked
        else R.string.preview_print_sheet_title_locked,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (unlocked) Primary else Amber),
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
                    text = stringResource(
                        if (unlocked) R.string.preview_print_sheet_title_unlocked
                        else R.string.preview_print_sheet_title_locked,
                    ),
                    color = accentColor,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                if (!unlocked) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Text(
                text = stringResource(
                    if (unlocked) R.string.preview_print_sheet_subtitle_unlocked
                    else R.string.preview_print_sheet_subtitle_locked,
                ),
                color = accentColor.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ExportCta(entitled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(
        if (entitled) R.string.preview_export_save_title
        else R.string.preview_export_paid_title,
    )
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
                .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
                .padding(horizontal = 22.dp),
        ) {
            Column {
                Text(
                    text = stringResource(
                        if (entitled) R.string.preview_export_save_title
                        else R.string.preview_export_paid_title,
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = stringResource(
                        if (entitled) R.string.preview_export_save_subtitle
                        else R.string.preview_export_paid_subtitle,
                    ),
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
                        text = stringResource(R.string.preview_export_price),
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
                text = stringResource(R.string.preview_footer_trust),
                color = Ink4,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

// Local alias for the shared CircleIconButton — keeps the existing call sites
// terse while routing through the canonical component.
@Composable
private fun CircleIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
) = CircleIconButton(icon = icon, onClick = onClick, contentDescription = contentDescription)
