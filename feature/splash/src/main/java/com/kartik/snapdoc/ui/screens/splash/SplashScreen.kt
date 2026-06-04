package com.kartik.snapdoc.ui.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.History
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kartik.snapdoc.feature.splash.R
import com.kartik.snapdoc.ui.theme.Amber
import com.kartik.snapdoc.ui.theme.AmberDark
import com.kartik.snapdoc.ui.theme.AmberSoft
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryDark
import com.kartik.snapdoc.ui.theme.PrimarySoft
import com.kartik.snapdoc.ui.theme.Success
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.s2
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFirstLaunch: () -> Unit,
    onReturning: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    var progress by remember { mutableStateOf(0f) }
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1600),
        label = "splash-progress",
    )
    LaunchedEffect(Unit) { progress = 1f }
    LaunchedEffect(destination) {
        if (destination == SplashDestination.Pending) return@LaunchedEffect
        delay(1200)
        when (destination) {
            SplashDestination.Onboarding -> onFirstLaunch()
            SplashDestination.Home -> onReturning()
            SplashDestination.Pending -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Soft radial gradient field behind everything.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(PrimarySoft, MaterialTheme.colorScheme.surface),
                        center = Offset.Unspecified,
                        radius = 1200f,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(76.dp))
            MadeForIndiaChip()
            Spacer(modifier = Modifier.height(120.dp))
            BrandMark()
            Spacer(modifier = Modifier.height(20.dp))
            Wordmark()
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink3,
            )
            Spacer(modifier = Modifier.weight(1f))
            LoaderBar(progress = animated)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_loading),
                style = MaterialTheme.typography.labelMedium,
                color = Ink3,
            )
            Spacer(modifier = Modifier.height(20.dp))
            TrustFooter()
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MadeForIndiaChip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .s1(99.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 8.dp, end = 12.dp, top = 5.dp, bottom = 5.dp),
    ) {
        IndianFlag()
        Text(
            text = stringResource(R.string.splash_made_for_india),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun IndianFlag() {
    Canvas(modifier = Modifier.size(width = 14.dp, height = 10.dp)) {
        val third = size.height / 3f
        drawRect(Color(0xFFFF9933), size = androidx.compose.ui.geometry.Size(size.width, third))
        drawRect(
            Color.White,
            topLeft = Offset(0f, third),
            size = androidx.compose.ui.geometry.Size(size.width, third),
        )
        drawRect(
            Color(0xFF138808),
            topLeft = Offset(0f, third * 2f),
            size = androidx.compose.ui.geometry.Size(size.width, third),
        )
        drawCircle(
            color = Color(0xFF000080),
            radius = third * 0.5f,
            center = Offset(size.width / 2f, size.height / 2f),
            style = Stroke(width = 0.6f),
        )
    }
}

@Composable
private fun BrandMark() {
    Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
        // Layered green tile with subtle inner highlight.
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Primary, PrimaryDark),
                        start = Offset(0f, 0f),
                        end = Offset(200f, 280f),
                    ),
                ),
        )
        ApertureGlyph()
        // Amber corner dot.
        Box(
            modifier = Modifier
                .offset(x = 38.dp, y = (-46).dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White)
                .padding(3.dp)
                .clip(CircleShape)
                .background(Amber),
        )
    }
}

@Composable
private fun ApertureGlyph() {
    Canvas(modifier = Modifier.size(52.dp)) {
        val w = size.width
        val cx = w / 2f
        val r = w * 0.32f
        // Outer rounded square (faded).
        drawRoundRect(
            color = Color.White.copy(alpha = 0.32f),
            topLeft = Offset(w * 0.13f, w * 0.13f),
            size = androidx.compose.ui.geometry.Size(w * 0.74f, w * 0.74f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.20f),
            style = Stroke(width = 2.4f),
        )
        // Aperture ring.
        drawCircle(
            color = Color.White,
            radius = r,
            center = Offset(cx, cx),
            style = Stroke(width = 2.4f),
        )
        // Inner dot.
        drawCircle(color = Color.White, radius = r * 0.32f, center = Offset(cx, cx))
        // Aperture vanes.
        val vanes = listOf(
            Pair(Offset(cx, cx - r * 0.75f), Offset(cx + r * 0.45f, cx - r * 0.4f)),
            Pair(Offset(cx + r * 0.75f, cx), Offset(cx + r * 0.4f, cx + r * 0.45f)),
            Pair(Offset(cx, cx + r * 0.75f), Offset(cx - r * 0.45f, cx + r * 0.4f)),
            Pair(Offset(cx - r * 0.75f, cx), Offset(cx - r * 0.4f, cx - r * 0.45f)),
        )
        vanes.forEach { (a, b) ->
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = a,
                end = b,
                strokeWidth = 1.8f,
            )
        }
    }
}

@Composable
private fun Wordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "snap",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Doc",
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            color = Primary,
        )
    }
}

@Composable
private fun LoaderBar(progress: Float) {
    Box(
        modifier = Modifier
            .width(132.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Hairline),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Primary, Primary, Amber),
                    ),
                ),
        )
    }
}

@Composable
private fun TrustFooter() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("UIDAI", "MEA", "UPSC", "SSC", "NTA").forEachIndexed { i, label ->
            if (i > 0) {
                Text(
                    text = "·",
                    color = Ink4.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text(
                text = label,
                color = Ink4,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            )
        }
    }
}

