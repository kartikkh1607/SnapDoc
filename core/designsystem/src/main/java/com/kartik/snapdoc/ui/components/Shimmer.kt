package com.kartik.snapdoc.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kartik.snapdoc.ui.theme.SnapDocCorners
import com.kartik.snapdoc.ui.theme.SnapDocTheme

/**
 * Linear gradient sweep across the modified node, used for placeholder loading.
 * Apply on top of a shape with a base background so the gradient blends.
 */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-translate",
    )
    val onSurface = MaterialTheme.colorScheme.onSurface
    val brush = Brush.linearGradient(
        colors = listOf(
            onSurface.copy(alpha = 0.06f),
            onSurface.copy(alpha = 0.14f),
            onSurface.copy(alpha = 0.06f),
        ),
        start = Offset(translate, 0f),
        end = Offset(translate + 400f, 0f),
    )
    then(Modifier.background(brush))
}

/** A rectangular placeholder block — defaults to a 14dp rounded corner card. */
@Composable
fun ShimmerBlock(
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface,
) {
    Box(
        modifier = modifier
            .clip(SnapDocCorners.card)
            .background(background)
            .shimmer(),
    )
}

/**
 * Loading placeholder mirroring the home screen's document grid: a hero block,
 * a row of category chips, and two rows of card-shaped tiles.
 */
@Composable
fun HomeShimmerSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 22.dp)) {
        ShimmerBlock(modifier = Modifier.fillMaxWidth().height(96.dp))
        Spacer(Modifier.height(14.dp))
        ShimmerBlock(modifier = Modifier.fillMaxWidth().height(46.dp))
        Spacer(Modifier.height(14.dp))
        repeat(2) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                ShimmerBlock(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                )
                ShimmerBlock(
                    modifier = Modifier.weight(1f).aspectRatio(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ShimmerPreview() {
    SnapDocTheme {
        Column(Modifier.padding(16.dp)) {
            ShimmerBlock(modifier = Modifier.fillMaxWidth().height(120.dp))
        }
    }
}
