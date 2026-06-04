package com.kartik.snapdoc.ui.screens.export

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kartik.snapdoc.feature.export.R
import com.kartik.snapdoc.data.billing.ProductIds
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kartik.snapdoc.ui.theme.Amber
import com.kartik.snapdoc.ui.theme.Grabber
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Scrim
import com.kartik.snapdoc.ui.theme.Ink2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import com.kartik.snapdoc.ui.theme.PrimarySoft
import com.kartik.snapdoc.ui.theme.sGreen

private data class Plan(
    val productId: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val priceRes: Int,
    val strikeRes: Int? = null,
    val badge: Pair<Int, Color>? = null,
)

private val Plans = listOf(
    Plan(
        productId = ProductIds.PHOTO_EXPORT,
        titleRes = R.string.export_plan_photo_title,
        subtitleRes = R.string.export_plan_photo_subtitle,
        priceRes = R.string.export_plan_photo_price,
    ),
    Plan(
        productId = ProductIds.STUDIO_BUNDLE,
        titleRes = R.string.export_plan_studio_title,
        subtitleRes = R.string.export_plan_studio_subtitle,
        priceRes = R.string.export_plan_studio_price,
        strikeRes = R.string.export_plan_studio_strike,
        badge = R.string.export_plan_studio_badge to Primary,
    ),
)

@Composable
fun ExportScreen(
    onDone: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Scrim.copy(alpha = 0.6f)),
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
                    .background(Grabber),
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
                        text = stringResource(R.string.export_eyebrow),
                        color = Primary,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.export_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.export_subtitle),
                    color = Ink3,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (state.phase == ExportPhase.Saved) {
                SavedSuccessCard(
                    savedUri = state.savedUri,
                    onShare = { uri ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            Intent.createChooser(intent, context.getString(R.string.export_share_chooser)),
                        )
                    },
                    onDone = onDone,
                )
                return@Column
            }

            if (state.entitlement.canExport && state.phase == ExportPhase.Saving) {
                SavingCard()
                return@Column
            }

            val errorRes = state.errorRes
            if (errorRes != null) {
                Text(
                    text = stringResource(errorRes),
                    color = Amber,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                )
            }

            Plans.forEach { plan ->
                PlanCard(
                    plan = plan,
                    selected = plan.productId == state.selectedProductId,
                    onSelect = { viewModel.selectProduct(plan.productId) },
                )
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
                TrustItem(icon = Icons.Outlined.Shield, label = stringResource(R.string.export_trust_secure))
                TrustItem(icon = Icons.Outlined.Refresh, label = stringResource(R.string.export_trust_redownload))
                TrustItem(icon = Icons.Outlined.Lock, label = stringResource(R.string.export_trust_private))
            }

            Spacer(modifier = Modifier.height(16.dp))
            val selectedPlan = Plans.firstOrNull { it.productId == state.selectedProductId } ?: Plans.first()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .sGreen(18.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Primary)
                    .clickable(
                        enabled = state.phase != ExportPhase.Purchasing,
                        role = Role.Button,
                    ) {
                        (context as? Activity)?.let { viewModel.pay(it) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                val ctaText = when (state.phase) {
                    ExportPhase.Purchasing -> stringResource(R.string.export_cta_purchasing)
                    else -> stringResource(R.string.export_cta_pay, stringResource(selectedPlan.priceRes))
                }
                Text(
                    text = ctaText,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.export_payment_methods),
                color = Ink4,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.export_restore),
                color = Primary,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(role = Role.Button) { viewModel.restore() }
                    .padding(6.dp),
            )
        }
    }
}

@Composable
private fun SavingCard() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Photo,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.export_saving),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun SavedSuccessCard(
    savedUri: android.net.Uri?,
    onShare: (android.net.Uri) -> Unit,
    onDone: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(PrimarySoft)
                .padding(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.export_saved_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                )
                Text(
                    text = stringResource(R.string.export_saved_subtitle),
                    color = Ink3,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        if (savedUri != null) {
            Spacer(modifier = Modifier.height(14.dp))
            AsyncImage(
                model = savedUri,
                contentDescription = stringResource(R.string.export_cd_saved),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 180.dp, height = 224.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
            )
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryFaint)
                    .clickable(
                        enabled = savedUri != null,
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.export_share_button),
                    ) { savedUri?.let(onShare) },
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.export_share_button),
                        color = Primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .sGreen(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Primary)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.export_done),
                        onClick = onDone,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.export_done),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun PlanCard(plan: Plan, selected: Boolean, onSelect: () -> Unit) {
    // Outer Box is not clipped so the badge can sit above the card's top edge
    // without being cut off by the card's rounded corners.
    Box(modifier = Modifier.fillMaxWidth()) {
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
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = onSelect,
                )
                .padding(14.dp),
        ) {
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
                        text = stringResource(plan.titleRes),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                        ),
                    )
                    Text(
                        text = stringResource(plan.subtitleRes),
                        color = if (selected) Ink2 else Ink3,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (plan.strikeRes != null) {
                        Text(
                            text = stringResource(plan.strikeRes),
                            color = Ink4,
                            style = MaterialTheme.typography.labelMedium.copy(
                                textDecoration = TextDecoration.LineThrough,
                            ),
                        )
                    }
                    Text(
                        text = stringResource(plan.priceRes),
                        color = if (selected) Primary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
        }
        if (plan.badge != null) {
            val (labelRes, color) = plan.badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 16.dp, y = (-8).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(
                    text = stringResource(labelRes),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
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
