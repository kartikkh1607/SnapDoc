package com.kartik.snapdoc.ui.screens.review

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import coil.compose.AsyncImage
import com.kartik.snapdoc.R
import com.kartik.snapdoc.data.specs.SpecCatalogRepository
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.data.specs.model.RulesSpec
import com.kartik.snapdoc.ui.components.DocPreviewHero
import com.kartik.snapdoc.ui.navigation.Routes
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.ReviewSurface
import com.kartik.snapdoc.ui.theme.Success
import com.kartik.snapdoc.ui.theme.sGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repo: SpecCatalogRepository,
) : ViewModel() {
    private val args = savedStateHandle.toRoute<Routes.Review>()
    val docId: String = args.docId
    val imageUri: String = args.imageUri
    // May be null if the catalog was hot-swapped out from under us; the Info
    // button hides itself in that case so we never show an empty sheet.
    val doc: DocumentSpec? = repo.byId(docId)
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onRetake: () -> Unit,
    onUsePhoto: (docId: String, imageUri: String) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    viewModel: ReviewViewModel = hiltViewModel(),
) {
    var showSpecSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReviewSurface),
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp, bottom = 14.dp),
        ) {
            DarkIconButton(
                icon = Icons.Outlined.Close,
                onClick = onRetake,
                contentDescription = stringResource(R.string.review_retake),
            )
            Text(
                text = stringResource(R.string.review_title),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.semantics { heading() },
            )
            if (viewModel.doc != null) {
                DarkIconButton(
                    icon = Icons.Outlined.Info,
                    onClick = { showSpecSheet = true },
                    contentDescription = stringResource(R.string.cd_info),
                )
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
        }

        // Captured image — large.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = with(sharedTransitionScope) {
                    Modifier
                        .fillMaxWidth()
                        .widthIn(max = 320.dp)
                        .aspectRatio(3f / 4f)
                        .sharedElement(
                            rememberSharedContentState(key = "photo-${viewModel.docId}"),
                            animatedVisibilityScope = animatedContentScope,
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(12.dp)
                },
            ) {
                if (viewModel.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = viewModel.imageUri,
                        contentDescription = stringResource(R.string.review_title),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    DocPreviewHero(modifier = Modifier.fillMaxSize())
                }
                Box(
                    modifier = Modifier
                        .padding(20.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = stringResource(R.string.review_chip),
                        color = Ink3,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp),
        ) {
            QualityBadge()
            Spacer(modifier = Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RetakeButton(onClick = onRetake, modifier = Modifier.weight(1f))
                UsePhotoButton(
                    onClick = { onUsePhoto(viewModel.docId, viewModel.imageUri) },
                    modifier = Modifier.weight(1.5f),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showSpecSheet && viewModel.doc != null) {
        ModalBottomSheet(
            onDismissRequest = { showSpecSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            SpecDetailSheet(doc = viewModel.doc)
        }
    }
}

@Composable
private fun SpecDetailSheet(doc: DocumentSpec) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp)
            .padding(bottom = 24.dp),
    ) {
        Text(
            text = doc.displayName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.review_spec_sheet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = Ink3,
        )
        Spacer(modifier = Modifier.height(20.dp))

        SpecRow(
            label = stringResource(R.string.docdetail_spec_size),
            value = stringResource(
                R.string.docdetail_spec_size_value,
                doc.dimensions.widthMm.toInt(),
                doc.dimensions.heightMm.toInt(),
            ),
        )
        SpecRow(
            label = stringResource(R.string.docdetail_spec_resolution),
            value = stringResource(
                R.string.docdetail_spec_resolution_value,
                doc.dimensions.widthPx,
                doc.dimensions.heightPx,
            ),
        )
        SpecRow(
            label = stringResource(R.string.docdetail_spec_background),
            value = doc.background.displayName,
        )
        SpecRow(
            label = stringResource(R.string.docdetail_spec_file),
            value = stringResource(
                R.string.docdetail_spec_file_value,
                doc.file.minSizeKb,
                doc.file.maxSizeKb,
            ),
            showDivider = false,
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.docdetail_requirements_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(10.dp))
        RulesList(rules = doc.rules)
        if (doc.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
            ) {
                Text(
                    text = doc.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink3,
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, showDivider: Boolean = true) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Ink3,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
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

@Composable
private fun RulesList(rules: RulesSpec) {
    val items = buildList {
        add(
            if (rules.glassesAllowed) stringResource(R.string.review_spec_rules_glasses_yes)
            else stringResource(R.string.review_spec_rules_glasses_no),
        )
        add(
            when {
                rules.headCoveringAllowed && rules.headCoveringReligiousOnly ->
                    stringResource(R.string.review_spec_rules_head_cover_religious)
                rules.headCoveringAllowed ->
                    stringResource(R.string.review_spec_rules_head_cover_yes)
                else -> stringResource(R.string.review_spec_rules_head_cover_no)
            },
        )
        if (rules.neutralExpression) add(stringResource(R.string.review_spec_rules_neutral))
        if (rules.mouthClosed) add(stringResource(R.string.review_spec_rules_mouth_closed))
        if (rules.eyesOpen) add(stringResource(R.string.review_spec_rules_eyes_open))
        if (rules.noShadows) add(stringResource(R.string.review_spec_rules_no_shadows))
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { RuleRow(it) }
    }
}

@Composable
private fun RuleRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Success.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Success,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// Hand-picked lighter green for icon contrast against Success.copy(alpha = 0.2f).
private val QualityBadgeIconTint = Color(0xFF7ED28A)

@Composable
private fun QualityBadge() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Success.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = QualityBadgeIconTint,
                modifier = Modifier.size(18.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.review_quality_title),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = stringResource(R.string.review_quality_subtitle),
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun RetakeButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.review_retake)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.Refresh,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.review_retake),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun UsePhotoButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = stringResource(R.string.review_use_photo)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .height(56.dp)
            .sGreen(18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Primary)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.review_use_photo),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DarkIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .clickable(
                onClickLabel = contentDescription,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}
