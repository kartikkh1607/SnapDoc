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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kartik.snapdoc.data.prefs.UserProfile
import com.kartik.snapdoc.R
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
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var progress by remember { mutableStateOf(0f) }
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1600),
        label = "splash-progress",
    )
    LaunchedEffect(Unit) { progress = 1f }
    LaunchedEffect(destination) {
        if (destination == SplashDestination.Pending) return@LaunchedEffect
        delay(1400)
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
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(76.dp))
            MadeForIndiaChip()
            Spacer(modifier = Modifier.height(120.dp))
            BrandMark()
            Spacer(modifier = Modifier.height(24.dp))
            Wordmark()
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = Ink3,
            )
            Spacer(modifier = Modifier.height(28.dp))
            profile?.let {
                WelcomeBackCard(it)
                Spacer(modifier = Modifier.height(14.dp))
                ResumeChip()
            }
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
            Spacer(modifier = Modifier.height(28.dp))
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
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
            ),
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
        drawRoundRect(
            color = Color.White.copy(alpha = 0.32f),
            topLeft = Offset(w * 0.13f, w * 0.13f),
            size = androidx.compose.ui.geometry.Size(w * 0.74f, w * 0.74f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.20f),
            style = Stroke(width = 2.4f),
        )
        drawCircle(
            color = Color.White,
            radius = r,
            center = Offset(cx, cx),
            style = Stroke(width = 2.4f),
        )
        drawCircle(color = Color.White, radius = r * 0.32f, center = Offset(cx, cx))
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
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                letterSpacing = (-1.2).sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Doc",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                letterSpacing = (-1.2).sp,
            ),
            color = Primary,
        )
    }
}

@Composable
private fun WelcomeBackCard(profile: UserProfile) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .s2(22.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
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
                text = profile.initials,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.splash_welcome_back),
                color = Ink3,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = profile.displayName,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Success),
                )
                Text(
                    text = stringResource(R.string.splash_stats),
                    color = Primary,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun ResumeChip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AmberSoft)
            .border(1.dp, AmberDark.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(start = 10.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Amber),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.splash_resume_title),
                color = AmberDark,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = stringResource(R.string.splash_resume_subtitle),
                color = AmberDark.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
            contentDescription = null,
            tint = AmberDark,
            modifier = Modifier.size(16.dp),
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
    Text(
        text = stringResource(R.string.splash_trust_footer),
        color = Ink4,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        ),
    )
}
