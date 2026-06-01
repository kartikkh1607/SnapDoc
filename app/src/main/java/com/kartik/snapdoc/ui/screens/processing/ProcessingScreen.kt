package com.kartik.snapdoc.ui.screens.processing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kartik.snapdoc.ui.navigation.Routes
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimarySoft
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.s2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class ProcessingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val docId: String = savedStateHandle.get<String>(Routes.Args.DOC_ID).orEmpty()
    val imageUri: String = savedStateHandle.get<String>(Routes.Args.IMAGE_URI).orEmpty()
}

private data class Step(val label: String, val state: StepState, val detail: String? = null)
private enum class StepState { Done, Active, Idle }

private val Steps = listOf(
    Step("Detecting face", StepState.Done),
    Step("Removing background", StepState.Done),
    Step("Applying specifications", StepState.Active, detail = "Resizing to 600×600 px…"),
    Step("Compressing file", StepState.Idle),
    Step("Verifying compliance", StepState.Idle),
)

@Composable
fun ProcessingScreen(
    onDone: (docId: String, imageUri: String) -> Unit,
    onError: () -> Unit,
    viewModel: ProcessingViewModel = hiltViewModel(),
) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2200),
        label = "processing-progress",
    )
    LaunchedEffect(Unit) {
        progress = 0.66f
        delay(2600)
        onDone(viewModel.docId, viewModel.imageUri)
    }
    @Suppress("UNUSED_EXPRESSION") onError

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(PrimarySoft, MaterialTheme.colorScheme.background),
                    center = Offset.Unspecified,
                    radius = 1100f,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp),
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .s1(14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = "Step 3 of 5",
                    color = Ink3,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
            MorphingLoader(progress = animated)

            Spacer(modifier = Modifier.height(24.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            ) {
                Text(
                    text = "Working on your photo",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Our AI is checking every detail against the official passport spec.",
                    color = Ink3,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
            Timeline(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "PROCESSED ON-DEVICE · NO IMAGES STORED",
                color = Ink4,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 40.dp),
            )
        }
    }
}

@Composable
private fun MorphingLoader(progress: Float) {
    val infinite = rememberInfiniteTransition(label = "loader-spin")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "loader-angle",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val stroke = 10f
            val radius = (size.minDimension - stroke) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            // Track
            drawCircle(
                color = PrimarySoft,
                radius = radius,
                center = center,
                style = Stroke(width = stroke),
            )
            // Progress arc — sweep based on progress, rotated by angle so it feels alive.
            val sweep = 360f * progress.coerceIn(0f, 1f)
            drawArc(
                color = Primary,
                startAngle = -90f + angle,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .s2(26.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayMedium,
            )
        }
    }
}

@Composable
private fun Timeline(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .s1(22.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp),
    ) {
        Steps.forEachIndexed { idx, step ->
            TimelineRow(
                step = step,
                isLast = idx == Steps.lastIndex,
            )
            if (idx != Steps.lastIndex) {
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
private fun TimelineRow(step: Step, isLast: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
    ) {
        StepBullet(state = step.state)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.label,
                color = when (step.state) {
                    StepState.Done -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    StepState.Active -> MaterialTheme.colorScheme.onSurface
                    StepState.Idle -> Ink4
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (step.state == StepState.Active) FontWeight.SemiBold else FontWeight.Medium,
                ),
            )
            if (step.detail != null && step.state == StepState.Active) {
                Text(
                    text = step.detail,
                    color = Primary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        when (step.state) {
            StepState.Done -> Text(
                text = "Done",
                color = Ink4,
                style = MaterialTheme.typography.labelMedium,
            )

            StepState.Active -> ActiveDots()
            StepState.Idle -> {}
        }
    }
    @Suppress("UNUSED_EXPRESSION") isLast
}

@Composable
private fun StepBullet(state: StepState) {
    val size = 22.dp
    when (state) {
        StepState.Done -> Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp),
            )
        }

        StepState.Active -> Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, Primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Primary),
            )
        }

        StepState.Idle -> Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Hairline),
        )
    }
}

@Composable
private fun ActiveDots() {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(3) { i ->
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (i == 1) Primary else Primary.copy(alpha = 0.35f)),
            )
        }
    }
}

