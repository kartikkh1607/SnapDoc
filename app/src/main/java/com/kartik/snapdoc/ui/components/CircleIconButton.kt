package com.kartik.snapdoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.ui.tooling.preview.Preview
import com.kartik.snapdoc.ui.theme.SnapDocCorners
import com.kartik.snapdoc.ui.theme.SnapDocTheme
import com.kartik.snapdoc.ui.theme.s1

/**
 * Standard rounded icon button used by Preview / DocDetail / PrintSheet headers.
 *
 * Camera and Review use distinct translucent-glass variants and stay local —
 * this component is for surface-on-background headers only.
 */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    background: Color = MaterialTheme.colorScheme.surface,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .s1(14.dp)
            .clip(SnapDocCorners.card)
            .background(background)
            .clickable(
                onClickLabel = contentDescription,
                role = Role.Button,
                onClick = onClick,
            ),
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

@Preview(showBackground = true)
@Composable
private fun CircleIconButtonPreview() {
    SnapDocTheme {
        CircleIconButton(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            onClick = {},
            contentDescription = "Back",
            modifier = Modifier.padding(16.dp),
        )
    }
}
