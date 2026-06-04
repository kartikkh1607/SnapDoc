package com.kartik.snapdoc.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kartik.snapdoc.feature.home.R
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.ui.components.DocKind
import com.kartik.snapdoc.ui.components.DocPreview
import com.kartik.snapdoc.ui.components.HomeShimmerSkeleton
import com.kartik.snapdoc.ui.components.ShoulderArt
import com.kartik.snapdoc.ui.theme.Amber
import com.kartik.snapdoc.ui.theme.AmberDark
import com.kartik.snapdoc.ui.theme.AmberSoft
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryDark
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import com.kartik.snapdoc.ui.theme.PrimarySoft
import com.kartik.snapdoc.ui.theme.Success
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.sGreen

@Composable
fun HomeScreen(
    onDocClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onDocClick = onDocClick,
        onSettingsClick = onSettingsClick,
        onCategorySelect = viewModel::onCategorySelect,
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onDocClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onCategorySelect: (String?) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 44.dp, bottom = 24.dp),
        ) {
            PersonalisedHeader(onSettingsClick = onSettingsClick)
            Spacer(modifier = Modifier.height(8.dp))
            ContinueHero(
                onClick = { state.documents.firstOrNull()?.let { onDocClick(it.id) } },
            )
            Spacer(modifier = Modifier.height(14.dp))
            SmartReminder()
            Spacer(modifier = Modifier.height(14.dp))
            CategoryChips(
                categories = listOf(null to stringResource(R.string.home_category_all)) +
                    state.categories.map { it.id to it.displayName },
                selected = state.selectedCategoryId,
                onSelect = onCategorySelect,
            )
            Spacer(modifier = Modifier.height(14.dp))
            SuggestedHeader()
            Spacer(modifier = Modifier.height(8.dp))
            if (state.loading) {
                HomeShimmerSkeleton()
            } else {
                DocumentGrid(documents = state.documents, onDocClick = onDocClick)
            }
        }
    }
}

@Composable
private fun PersonalisedHeader(onSettingsClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 12.dp),
    ) {
        AvatarTile()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_eyebrow),
                style = MaterialTheme.typography.labelMedium,
                color = Ink3,
            )
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        NotificationBell(onClick = onSettingsClick)
    }
}

@Composable
private fun AvatarTile() {
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Primary, PrimaryDark),
                        start = Offset(0f, 0f),
                        end = Offset(120f, 120f),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "SD",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
        // Online dot
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-2).dp, y = (-2).dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(2.dp)
                .clip(CircleShape)
                .background(Success),
        )
    }
}

@Composable
private fun NotificationBell(onClick: () -> Unit) {
    val label = stringResource(R.string.home_cd_notifications)
    Box(
        modifier = Modifier
            .size(40.dp)
            .s1(14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(19.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-9).dp, y = 9.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(1.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error),
        )
    }
}

@Composable
private fun ContinueHero(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .sGreen(22.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Primary, PrimaryDark),
                    start = Offset(0f, 0f),
                    end = Offset(500f, 500f),
                ),
            )
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.home_continue_title),
                onClick = onClick,
            )
            .padding(14.dp),
    ) {
        // Mini preview card.
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFFFF8EC)),
        ) {
            ShoulderArt(
                modifier = Modifier.fillMaxSize(),
                shirtColor = Color(0xFF5C4633),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Amber)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_continue_step_left),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_continue_title),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = stringResource(R.string.home_continue_subtitle),
                color = Color.White.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SmartReminder() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AmberSoft)
            .border(1.dp, AmberDark.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Amber),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_reminder_title),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = AmberDark,
            )
            Text(
                text = stringResource(R.string.home_reminder_subtitle),
                style = MaterialTheme.typography.labelMedium,
                color = AmberDark.copy(alpha = 0.85f),
            )
        }
        Text(
            text = stringResource(R.string.home_reminder_cta),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = AmberDark,
        )
    }
}

@Composable
private fun CategoryChips(
    categories: List<Pair<String?, String>>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding = PaddingValues(horizontal = 22.dp),
    ) {
        items(categories) { (id, name) ->
            val active = selected == id || (id == null && selected == null && name == "All")
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .let { mod ->
                        if (active) mod.background(MaterialTheme.colorScheme.onSurface)
                        else mod.s1(99.dp).background(MaterialTheme.colorScheme.surface)
                    }
                    .selectable(
                        selected = active,
                        role = Role.Tab,
                        onClick = { onSelect(id) },
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (active) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun SuggestedHeader() {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
    ) {
        Text(
            text = stringResource(R.string.home_suggested_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = stringResource(R.string.home_suggested_action),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Primary,
        )
    }
}

@Composable
private fun DocumentGrid(documents: List<DocumentSpec>, onDocClick: (String) -> Unit) {
    val rows = documents.take(4).chunked(2)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp),
    ) {
        rows.forEachIndexed { rowIdx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { colIdx, doc ->
                    val featured = rowIdx == 0 && colIdx == 0
                    DocCardLarge(
                        doc = doc,
                        featured = featured,
                        onClick = { onDocClick(doc.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DocCardLarge(
    doc: DocumentSpec,
    featured: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .s1(18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                role = Role.Button,
                onClickLabel = doc.displayName,
                onClick = onClick,
            )
            .height(132.dp)
            .padding(12.dp),
    ) {
        Column {
            DocPreview(
                kind = kindFor(doc),
                modifier = Modifier.size(width = 54.dp, height = 66.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = doc.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                text = "${doc.dimensions.widthMm.toInt()}×${doc.dimensions.heightMm.toInt()} mm",
                style = MaterialTheme.typography.labelMedium,
                color = Ink3,
            )
        }
        if (featured) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(5.dp))
                    .background(PrimarySoft)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_featured_badge),
                    color = Primary,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                    ),
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeContentPreview() {
    com.kartik.snapdoc.ui.theme.SnapDocTheme {
        HomeContent(
            state = HomeUiState(
                loading = false,
                categories = listOf(
                    com.kartik.snapdoc.data.specs.model.CategorySpec("in_government", "Indian Gov", "ic_govt"),
                    com.kartik.snapdoc.data.specs.model.CategorySpec("intl_visa", "Visas", "ic_visa"),
                ),
                documents = listOf(previewDoc("in_passport", "Indian Passport"), previewDoc("in_pan", "PAN Card")),
            ),
            onDocClick = {},
            onSettingsClick = {},
            onCategorySelect = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, heightDp = 900)
@Composable
private fun HomeContentLoadingPreview() {
    com.kartik.snapdoc.ui.theme.SnapDocTheme {
        HomeContent(
            state = HomeUiState(loading = true),
            onDocClick = {},
            onSettingsClick = {},
            onCategorySelect = {},
        )
    }
}

private fun previewDoc(id: String, displayName: String): DocumentSpec = DocumentSpec(
    id = id,
    displayName = displayName,
    shortName = displayName,
    categoryId = "in_government",
    popularity = 100,
    dimensions = com.kartik.snapdoc.data.specs.model.DimensionsSpec(35f, 45f, 413, 531, 300),
    background = com.kartik.snapdoc.data.specs.model.BackgroundSpec("#FFFFFF", "White", 5),
    face = com.kartik.snapdoc.data.specs.model.FaceSpec(70, 80, 50, 70),
    file = com.kartik.snapdoc.data.specs.model.FileSpec("JPG", 10, 100),
    rules = com.kartik.snapdoc.data.specs.model.RulesSpec(),
)

private fun kindFor(doc: DocumentSpec): DocKind {
    val cat = doc.categoryId.lowercase()
    val name = doc.displayName.lowercase()
    return when {
        "passport" in name || "passport" in cat -> DocKind.Passport
        "aadhaar" in name -> DocKind.Aadhaar
        "pan" in name -> DocKind.Pan
        "upsc" in name || "exam" in cat -> DocKind.Upsc
        "visa" in name || "visa" in cat -> DocKind.Visa
        "ssc" in name -> DocKind.Ssc
        else -> DocKind.Passport
    }
}

