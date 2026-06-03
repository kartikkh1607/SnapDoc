package com.kartik.snapdoc.ui.screens.print_sheet

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kartik.snapdoc.R
import com.kartik.snapdoc.domain.print.SheetLayout
import com.kartik.snapdoc.domain.print.SheetSize
import com.kartik.snapdoc.ui.theme.AmberDark
import com.kartik.snapdoc.ui.theme.AmberSoft
import com.kartik.snapdoc.ui.theme.ErrorRed
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.s3
import com.kartik.snapdoc.ui.theme.sGreen

@Composable
fun PrintSheetScreen(
    onBack: () -> Unit,
    viewModel: PrintSheetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val share: (Uri, String) -> Unit = { uri, mime ->
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.printsheet_share_chooser)),
        )
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
                .padding(top = 56.dp, bottom = 130.dp),
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(R.string.printsheet_cd_back),
                )
                Text(
                    text = stringResource(R.string.printsheet_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                CircleIcon(
                    icon = Icons.Outlined.Share,
                    onClick = {
                        val uri = state.shareUri
                        val mime = state.shareMime
                        if (uri != null && mime != null) share(uri, mime)
                    },
                    tint = if (state.shareUri != null) Primary else Ink4,
                    contentDescription = stringResource(R.string.printsheet_cd_share),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Header(state = state, modifier = Modifier.padding(horizontal = 22.dp))

            Spacer(modifier = Modifier.height(14.dp))
            SheetPicker(
                selected = state.sheet,
                onSelect = viewModel::selectSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            )

            Spacer(modifier = Modifier.height(14.dp))
            SheetPreview(
                state = state,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            if (state.locked) {
                Spacer(modifier = Modifier.height(14.dp))
                LockedBanner(modifier = Modifier.padding(horizontal = 22.dp))
            }

            if (state.phase == PrintExportPhase.Saved) {
                Spacer(modifier = Modifier.height(14.dp))
                SavedBanner(state = state, onShare = share, modifier = Modifier.padding(horizontal = 22.dp))
            } else if (state.error != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = state.error ?: "",
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
            }
        }

        FooterActions(
            state = state,
            onSaveJpg = viewModel::saveJpg,
            onSavePdf = viewModel::savePdf,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun Header(state: PrintSheetUiState, modifier: Modifier = Modifier) {
    val doc = state.doc
    val layout = state.layout
    val tagline = if (layout != null && doc != null) {
        val copies = pluralStringResource(
            R.plurals.printsheet_copies,
            layout.copies,
            layout.copies,
        )
        stringResource(
            R.string.printsheet_header_template,
            layout.sheet.displayName.uppercase(),
            copies,
            doc.dimensions.widthMm.toInt(),
            doc.dimensions.heightMm.toInt(),
        )
    } else {
        stringResource(R.string.printsheet_header_preparing)
    }
    Column(modifier = modifier) {
        Text(
            text = tagline,
            color = Primary,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.printsheet_subtitle),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun SheetPicker(
    selected: SheetSize,
    onSelect: (SheetSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        SheetSize.entries.forEach { sheet ->
            val isSelected = sheet == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) Primary else MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Primary else Hairline,
                        shape = RoundedCornerShape(14.dp),
                    )
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = { onSelect(sheet) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = sheet.displayName,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun SheetPreview(state: PrintSheetUiState, modifier: Modifier = Modifier) {
    val layout = state.layout
    val photoUri = state.shareUri.takeIf { state.shareMime == "image/jpeg" }
    val sheet = state.sheet
    // Keep the rendered sheet a constant max height so all three formats fit comfortably.
    val maxHeightDp = 360f
    val scale = maxHeightDp / sheet.heightPt.toFloat()
    val widthDp = (sheet.widthPt * scale).dp
    val heightDp = maxHeightDp.dp

    Box(
        modifier = modifier
            .size(width = widthDp, height = heightDp)
            .s3(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding(0.dp),
    ) {
        if (layout != null) {
            val photoWdp = (layout.photoWidthPt * scale).dp
            val photoHdp = (layout.photoHeightPt * scale).dp
            val gutterDp = (layout.gutterPt * scale).dp
            val marginLeftDp = (layout.marginLeftPt * scale).dp
            val marginTopDp = (layout.marginTopPt * scale).dp

            Column(
                verticalArrangement = Arrangement.spacedBy(gutterDp),
                modifier = Modifier.padding(start = marginLeftDp, top = marginTopDp),
            ) {
                repeat(layout.rows) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gutterDp)) {
                        repeat(layout.cols) {
                            PhotoCell(
                                photoUri = photoUri,
                                modifier = Modifier
                                    .size(width = photoWdp, height = photoHdp),
                            )
                        }
                    }
                }
            }
        }
        Text(
            text = stringResource(R.string.printsheet_sheet_dims, sheet.widthMm.toInt(), sheet.heightMm.toInt()),
            color = Ink4,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp),
        )
    }
}

@Composable
private fun PhotoCell(photoUri: Uri?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(1.dp))
            .background(Hairline2)
            .border(0.5.dp, Color.Black.copy(alpha = 0.35f), RoundedCornerShape(1.dp)),
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun LockedBanner(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AmberSoft)
            .border(1.dp, AmberDark.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AmberDark),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.printsheet_locked_title),
                color = AmberDark,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = stringResource(R.string.printsheet_locked_subtitle),
                color = AmberDark.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SavedBanner(
    state: PrintSheetUiState,
    onShare: (Uri, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(PrimaryFaint)
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    if (state.shareMime == "application/pdf") R.string.printsheet_saved_pdf
                    else R.string.printsheet_saved_jpg,
                ),
                color = Primary,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = stringResource(R.string.printsheet_saved_subtitle),
                color = Ink3,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        val uri = state.shareUri
        val mime = state.shareMime
        if (uri != null && mime != null) {
            val label = stringResource(R.string.printsheet_share)
            Text(
                text = label,
                color = Primary,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .clickable(role = Role.Button, onClickLabel = label) { onShare(uri, mime) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun FooterActions(
    state: PrintSheetUiState,
    onSaveJpg: () -> Unit,
    onSavePdf: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !state.locked && state.phase != PrintExportPhase.Generating
    val saveJpgLabel = stringResource(R.string.printsheet_save_jpg)
    val savePdfLabel = stringResource(R.string.printsheet_save_pdf)
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Hairline, RoundedCornerShape(18.dp))
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = saveJpgLabel,
                    onClick = onSaveJpg,
                )
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.printsheet_save_jpg),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1.4f)
                .height(56.dp)
                .sGreen(18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (enabled) Primary else Primary.copy(alpha = 0.55f))
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = savePdfLabel,
                    onClick = onSavePdf,
                )
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.Download,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(
                    if (state.phase == PrintExportPhase.Generating) R.string.printsheet_generating
                    else R.string.printsheet_save_pdf,
                ),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CircleIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    tint: Color,
    contentDescription: String,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .s1(14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}

