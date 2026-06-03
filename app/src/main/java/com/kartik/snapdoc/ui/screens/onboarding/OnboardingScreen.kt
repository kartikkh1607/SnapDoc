package com.kartik.snapdoc.ui.screens.onboarding

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.kartik.snapdoc.R
import com.kartik.snapdoc.ui.components.ShoulderArt
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimaryFaint
import com.kartik.snapdoc.ui.theme.Success
import com.kartik.snapdoc.ui.theme.s2
import com.kartik.snapdoc.ui.theme.s3
import com.kartik.snapdoc.ui.theme.sGreen

private data class Slide(
    val titleRes: Int,
    val subtitleRes: Int,
)

private val Slides = listOf(
    Slide(R.string.onboarding_slide1_title, R.string.onboarding_slide1_subtitle),
    Slide(R.string.onboarding_slide2_title, R.string.onboarding_slide2_subtitle),
    Slide(R.string.onboarding_slide3_title, R.string.onboarding_slide3_subtitle),
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var index by remember { mutableIntStateOf(0) }
    val slide = Slides[index]
    val complete: () -> Unit = { viewModel.finish(onFinish) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
        ) {
            // Skip
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 22.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink3,
                    modifier = Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.onboarding_cd_skip),
                        onClick = complete,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            VisualCanvas()

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = stringResource(slide.titleRes),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(slide.subtitleRes),
                style = MaterialTheme.typography.bodyLarge,
                color = Ink3,
                modifier = Modifier.padding(horizontal = 32.dp),
            )

            Spacer(modifier = Modifier.weight(1f))
            FooterControls(
                index = index,
                onNext = {
                    if (index == Slides.lastIndex) complete()
                    else index++
                },
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun VisualCanvas() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(horizontal = 24.dp),
    ) {
        // Soft canvas backdrop.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 18.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(PrimaryFaint),
        )

        // BEFORE card (tilted -6°).
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 26.dp, top = 50.dp)
                .rotate(-6f)
                .size(width = 130.dp, height = 168.dp)
                .s2(18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF8B7359)),
        ) {
            ShoulderArt(
                modifier = Modifier.fillMaxSize(),
                shirtColor = Color(0xFF5C4633),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 42.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.onSurface)
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_before),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        // Center arrow.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(40.dp)
                .sGreen(20.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }

        // AFTER card (tilted +4°).
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 80.dp)
                .rotate(4f)
                .size(width = 138.dp, height = 178.dp)
                .s3(18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF4F4F2)),
        ) {
            ShoulderArt(
                modifier = Modifier.fillMaxSize(),
                shirtColor = Color(0xFF2E4F7A),
            )
        }

        // Verified chip overlay (near AFTER card bottom).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 70.dp)
                .s2(99.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 6.dp, end = 9.dp, top = 5.dp, bottom = 5.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Success),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(9.dp),
                )
            }
            Text(
                text = stringResource(R.string.onboarding_verified),
                color = Success,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun FooterControls(index: Int, onNext: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Slides.indices.forEach { i ->
                val active = i == index
                Box(
                    modifier = Modifier
                        .width(if (active) 22.dp else 7.dp)
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) Primary else Ink4.copy(alpha = 0.4f)),
                )
            }
        }
        val nextLabel = stringResource(R.string.onboarding_cd_next)
        Box(
            modifier = Modifier
                .size(64.dp)
                .sGreen(32.dp)
                .clip(CircleShape)
                .background(Primary)
                .clickable(
                    role = Role.Button,
                    onClickLabel = nextLabel,
                    onClick = onNext,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

