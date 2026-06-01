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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
)

private data class SettingsGroup(val title: String, val rows: List<SettingsRow>)

private val Groups = listOf(
    SettingsGroup(
        title = "Account",
        rows = listOf(
            SettingsRow("Purchases & receipts", "4 photos exported", Icons.Outlined.Wallet),
            SettingsRow("Restore purchase", null, Icons.Outlined.Refresh),
        ),
    ),
    SettingsGroup(
        title = "Preferences",
        rows = listOf(
            SettingsRow("Language", "English (India)", Icons.Outlined.Language),
            SettingsRow("Save to gallery", "On", Icons.Outlined.Image, toggle = true),
            SettingsRow("On-device processing", "Always", Icons.Outlined.Bolt),
        ),
    ),
    SettingsGroup(
        title = "Support",
        rows = listOf(
            SettingsRow("Help & FAQ", null, Icons.AutoMirrored.Outlined.Help),
            SettingsRow("Contact support", null, Icons.Outlined.Notifications),
            SettingsRow("Rate SnapDoc", null, Icons.Outlined.Star),
        ),
    ),
    SettingsGroup(
        title = "Legal",
        rows = listOf(
            SettingsRow("Privacy policy", null, Icons.Outlined.Shield),
            SettingsRow("Terms of service", null, Icons.Outlined.Book),
            SettingsRow("About", "v 4.2.1", Icons.Outlined.Info),
        ),
    ),
)

@Composable
fun SettingsScreen(onBack: () -> Unit) {
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
                    text = "Settings",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineLarge,
                )
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .s1(14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            // Profile card
            ProfileCard(modifier = Modifier.padding(horizontal = 22.dp))

            Spacer(modifier = Modifier.height(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Groups.forEach { group ->
                    GroupCard(group = group)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Made in Bengaluru · v 4.2.1 (build 4210)",
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
        // Decorative ring (top-right)
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
                Text(
                    text = "RS",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Riya Sharma",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = "riya.sharma@gmail.com",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
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
