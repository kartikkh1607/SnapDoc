package com.kartik.snapdoc.ui.screens.settings

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kartik.snapdoc.R
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.sGreen

private data class SettingsRow(
    val label: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val toggle: Boolean = false,
    val onClick: (() -> Unit)? = null,
)

private data class SettingsGroup(val title: String, val rows: List<SettingsRow>)

@Composable
private fun buildGroups(state: SettingsUiState, onRestore: () -> Unit): List<SettingsGroup> = listOf(
    SettingsGroup(
        title = stringResource(R.string.settings_group_purchases),
        rows = listOf(
            SettingsRow(
                label = stringResource(R.string.settings_row_photo_export),
                subtitle = if (state.entitlement.canExport) {
                    stringResource(R.string.settings_row_photo_export_unlocked)
                } else {
                    stringResource(R.string.settings_row_photo_export_locked)
                },
                icon = Icons.Outlined.Wallet,
            ),
            SettingsRow(
                label = stringResource(R.string.settings_row_studio_bundle),
                subtitle = if (state.entitlement.studioBundleUnlocked) {
                    stringResource(R.string.settings_row_studio_bundle_unlocked)
                } else {
                    stringResource(R.string.settings_row_studio_bundle_locked)
                },
                icon = Icons.Outlined.Image,
            ),
            SettingsRow(
                label = if (state.restoring) {
                    stringResource(R.string.settings_row_restoring)
                } else {
                    stringResource(R.string.settings_row_restore)
                },
                subtitle = state.restoreMessage,
                icon = Icons.Outlined.Refresh,
                onClick = onRestore,
            ),
        ),
    ),
    SettingsGroup(
        title = stringResource(R.string.settings_group_preferences),
        rows = listOf(
            SettingsRow(
                stringResource(R.string.settings_row_language),
                stringResource(R.string.settings_row_language_value),
                Icons.Outlined.Language,
            ),
            SettingsRow(
                stringResource(R.string.settings_row_on_device),
                stringResource(R.string.settings_row_on_device_value),
                Icons.Outlined.Bolt,
            ),
        ),
    ),
    SettingsGroup(
        title = stringResource(R.string.settings_group_support),
        rows = listOf(
            SettingsRow(stringResource(R.string.settings_row_help), null, Icons.AutoMirrored.Outlined.Help),
            SettingsRow(stringResource(R.string.settings_row_contact), null, Icons.Outlined.Notifications),
            SettingsRow(stringResource(R.string.settings_row_rate), null, Icons.Outlined.Star),
        ),
    ),
    SettingsGroup(
        title = stringResource(R.string.settings_group_legal),
        rows = listOf(
            SettingsRow(stringResource(R.string.settings_row_privacy), null, Icons.Outlined.Shield),
            SettingsRow(stringResource(R.string.settings_row_terms), null, Icons.Outlined.Book),
            SettingsRow(
                label = stringResource(R.string.settings_row_about),
                subtitle = stringResource(R.string.settings_about_version, state.versionName, state.versionCode),
                icon = Icons.Outlined.Info,
            ),
        ),
    ),
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val groups = buildGroups(state, onRestore = viewModel::restore)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 56.dp, bottom = 64.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge,
                )
                val closeLabel = stringResource(R.string.settings_cd_close)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .s1(14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = closeLabel,
                            onClick = onBack,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Profile card
            ProfileCard(modifier = Modifier.padding(horizontal = 22.dp))

            Spacer(modifier = Modifier.height(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                groups.forEach { group ->
                    GroupCard(group = group)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.settings_footer, state.versionName, state.versionCode),
                color = Ink4,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun ProfileCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .sGreen(20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Primary)
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
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
                    .size(52.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_profile_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = stringResource(R.string.settings_profile_subtitle),
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun GroupCard(group: SettingsGroup) {
    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
        Text(
            text = group.title.uppercase(),
            color = Ink3,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .s1(18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 14.dp),
        ) {
            group.rows.forEachIndexed { idx, row ->
                SettingsRowItem(row = row)
                if (idx != group.rows.lastIndex) {
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
}

@Composable
private fun SettingsRowItem(row: SettingsRow) {
    val clickable = row.onClick
    val label = row.label
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let {
                if (clickable != null) {
                    it.clickable(role = Role.Button, onClickLabel = label, onClick = clickable)
                } else it
            }
            .padding(vertical = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryFaint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = row.icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            )
            if (row.subtitle != null) {
                Text(
                    text = row.subtitle,
                    color = Ink3,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        if (row.toggle) {
            Toggle(on = true)
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Ink4,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun Toggle(on: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 22.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (on) Primary else Hairline2),
    ) {
        Box(
            modifier = Modifier
                .align(if (on) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(horizontal = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
