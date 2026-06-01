package com.kartik.snapdoc.ui.screens.camera

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kartik.snapdoc.ui.components.CameraSilhouette
import com.kartik.snapdoc.ui.components.FaceOvalGuide
import com.kartik.snapdoc.ui.navigation.Routes
import com.kartik.snapdoc.ui.theme.Amber
import com.kartik.snapdoc.ui.theme.Primary
import com.kartik.snapdoc.ui.theme.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val docId: String = savedStateHandle.get<String>(Routes.Args.DOC_ID).orEmpty()
}

@Composable
fun CameraScreen(
    onClose: () -> Unit,
    onCaptured: (docId: String, imageUri: String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD3CFC6),
                        Color(0xFFB9B2A4),
                        Color(0xFF8A8273),
                        Color(0xFF5A5448),
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1800f),
                ),
            ),
    ) {
        // Silhouette in frame.
        CameraSilhouette(modifier = Modifier.fillMaxSize())

        // Glass top bar.
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
                    text = "Indian Passport",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "51 × 51 mm · WHITE BG",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            GlassChip(icon = Icons.Outlined.Bolt, onClick = {}, tint = Amber)
        }

        // Guidance pill.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 124.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Primary.copy(alpha = 0.92f))
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
                text = "Perfect — hold still",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        // Face oval guide.
        FaceOvalGuide(
            strokeColor = Color.White.copy(alpha = 0.85f),
            cornerColor = Amber,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 168.dp)
                .size(width = 248.dp, height = 320.dp),
        )

        // Live AI checks rail.
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 14.dp),
        ) {
            listOf(
                "Face centered",
                "Even lighting",
                "Plain background",
                "Eyes open",
            ).forEach { check -> CheckChip(label = check) }
        }

        // Bottom gradient + controls.
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

        // Mode tabs.
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
            CaptureButton(onClick = { onCaptured(viewModel.docId, "stub://captured") })
            FlipButton()
        }
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
private fun CheckChip(label: String) {
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
                .background(Success),
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
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
private fun GalleryThumb() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFCDD6CC))
            .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
    ) {
        CameraSilhouette(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.16f))
            .border(3.dp, Color.White.copy(alpha = 0.95f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(4.dp, Primary.copy(alpha = 0.4f), CircleShape),
        )
    }
}

@Composable
private fun FlipButton() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.14f)),
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
