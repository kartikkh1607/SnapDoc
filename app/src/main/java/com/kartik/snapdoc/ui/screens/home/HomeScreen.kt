package com.kartik.snapdoc.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.R
import com.kartik.snapdoc.ui.components.DocKind
import com.kartik.snapdoc.ui.components.DocPreview
import com.kartik.snapdoc.ui.components.HomeShimmerSkeleton
import com.kartik.snapdoc.ui.components.SectionEntry
import com.kartik.snapdoc.ui.components.pressScale
import com.kartik.snapdoc.ui.theme.Amber
import com.kartik.snapdoc.ui.theme.ErrorRed
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryDark
import com.kartik.snapdoc.ui.theme.PrimarySoft
import com.kartik.snapdoc.ui.theme.Success
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.sGreen

private val Gutter = 22.dp

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
    val firstName = state.profile.displayName.split(' ').firstOrNull().orEmpty()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SectionEntry(index = 0) {
            PersonalisedHeader(
                initials = state.profile.initials,
                firstName = firstName,
                onSettingsClick = onSettingsClick,
            )
        }
        SectionEntry(index = 1) {
            ContinueHero(
                onClick = { state.documents.firstOrNull()?.let { onDocClick(it.id) } },
            )
        }
        SectionEntry(index = 2) {
            CategoryChips(
                categories = listOf(null to stringResource(R.string.home_category_all)) +
                    state.categories.map { it.id to it.displayName },
                selected = state.selectedCategoryId,
                onSelect = onCategorySelect,
            )
        }
        SectionEntry(index = 3) {
            SuggestedHeader(firstName = firstName)
        }
        SectionEntry(index = 4) {
            if (state.loading) {
                HomeShimmerSkeleton()
            } else {
                DocumentGrid(documents = state.documents, onDocClick = onDocClick)
            }
        }
    }
}

@Composable
private fun PersonalisedHeader(
    initials: String,
    firstName: String,
    onSettingsClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter),
    ) {
        AvatarTile(initials = initials)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_eyebrow),
                style = MaterialTheme.typography.labelMedium,
                color = Ink3,
            )
            Text(
                text = stringResource(R.string.home_greeting, firstName),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        NotificationBell(onClick = onSettingsClick)
    }
}

@Composable
private fun AvatarTile(initials: String) {
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
                text = initials,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
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
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .pressScale(interaction)
            .s1(14.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
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
                .background(ErrorRed),
        )
    }
}

@Composable
private fun ContinueHero(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter)
            .pressScale(interaction)
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
                interactionSource = interaction,
                indication = LocalIndication.current,
                role = Role.Button,
                onClickLabel = stringResource(R.string.home_continue_title),
                onClick = onClick,
            )
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 32.dp, y = 32.dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f)),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFF8EC)),
            )
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
                            letterSpacing = 0.4.sp,
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
}

@Composable
private fun CategoryChips(
    categories: List<Pair<String?, String>>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        contentPadding = PaddingValues(horizontal = Gutter),
    ) {
        items(categories) { (id, name) ->
            val active = selected == id
            val activeBg = MaterialTheme.colorScheme.onSurface
            val inactiveBg = MaterialTheme.colorScheme.surface
            val activeFg = Color.White
            val inactiveFg = MaterialTheme.colorScheme.onSurface
            val bg by animateColorAsState(
                targetValue = if (active) activeBg else inactiveBg,
                animationSpec = tween(220),
                label = "chip-bg",
            )
            val fg by animateColorAsState(
                targetValue = if (active) activeFg else inactiveFg,
                animationSpec = tween(220),
                label = "chip-fg",
            )
            Box(
                modifier = Modifier
                    .s1(99.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(bg)
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
                    color = fg,
                )
            }
        }
    }
}

@Composable
private fun SuggestedHeader(firstName: String) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Gutter),
    ) {
        Text(
            text = stringResource(R.string.home_suggested_for, firstName),
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
            .padding(horizontal = Gutter),
    ) {
        rows.forEachIndexed { rowIdx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEachIndexed { colIdx, doc ->
                    val featured = rowIdx == 0 && colIdx == 0
                    GridItemStagger(
                        index = rowIdx * 2 + colIdx,
                        modifier = Modifier.weight(1f),
                    ) {
                        DocCardLarge(
                            doc = doc,
                            featured = featured,
                            onClick = { onDocClick(doc.id) },
                        )
                    }
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
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .pressScale(interaction)
            .s1(18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
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
                text = stringResource(
                    R.string.home_doc_card_size,
                    doc.dimensions.widthMm.toInt(),
                    doc.dimensions.heightMm.toInt(),
                ),
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
                        letterSpacing = 0.3.sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun GridItemStagger(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 55L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(280)) + slideInVertically(tween(300)) { it / 4 },
    ) {
        content()
    }
}

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
