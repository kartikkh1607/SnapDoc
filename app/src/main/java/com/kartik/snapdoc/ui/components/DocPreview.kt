package com.kartik.snapdoc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kartik.snapdoc.ui.theme.Hairline

// Mirrors `DocPreview` from tokens.jsx — a tiny passport-style
// mock with a face oval + shoulders so we don't pull in fake photos.

enum class DocKind { Passport, Aadhaar, Pan, Upsc, Visa, Ssc }

private data class Palette(val bg: Color, val shirt: Color)

private fun paletteFor(kind: DocKind): Palette = when (kind) {
    DocKind.Passport -> Palette(Color(0xFFF4F7F4), Color(0xFF2E4F7A))
    DocKind.Aadhaar -> Palette(Color(0xFFFFF8EC), Color(0xFF5C4633))
    DocKind.Pan -> Palette(Color(0xFFF1F4FA), Color(0xFF2E4F7A))
    DocKind.Upsc -> Palette(Color(0xFFF6F0EC), Color(0xFF5C4633))
    DocKind.Visa -> Palette(Color(0xFFEEF2F7), Color(0xFF2E4F7A))
    DocKind.Ssc -> Palette(Color(0xFFF3F0F7), Color(0xFF4B3A6B))
}

private val Skin = Color(0xFFE8D7C4)

@Composable
fun DocPreview(
    kind: DocKind,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 6.dp,
    showBorder: Boolean = false,
) {
    val palette = paletteFor(kind)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(palette.bg)
            .then(
                if (showBorder) Modifier.border(1.dp, Hairline, RoundedCornerShape(cornerRadius))
                else Modifier
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFaceShoulders(skin = Skin, shirt = palette.shirt)
        }
    }
}

// Centered head + shoulders silhouette, scaled to the available size.
private fun DrawScope.drawFaceShoulders(skin: Color, shirt: Color) {
    val w = size.width
    val h = size.height
    // Face oval centered at ~38% from top, radius proportional to box.
    val faceCx = w / 2f
    val faceCy = h * 0.40f
    val faceRx = w * 0.21f
    val faceRy = h * 0.21f
    drawOval(
        color = skin,
        topLeft = Offset(faceCx - faceRx, faceCy - faceRy),
        size = Size(faceRx * 2f, faceRy * 2f),
    )
    // Shoulders / shirt — wider oval near the bottom.
    val shCx = w / 2f
    val shCy = h * 0.95f
    val shRx = w * 0.34f
    val shRy = h * 0.22f
    drawOval(
        color = shirt,
        topLeft = Offset(shCx - shRx, shCy - shRy),
        size = Size(shRx * 2f, shRy * 2f),
    )
}

// A larger hero version used on detail / review / preview screens.
// Padding + outer card is the caller's job; this is just the inner art.
@Composable
fun DocPreviewHero(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Hairline, RoundedCornerShape(12.dp))
            .padding(0.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFaceShoulders(skin = Skin, shirt = Color(0xFF2E4F7A))
        }
    }
}

// Convenience overload to wrap with explicit size — handy in detail screens.
@Composable
fun DocPreviewHero(width: Dp, height: Dp) {
    DocPreviewHero(modifier = Modifier.size(width, height))
}

@Composable
fun ShoulderArt(modifier: Modifier = Modifier, shirtColor: Color = Color(0xFF2E4F7A)) {
    Canvas(modifier = modifier) {
        drawFaceShoulders(skin = Skin, shirt = shirtColor)
    }
}

// Used by the camera silhouette overlay.
@Composable
fun CameraSilhouette(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val faceCx = w / 2f
        val faceCy = h * 0.45f
        val faceRx = w * 0.21f
        val faceRy = h * 0.13f
        val silhouette = Color(0xFF3C3228).copy(alpha = 0.55f)
        drawOval(
            color = silhouette,
            topLeft = Offset(faceCx - faceRx, faceCy - faceRy),
            size = Size(faceRx * 2f, faceRy * 2f),
        )
        val shCx = w / 2f
        val shCy = h * 0.78f
        val shRx = w * 0.34f
        val shRy = h * 0.16f
        drawOval(
            color = silhouette,
            topLeft = Offset(shCx - shRx, shCy - shRy),
            size = Size(shRx * 2f, shRy * 2f),
        )
    }
}

@Preview(showBackground = true, widthDp = 72, heightDp = 90)
@Composable
private fun DocPreviewPassportPreview() {
    DocPreview(kind = DocKind.Passport, modifier = Modifier.size(72.dp, 90.dp))
}

@Preview(showBackground = true, widthDp = 72, heightDp = 90)
@Composable
private fun DocPreviewAadhaarPreview() {
    DocPreview(kind = DocKind.Aadhaar, modifier = Modifier.size(72.dp, 90.dp))
}

@Preview(showBackground = true, widthDp = 248, heightDp = 320, backgroundColor = 0xFF111111)
@Composable
private fun FaceOvalGuidePreview() {
    FaceOvalGuide(
        strokeColor = Color.White.copy(alpha = 0.85f),
        cornerColor = Color(0xFFFFB300),
        modifier = Modifier.size(248.dp, 320.dp),
    )
}

// Dashed face oval guide for the camera frame.
@Composable
fun FaceOvalGuide(strokeColor: Color, cornerColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val rx = w * 0.46f
        val ry = h * 0.47f
        drawOval(
            color = strokeColor,
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2f, ry * 2f),
            style = Stroke(width = 2.4f),
        )
        // Corner brackets.
        val bracketLen = 14f
        val brackets = listOf(
            Triple(24f, 24f, true to true),     // NW
            Triple(w - 24f, 24f, false to true),  // NE
            Triple(24f, h - 24f, true to false),    // SW
            Triple(w - 24f, h - 24f, false to false), // SE
        )
        brackets.forEach { (x, y, dir) ->
            val (west, north) = dir
            val dx = if (west) 1f else -1f
            val dy = if (north) 1f else -1f
            drawLine(
                color = cornerColor,
                start = Offset(x, y + dy * bracketLen),
                end = Offset(x, y),
                strokeWidth = 3f,
            )
            drawLine(
                color = cornerColor,
                start = Offset(x, y),
                end = Offset(x + dx * bracketLen, y),
                strokeWidth = 3f,
            )
        }
    }
}
