package com.kartik.snapdoc.ui.screens.export

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kartik.snapdoc.ui.theme.Amber
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Ink2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import com.kartik.snapdoc.ui.theme.PrimarySoft
import com.kartik.snapdoc.ui.theme.sGreen

// "Export" is the paywall sheet from the design — a bottom-sheet
// with product comparison cards, trust row, and a UPI CTA.

private data class Plan(
    val title: String,
    val subtitle: String,
    val price: String,
    val strike: String? = null,
    val badge: Pair<String, Color>? = null,
)

private val Plans = listOf(
    Plan(
        title = "Digital photo",
        subtitle = "Spec-perfect JPG · re-downloadable",
        price = "₹49",
    ),
    Plan(
        title = "Photo + Print sheet",
        subtitle = "Digital JPG + A4 with 8 copies, cut-line ready",
        price = "₹79",
        strike = "₹109",
        badge = "MOST POPULAR" to Primary,
    ),
    Plan(
        title = "Family pack · 5 photos",
        subtitle = "Any 5 documents · Photo + Print sheets",
        price = "₹199",
        strike = "₹395",
        badge = "BEST VALUE" to Amber,
    ),
)

@Composable
fun ExportScreen(onDone: () -> Unit) {
    var selected by remember { mutableIntStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F140F).copy(alpha = 0.6f)),
    ) {
        // Sheet
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 36.dp),
        ) {
            // Grabber
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 18.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFE0E0E0)),
            )

            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(PrimarySoft)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        text = "ONE-TIME PAYMENT · NO SUBSCRIPTION",
                        color = Primary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pick your export",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Pay once. Re-download anytime. Keep it forever.",
                    color = Ink3,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Plans.forEachIndexed { idx, plan ->
                PlanCard(plan = plan, selected = idx == selected, onSelect = { selected = idx })
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Trust row
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(PrimaryFaint)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                TrustItem(icon = Icons.Outlined.Shield, label = "Secure pay")
                TrustItem(icon = Icons.Outlined.Refresh, label = "Re-download")
                TrustItem(icon = Icons.Outlined.Lock, label = "Private")
            }

            Spacer(modifier = Modifier.height(16.dp))
            // CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sGreen(18.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Primary)
                    .clickable(onClick = onDone),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Pay ${Plans[selected].price} with UPI",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "UPI · Cards · Net banking · Wallets",
                color = Ink4,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
private fun PlanCard(plan: Plan, selected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) PrimaryFaint else MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = if (selected) Primary else Hairline,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onSelect)
            .padding(14.dp),
    ) {
        if (plan.badge != null) {
            val (label, color) = plan.badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = (-22).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(
                    text = label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Radio
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (selected) Primary else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (selected) Primary else Hairline,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = plan.subtitle,
                    color = if (selected) Ink2 else Ink3,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (plan.strike != null) {
                    Text(
                        text = plan.strike,
                        color = Ink4,
                        style = MaterialTheme.typography.labelMedium.copy(
                            textDecoration = TextDecoration.LineThrough,
                        ),
                    )
                }
                Text(
                    text = plan.price,
                    color = if (selected) Primary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun TrustItem(icon: ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Primary,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}
