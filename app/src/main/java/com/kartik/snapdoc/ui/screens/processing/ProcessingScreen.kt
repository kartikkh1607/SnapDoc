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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kartik.snapdoc.R
import com.kartik.snapdoc.domain.pipeline.PipelineFailureReason
import com.kartik.snapdoc.domain.pipeline.ProcessingStage
import com.kartik.snapdoc.ui.theme.Hairline
import com.kartik.snapdoc.ui.theme.Hairline2
import com.kartik.snapdoc.ui.theme.Ink3
import com.kartik.snapdoc.ui.theme.Ink4
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.PrimarySoft
import com.kartik.snapdoc.ui.theme.s1
import com.kartik.snapdoc.ui.theme.s2

private data class Step(val label: String, val state: StepState, val detail: String? = null)
private enum class StepState { Done, Active, Idle }

private val PipelineStageRes = listOf(
    ProcessingStage.DetectingFace to R.string.processing_step_face,
    ProcessingStage.RemovingBackground to R.string.processing_step_bg_remove,
    ProcessingStage.ApplyingBackground to R.string.processing_step_bg_apply,
    ProcessingStage.Cropping to R.string.processing_step_crop,
    ProcessingStage.Resizing to R.string.processing_step_resize,
    ProcessingStage.Compressing to R.string.processing_step_compress,
    ProcessingStage.Validating to R.string.processing_step_validate,
)

@Composable
private fun buildSteps(current: ProcessingStage): List<Step> {
    val currentIdx = PipelineStageRes.indexOfFirst { it.first == current }.coerceAtLeast(0)
    return PipelineStageRes.mapIndexed { idx, (_, res) ->
        val state = when {
            current == ProcessingStage.Done -> StepState.Done
            idx < currentIdx -> StepState.Done
            idx == currentIdx -> StepState.Active
            else -> StepState.Idle
        }
        Step(stringResource(res), state)
    }
}

@Composable
fun ProcessingScreen(
    onDone: (docId: String, imageUri: String) -> Unit,
    onError: () -> Unit,
    onCancel: () -> Unit,
    viewModel: ProcessingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val animated by animateFloatAsState(
        targetValue = state.progress,
        animationSpec = tween(durationMillis = 600),
        label = "processing-progress",
    )

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is ProcessingEvent.Done -> onDone(event.docId, event.imageUri)
                }
            }
        }
    }

    val error = state.error
    if (error != null) {
        ProcessingErrorContent(
            message = stringResource(error.stringRes()),
            onRetry = viewModel::retry,
            onRetake = onError,
        )
        return
    }

    val steps = buildSteps(state.stage)

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
                val cancelLabel = stringResource(R.string.processing_cd_cancel)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .s1(14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = cancelLabel,
                            onClick = onCancel,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
                val currentIdx = (steps.indexOfFirst { it.state == StepState.Active } + 1)
                    .coerceAtLeast(1)
                Text(
                    text = stringResource(R.string.processing_step_of, currentIdx, steps.size),
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
                    text = stringResource(R.string.processing_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.processing_subtitle),
                    color = Ink3,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
            Timeline(
                steps = steps,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
            )

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.processing_footer),
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
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
    }
}

@Composable
private fun Timeline(steps: List<Step>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .s1(22.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 18.dp),
    ) {
        steps.forEachIndexed { idx, step ->
            TimelineRow(step = step)
            if (idx != steps.lastIndex) {
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
private fun TimelineRow(step: Step) {
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
                text = stringResource(R.string.processing_step_done),
                color = Ink4,
                style = MaterialTheme.typography.labelMedium,
            )

            StepState.Active -> ActiveDots()
            StepState.Idle -> {}
        }
    }
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

private fun PipelineFailureReason.stringRes(): Int = when (this) {
    PipelineFailureReason.SourceUnreadable -> R.string.processing_error_source_unreadable
    PipelineFailureReason.NoFaceDetected -> R.string.processing_error_no_face
    PipelineFailureReason.FaceTooClose -> R.string.processing_error_face_too_close
    PipelineFailureReason.FinalDecodeFailed -> R.string.processing_error_final_decode
    PipelineFailureReason.UnknownDocument -> R.string.processing_error_unknown_document
    is PipelineFailureReason.Unexpected -> R.string.processing_error_unexpected
}

@Composable
private fun ProcessingErrorContent(
    message: String,
    onRetry: () -> Unit,
    onRetake: () -> Unit,
) {
    val title = stringResource(R.string.processing_error_title)
    val retryLabel = stringResource(R.string.processing_error_retry)
    val retakeLabel = stringResource(R.string.processing_error_retake)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                },
            )
            Text(
                text = message,
                color = Ink3,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(role = Role.Button, onClickLabel = retakeLabel, onClick = onRetake)
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = retakeLabel,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Primary)
                        .clickable(role = Role.Button, onClickLabel = retryLabel, onClick = onRetry)
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = retryLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

