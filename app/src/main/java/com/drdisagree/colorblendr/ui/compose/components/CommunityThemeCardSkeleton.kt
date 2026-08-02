package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

@Composable
fun CommunityThemeCardSkeleton(
    pulse: Float,
    modifier: Modifier = Modifier
) {
    Box {
        CommunityThemeCard(
            theme = previewCommunityTheme,
            onClick = {},
            modifier = modifier.alpha(0f)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(pulse)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
    }
}

@Composable
fun rememberSkeletonPulse(): Float {
    val pulse by rememberInfiniteTransition(label = "communitySkeleton").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "communitySkeletonAlpha"
    )
    return pulse
}

@Preview
@Composable
private fun CommunityThemeCardSkeletonPreview() {
    ColorBlendrTheme {
        CommunityThemeCardSkeleton(pulse = 0.7f)
    }
}