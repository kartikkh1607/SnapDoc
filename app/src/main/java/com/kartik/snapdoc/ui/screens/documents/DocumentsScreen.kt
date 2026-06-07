package com.kartik.snapdoc.ui.screens.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kartik.snapdoc.R
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.ui.components.DocKind
import com.kartik.snapdoc.ui.components.DocPreview
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary

private val Gutter = 22.dp

@Composable
fun DocumentsScreen(
    onDocClick: (String) -> Unit = {},
    viewModel: DocumentsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(top = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.documents_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .padding(horizontal = Gutter, vertical = 12.dp)
                .semantics { heading() },
        )

        SearchField(
            query = state.query,
            onQueryChange = viewModel::setQuery,
            modifier = Modifier.padding(horizontal = Gutter),
        )

        Spacer(modifier = Modifier.height(12.dp))

        CategoryChips(
            categories = listOf(null to stringResource(R.string.documents_category_all)) +
                state.categories.map { it.id to it.displayName },
            selected = state.selectedCategoryId,
            onSelect = viewModel::setCategory,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.documents_count_template, state.documents.size),
            color = Ink3,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = Gutter, vertical = 4.dp),
        )

        if (state.documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = stringResource(R.string.documents_empty_search),
                    color = Ink4,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = Gutter, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.documents, key = { it.id }) { doc ->
                    DocRow(doc = doc, onClick = { onDocClick(doc.id) })
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hint = stringResource(R.string.documents_search_hint)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = Ink3,
            modifier = Modifier.size(18.dp),
        )
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = hint,
                    color = Ink4,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(Primary),
                modifier = Modifier.fillMaxWidth(),
            )
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = Gutter),
    ) {
        items(categories) { (id, name) ->
            val active = selected == id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface)
                    .clickable(role = Role.Tab) { onSelect(id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
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
private fun DocRow(doc: DocumentSpec, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClickLabel = doc.displayName, onClick = onClick)
            .padding(14.dp),
    ) {
        DocPreview(
            kind = kindFor(doc),
            modifier = Modifier.size(width = 44.dp, height = 56.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = doc.displayName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
            )
            Text(
                text = "${doc.dimensions.widthMm.toInt()} × ${doc.dimensions.heightMm.toInt()} mm · ${doc.background.displayName}",
                color = Ink3,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = Ink4,
            modifier = Modifier.size(20.dp),
        )
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
