package com.kartik.snapdoc.ui.screens.camera

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
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
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.kartik.snapdoc.data.specs.model.DocumentSpec
import com.kartik.snapdoc.domain.camera.FaceGuidanceAnalyzer
import com.kartik.snapdoc.domain.camera.FaceGuidanceState
import com.kartik.snapdoc.domain.camera.GuidanceChecks
import com.kartik.snapdoc.domain.camera.headline
import com.kartik.snapdoc.ui.components.FaceOvalGuide
import com.kartik.snapdoc.ui.theme.Amber
import com.kartik.snapdoc.ui.theme.ErrorRed
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.Success
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onClose: () -> Unit,
    onCaptured: (docId: String, imageUri: String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permission = rememberPermissionState(Manifest.permission.CAMERA)

    when (val status = permission.status) {
        is PermissionStatus.Granted -> CameraContent(
            state = state,
            onClose = onClose,
            onCaptured = onCaptured,
            onGuidance = viewModel::onGuidance,
            onToggleLens = viewModel::toggleLens,
            onCapturingChanged = viewModel::setCapturing,
            onError = viewModel::setError,
        )
        is PermissionStatus.Denied -> PermissionGate(
            shouldShowRationale = status.shouldShowRationale,
            onClose = onClose,
            onRequest = { permission.launchPermissionRequest() },
        )
    }
}

@Composable
private fun CameraContent(
    state: CameraUiState,
    onClose: () -> Unit,
    onCaptured: (docId: String, imageUri: String) -> Unit,
    onGuidance: (FaceGuidanceState, GuidanceChecks) -> Unit,
    onToggleLens: () -> Unit,
    onCapturingChanged: (Boolean) -> Unit,
    onError: (String?) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS)
            imageCaptureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
        }
    }

    val analyzer = remember(state.doc?.id) {
        state.doc?.let { doc ->
            FaceGuidanceAnalyzer(doc.face) { guidance, checks ->
                onGuidance(guidance, checks)
            }
        }
    }

    LaunchedEffect(state.lensFacing) {
        controller.cameraSelector = CameraSelector.Builder()
            .requireLensFacing(state.lensFacing)
            .build()
    }

    LaunchedEffect(analyzer) {
        if (analyzer != null) {
            controller.setImageAnalysisAnalyzer(mainExecutor, analyzer)
        } else {
            controller.clearImageAnalysisAnalyzer()
        }
    }

    DisposableEffect(lifecycleOwner) {
        controller.bindToLifecycle(lifecycleOwner)
        onDispose {
            controller.unbind()
            analyzer?.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    this.controller = controller
                }
            },
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 56.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF14141B).copy(alpha = 0.42f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                .padding(horizontal = 8.dp),
        ) {
            GlassChip(icon = Icons.Outlined.Close, onClick = onClose, tint = Color.White)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.doc?.displayName ?: "Document",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = state.doc?.let {
                        "${it.dimensions.widthMm.toInt()} × ${it.dimensions.heightMm.toInt()} mm · ${it.background.displayName.uppercase()} BG"
                    } ?: "",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            GlassChip(icon = Icons.Outlined.Bolt, onClick = {}, tint = Amber)
        }

        GuidancePill(
            guidance = state.guidance,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 124.dp),
        )

        FaceOvalGuide(
            strokeColor = Color.White.copy(alpha = 0.85f),
            cornerColor = if (state.guidance == FaceGuidanceState.Ready) Success else Amber,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 168.dp)
                .size(width = 248.dp, height = 320.dp),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 14.dp),
        ) {
            CheckChip("Face centered", state.checks.faceCentered)
            CheckChip("Even lighting", state.checks.evenLighting)
            CheckChip("Plain background", state.checks.plainBackground)
            CheckChip("Eyes open", state.checks.eyesOpen)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF080A08).copy(alpha = 0.7f),
                        ),
                    ),
                ),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 148.dp),
        ) {
            Text(
                text = "UPLOAD",
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CAMERA",
                    color = Amber,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Amber),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp, start = 36.dp, end = 36.dp)
                .fillMaxWidth(),
        ) {
            GalleryThumb()
            CaptureButton(
                enabled = state.captureEnabled,
                onClick = {
                    onCapturingChanged(true)
                    val docId = state.doc?.id ?: return@CaptureButton
                    val file = File(context.cacheDir, "snapdoc_capture_${docId}_${System.currentTimeMillis()}.jpg")
                    val output = ImageCapture.OutputFileOptions.Builder(file).build()
                    controller.takePicture(
                        output,
                        mainExecutor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(results: ImageCapture.OutputFileResults) {
                                onCapturingChanged(false)
                                val uri = results.savedUri ?: Uri.fromFile(file)
                                onCaptured(docId, uri.toString())
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e(TAG, "Capture failed", exception)
                                onCapturingChanged(false)
                                onError("Couldn't capture photo. Please try again.")
                            }
                        },
                    )
                },
            )
            FlipButton(onClick = onToggleLens)
        }
    }
}

@Composable
private fun PermissionGate(
    shouldShowRationale: Boolean,
    onClose: () -> Unit,
    onRequest: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (!shouldShowRationale) onRequest()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(36.dp),
                )
            }
            Text(
                text = "Camera permission needed",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "SnapDoc uses your camera to take a perfectly-sized document photo. Photos stay on your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onClose)
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Primary)
                        .clickable(onClick = onRequest)
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = "Grant access",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuidancePill(guidance: FaceGuidanceState, modifier: Modifier = Modifier) {
    val background = when (guidance) {
        FaceGuidanceState.Ready -> Primary.copy(alpha = 0.92f)
        FaceGuidanceState.NoFace, FaceGuidanceState.MultipleFaces -> ErrorRed.copy(alpha = 0.92f)
        else -> Color(0xFF14141B).copy(alpha = 0.78f)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(background)
            .padding(start = 12.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = guidance.headline(),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        )
    }
}

@Composable
private fun CheckChip(label: String, passed: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(Color(0xFF080A08).copy(alpha = 0.42f))
            .padding(start = 6.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(if (passed) Success else Color.White.copy(alpha = 0.20f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(10.dp),
            )
        }
        Text(
            text = label,
            color = Color.White.copy(alpha = if (passed) 1f else 0.55f),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun GlassChip(icon: ImageVector, onClick: () -> Unit, tint: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.10f))
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

@Composable
private fun GalleryThumb() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
    )
}

@Composable
private fun CaptureButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .border(3.dp, Color.White.copy(alpha = if (enabled) 0.95f else 0.5f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (enabled) Color.White else Color.White.copy(alpha = 0.6f))
                .border(4.dp, Primary.copy(alpha = if (enabled) 0.4f else 0.2f), CircleShape),
        )
    }
}

@Composable
private fun FlipButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Cameraswitch,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Suppress("unused")
private fun DocumentSpec.unused(): Int = popularity

private const val TAG = "CameraScreen"
