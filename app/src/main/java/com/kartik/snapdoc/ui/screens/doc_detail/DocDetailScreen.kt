package com.kartik.snapdoc.ui.screens.doc_detail

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kartik.snapdoc.R
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.ui.components.CircleIconButton
import com.kartik.snapdoc.ui.components.DocPreviewHero
import com.kartik.snapdoc.ui.theme.Background
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import com.kartik.snapdoc.ui.theme.PrimarySoft
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.s2
import com.kartik.snapdoc.ui.theme.s3
import com.kartik.snapdoc.ui.theme.sGreen

@Composable
fun DocDetailScreen(
    onBack: () -> Unit,
    onTakePhoto: (docId: String) -> Unit,
    viewModel: DocDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val doc = state.doc

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (doc == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (state.notFound) R.string.docdetail_not_found else R.string.docdetail_loading,
                    ),
                    color = Ink3,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp),
        ) {
            HeroSection(onBack = onBack)
            Spacer(modifier = Modifier.height(32.dp))
            BodyContent(doc = doc)
        }

        StickyCta(
            onClick = { onTakePhoto(doc.id) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HeroSection(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(PrimaryFaint),
    ) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, end = 22.dp, top = 58.dp),
        ) {
            CircleIconButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                onClick = onBack,
                contentDescription = stringResource(R.string.cd_back),
            )
            CircleIconButton(
                icon = Icons.Outlined.Info,
                onClick = {},
                contentDescription = stringResource(R.string.cd_info),
            )
        }

        // Floating hero card.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (-12).dp)
                .size(width = 152.dp, height = 192.dp)
                .s3(18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(10.dp),
        ) {
            DocPreviewHero(modifier = Modifier.fillMaxSize())
        }
    }
}


@Composable
private fun BodyContent(doc: DocumentSpec) {
    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
        // Verified badge.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .s2(99.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 8.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(9.dp),
                )
            }
            Text(
                text = stringResource(R.string.docdetail_verified_badge),
                color = Primary,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.docdetail_eyebrow, doc.categoryId.uppercase()),
            color = Primary,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = doc.displayName,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
        )
        if (doc.sourceUrl.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.docdetail_source, doc.sourceUrl.cleanHost()),
                color = Ink3,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        SpecCards(doc = doc)
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = stringResource(R.string.docdetail_requirements_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(modifier = Modifier.height(10.dp))
        RequirementsCard(doc = doc)
    }
}

@Composable
private fun SpecCards(doc: DocumentSpec) {
    val specs = listOf(
        stringResource(R.string.docdetail_spec_size) to stringResource(
            R.string.docdetail_spec_size_value,
            doc.dimensions.widthMm.toInt(),
            doc.dimensions.heightMm.toInt(),
        ),
        stringResource(R.string.docdetail_spec_resolution) to stringResource(
            R.string.docdetail_spec_resolution_value,
            doc.dimensions.widthPx,
            doc.dimensions.heightPx,
        ),
        stringResource(R.string.docdetail_spec_background) to doc.background.displayName,
        stringResource(R.string.docdetail_spec_file) to stringResource(
            R.string.docdetail_spec_file_value,
            doc.file.minSizeKb,
            doc.file.maxSizeKb,
        ),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        specs.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value) ->
                    SpecCell(label = label, value = value, modifier = Modifier.weight(1f))
                }
                if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SpecCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .s1(14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            color = Ink3,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun RequirementsCard(doc: DocumentSpec) {
    val items = buildList {
        add(stringResource(R.string.docdetail_req_background_template, doc.background.displayName))
        add(stringResource(R.string.docdetail_req_face))
        if (doc.rules.neutralExpression) add(stringResource(R.string.docdetail_req_neutral))
        if (!doc.rules.glassesAllowed) add(stringResource(R.string.docdetail_req_no_glasses))
        add(stringResource(R.string.docdetail_req_recent))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .s1(18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp),
    ) {
        items.forEachIndexed { idx, req ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(PrimarySoft),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp),
                    )
                }
                Text(
                    text = req,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (idx < items.lastIndex) {
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
private fun StickyCta(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Background),
                ),
            )
            .padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 32.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .sGreen(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Primary)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.PhotoCamera,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.docdetail_cta_take),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun String.cleanHost(): String =
    removePrefix("https://").removePrefix("http://").trimEnd('/').substringBefore('/')
