package com.kartik.snapdoc.ui.screens.print_sheet

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CropFree
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
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
import com.kartik.snapdoc.ui.components.ShoulderArt
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.s3
import com.kartik.snapdoc.ui.theme.sGreen

@Composable
fun PrintSheetScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, bottom = 120.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            ) {
                CircleIcon(Icons.AutoMirrored.Outlined.ArrowBack, onBack, MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "Print sheet",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                CircleIcon(Icons.Outlined.Share, {}, Primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                Text(
                    text = "A4 · 8 COPIES · 51 × 51 MM",
                    color = Primary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Cut along the dotted lines",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            A4Sheet(modifier = Modifier.align(Alignment.CenterHorizontally))

            Spacer(modifier = Modifier.height(20.dp))
            OptionsCard(modifier = Modifier.padding(horizontal = 22.dp))
        }

        FooterActions(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 22.dp, end = 22.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun A4Sheet(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 240.dp, height = 339.dp)
            .s3(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Text(
            text = "A4 · 210 × 297 mm",
            color = Ink4,
            style = MaterialTheme.typography.labelSmall,
        )
        Column(
            modifier = Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(4) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(2) {
                        PhotoCell(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Text(
            text = "✂ cut",
            color = Ink4,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun PhotoCell(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White)
            .border(1.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(2.dp)),
    ) {
        ShoulderArt(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun OptionsCard(modifier: Modifier = Modifier) {
    data class Opt(val label: String, val value: String, val icon: ImageVector)
    val rows = listOf(
        Opt("Paper size", "A4 — 210 × 297 mm", Icons.Outlined.Wallet),
        Opt("Copies per sheet", "8 photos", Icons.Outlined.GridView),
        Opt("Cut lines", "Light dashed", Icons.Outlined.CropFree),
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .s1(18.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
    ) {
        rows.forEachIndexed { idx, r ->
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
                        imageVector = r.icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = r.label,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = r.value,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Ink4,
                    modifier = Modifier.size(16.dp),
                )
            }
            if (idx != rows.lastIndex) {
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
private fun FooterActions(modifier: Modifier = Modifier) {
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
                .clickable {}
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.Print,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "Print",
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
                .background(Primary)
                .clickable {}
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
                text = "Save sheet · PDF",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CircleIcon(icon: ImageVector, onClick: () -> Unit, tint: Color) {
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
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
    }
}
