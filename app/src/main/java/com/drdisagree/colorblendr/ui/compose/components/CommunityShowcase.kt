package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.CommunityViewModel
import com.drdisagree.colorblendr.utils.community.CommunityTrending
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

// Community themes showcase: header with View all, then an endlessly
// drifting carousel of trending cards. Count = cards that fit the larger
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
            allThemes?.let { themes ->
                showcaseIds = withContext(Dispatchers.Default) {
                    CommunityTrending.top(themes, showcaseCount).map { it.id }
                }
            }
        }
    }

    // Remap latched ids to current themes so vote counts stay live without
    // changing the set or order. Only the latched ids get mapped — no map of
    // the whole cache.
    val showcase = remember(showcaseIds, allThemes) {
        showcaseIds?.let { ids ->
            val wanted = ids.toHashSet()
            val byId = HashMap<String, CommunityTheme>(ids.size)
            allThemes.orEmpty().forEach { if (it.id in wanted) byId[it.id] = it }
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
// null themes -> pulsing placeholders sized by an invisible real card
@Composable
private fun DriftingCarousel(
    themes: List<CommunityTheme>?,
    onThemeClick: (CommunityTheme) -> Unit
) {
    val windowWidthDp = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    val shimmerCount = (windowWidthDp.value / (CARD_WIDTH_DP + CARD_GAP_DP)).toInt() + 1

    // Looped item space so the row never ends; start in the middle aligned to
    // a list-size multiple.
    val infinite = (themes?.size ?: 0) > 1
    val itemCount = when {
        themes == null -> shimmerCount
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
    // 0.4dp per 60Hz frame = 25dp/s, now refresh-rate independent.
    val driftPerSecondPx = with(LocalDensity.current) { 25.dp.toPx() }

    // Drift only while the screen is resumed and the user isn't dragging.
    val lifecycleState by LocalLifecycleOwner.current.lifecycle
        .currentStateAsState()
    val resumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    LaunchedEffect(themes, dragged, resumed) {
        if (!infinite || dragged || !resumed) return@LaunchedEffect
        // Frame-clock driven: scroll by the real elapsed time each frame so
        // speed and smoothness match any display refresh rate. Delta clamped
        // so a stall doesn't cause a jump.
        var lastFrameNanos = withFrameNanos { it }
        while (isActive) {
            val frameNanos = withFrameNanos { it }
            val deltaSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f)
                .coerceAtMost(0.1f)
            lastFrameNanos = frameNanos
            listState.scrollBy(driftPerSecondPx * deltaSeconds)
        }
    }

    val pulse = rememberSkeletonPulse()

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
                CommunityThemeCardSkeleton(pulse = pulse)
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
