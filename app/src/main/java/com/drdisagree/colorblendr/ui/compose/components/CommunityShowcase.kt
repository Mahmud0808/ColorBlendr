package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.CommunityViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

// Community themes showcase: header with View all, then an endlessly
// drifting carousel of top-voted cards. Count = cards that fit the larger
// screen dimension + 3 extra (or fewer if the cloud has fewer). First run
// shows shimmer while the first fetch lands.
@Composable
fun CommunityShowcase(
    onViewAll: () -> Unit,
    onThemeClick: (CommunityTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        ShowcaseContent(
            showcase = listOf(previewCommunityTheme),
            onViewAll = onViewAll,
            onThemeClick = onThemeClick,
            modifier = modifier
        )
        return
    }

    val communityViewModel: CommunityViewModel = viewModel()
    val allThemes by communityViewModel.allThemes.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        communityViewModel.refreshFromCache()
        onPauseOrDispose { }
    }

    // Offline first run: nothing cached, nothing fetched — hide the section.
    if (allThemes?.isEmpty() == true) return

    // Cards that span the larger window dimension (card + gap), plus 3.
    // max() is rotation-invariant, so the count is stable across rotation.
    val containerSize = LocalWindowInfo.current.containerSize
    val largerDimDp = with(LocalDensity.current) {
        maxOf(containerSize.width, containerSize.height).toDp()
    }
    val showcaseCount = (largerDimDp.value / (CARD_WIDTH_DP + CARD_GAP_DP)).toInt() + 3

    // Latch which cards and in what order, once, so the set stays fixed while
    // shown; a fresh screen entry re-latches from cache.
    var showcaseIds by rememberSaveable { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(allThemes, showcaseCount) {
        if (showcaseIds == null) {
            allThemes?.let {
                showcaseIds = it.sortedByDescending { theme -> theme.upvotes }
                    .take(showcaseCount)
                    .map { theme -> theme.id }
            }
        }
    }

    // Remap latched ids to current themes so vote counts stay live without
    // changing the set or order.
    val showcase = remember(showcaseIds, allThemes) {
        showcaseIds?.let { ids ->
            val byId = allThemes.orEmpty().associateBy { it.id }
            ids.mapNotNull { byId[it] }
        }
    }

    ShowcaseContent(
        showcase = showcase,
        onViewAll = onViewAll,
        onThemeClick = onThemeClick,
        modifier = modifier
    )
}

// CommunityThemeCard default width + LazyRow gap.
private const val CARD_WIDTH_DP = 160
private const val CARD_GAP_DP = 10

@Composable
private fun ShowcaseContent(
    showcase: List<CommunityTheme>?,
    onViewAll: () -> Unit,
    onThemeClick: (CommunityTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.community_themes),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onViewAll) {
                Text(text = stringResource(R.string.view_all))
                Icon(
                    painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.KeyboardArrowRight),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp)
                )
            }
        }

        DriftingCarousel(
            themes = showcase,
            onThemeClick = onThemeClick
        )
    }
}

// One container for loading and loaded states, so spacing is identical:
// null themes -> three pulsing placeholders sized by an invisible real card.
@Composable
private fun DriftingCarousel(
    themes: List<CommunityTheme>?,
    onThemeClick: (CommunityTheme) -> Unit
) {
    // Looped item space so the row never ends; start in the middle aligned to
    // a list-size multiple.
    val infinite = (themes?.size ?: 0) > 1
    val itemCount = when {
        themes == null -> 3
        infinite -> Int.MAX_VALUE
        else -> themes.size
    }
    val startIndex = if (infinite) {
        (Int.MAX_VALUE / 2).let { it - it % themes!!.size }
    } else {
        0
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)

    // Constant drift, paused while the user holds or drags.
    val dragged by listState.interactionSource.collectIsDraggedAsState()
    val driftPerTick = with(LocalDensity.current) { 0.4.dp.toPx() }

    // Drift only while the screen is resumed and the user isn't dragging.
    val lifecycleState by LocalLifecycleOwner.current.lifecycle
        .currentStateAsState()
    val resumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    LaunchedEffect(themes, dragged, resumed) {
        if (!infinite || dragged || !resumed) return@LaunchedEffect
        while (isActive) {
            listState.scrollBy(driftPerTick)
            delay(16)
        }
    }

    val pulse by rememberInfiniteTransition(label = "showcaseShimmer").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "showcaseShimmerAlpha"
    )

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = themes != null,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    ) {
        items(itemCount) { index ->
            if (themes == null) {
                Box {
                    CommunityThemeCard(
                        theme = previewCommunityTheme,
                        onClick = {},
                        modifier = Modifier.alpha(0f)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(pulse)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    )
                }
            } else {
                val theme = themes[index % themes.size]
                CommunityThemeCard(
                    theme = theme,
                    onClick = { onThemeClick(theme) }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityShowcasePreview() {
    ColorBlendrTheme {
        CommunityShowcase(
            onViewAll = {},
            onThemeClick = {}
        )
    }
}
