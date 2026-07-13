package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.utils.community.CommunityThemePalette
import com.drdisagree.colorblendr.utils.community.communityColorScheme

// Rows of overlapping shade circles (primary, tertiary, neutral) with name +
// upvote count. Self-themed: container and text colors come from the theme's
// own derived palette, not the app theme.
@Composable
fun CommunityThemeCard(
    theme: CommunityTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDownloads: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val palette = remember(theme, isDark) { CommunityThemePalette.derive(theme, isDark) }

    // Real scheme roles from the generated theme (respects background
    // saturation/lightness and spec version).
    val baseScheme = MaterialTheme.colorScheme
    val scheme = remember(theme, isDark, baseScheme) {
        communityColorScheme(theme, isDark, baseScheme)
    }
    // Surface container tinted toward the accent for extra vibrancy.
    val container = lerp(scheme.surfaceContainer, scheme.primary, 0.12f)
    val onContainer = scheme.onSurface
    val subtle = scheme.onSurfaceVariant

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "communityCardScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        color = container,
        modifier = modifier
            .width(160.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Primary, tertiary, neutral shade rows.
            // Tones picked against the surface container so no circle blends
            // into the card.
            val shadeIndices = if (isDark) DARK_SHADE_INDICES else LIGHT_SHADE_INDICES
            listOf(0, 2, 3).forEach { row ->
                ShadeCircleRow(
                    shades = shadeIndices.map { Color(palette[row][it]) },
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = theme.name,
                color = onContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Outlined.ThumbUp),
                    contentDescription = null,
                    tint = subtle,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = theme.upvotes.toString(),
                    color = subtle,
                    fontSize = 12.sp
                )
                if (showDownloads) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Download),
                        contentDescription = null,
                        tint = subtle,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = theme.downloads.toString(),
                        color = subtle,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// Each circle overlaps the previous one; later children draw on top.
@Composable
private fun ShadeCircleRow(
    shades: List<Color>,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-6).dp),
        modifier = modifier
    ) {
        shades.forEach { shade ->
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(shade)
            )
        }
    }
}

// Tones picked to clear the surface container (tone ~10 dark / ~95 light).
private val DARK_SHADE_INDICES = listOf(2, 4, 6, 8)
private val LIGHT_SHADE_INDICES = listOf(4, 6, 8, 10)

internal val previewCommunityTheme = CommunityTheme(
    id = "preview-theme",
    name = "Ocean Breeze",
    description = "Calm blue theme for quiet minds.",
    author = "preview",
    style = MONET.TONAL_SPOT,
    seedColor = 0xFF0061A4.toInt(),
    secondaryColor = null,
    tertiaryColor = null,
    accentSaturation = 100,
    backgroundSaturation = 100,
    backgroundLightness = 100,
    modeSpecificThemes = false,
    accentSaturationLight = 100,
    backgroundSaturationLight = 100,
    backgroundLightnessLight = 100,
    accurateShades = true,
    colorSpecVersion = 0,
    pitchBlack = false,
    tintText = true,
    colorOverrides = emptyMap(),
    upvotes = 42,
    downloads = 128,
    createdAt = 0L
)

@Preview
@Composable
private fun CommunityThemeCardPreview() {
    ColorBlendrTheme {
        CommunityThemeCard(
            theme = previewCommunityTheme,
            onClick = {}
        )
    }
}
