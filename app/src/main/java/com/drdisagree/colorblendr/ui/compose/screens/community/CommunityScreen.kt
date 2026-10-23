package com.drdisagree.colorblendr.ui.compose.screens.community

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.domain.AppScope
import com.drdisagree.colorblendr.data.enums.CommunitySort
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbar
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.CommunityThemeCard
import com.drdisagree.colorblendr.ui.compose.components.CommunityThemeCardSkeleton
import com.drdisagree.colorblendr.ui.compose.components.ExpressiveEmptyState
import com.drdisagree.colorblendr.ui.compose.components.SearchBar
import com.drdisagree.colorblendr.ui.compose.components.SingleChoiceDialog
import com.drdisagree.colorblendr.ui.compose.components.ToolbarIconPill
import com.drdisagree.colorblendr.ui.compose.components.TurnstileChallenge
import com.drdisagree.colorblendr.ui.compose.components.previewContentPadding
import com.drdisagree.colorblendr.ui.compose.components.rememberSkeletonPulse
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.viewmodels.CommunityViewModel
import com.drdisagree.colorblendr.utils.community.CommunityColorMatch
import com.drdisagree.colorblendr.utils.community.CommunityTextRules
import com.drdisagree.colorblendr.utils.community.CommunityThemeCodec
import com.drdisagree.colorblendr.utils.community.CommunityTrending
import com.drdisagree.colorblendr.utils.community.CommunityUploader
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerDialog
import me.jfenn.colorpickerdialog.compose.dialogs.ColorPickerType
import android.graphics.Color as AndroidColor

// All community creations: searchable, sortable (upvotes / downloads /
// latest) adaptive grid of self-themed cards.
@Composable
fun CommunityScreen(
    onThemeClick: (String) -> Unit = {}
) {
    if (LocalInspectionMode.current) {
        CommunityScreenContent(
            themes = emptyList(),
            sort = CommunitySort.UPVOTES,
            onSortChange = {},
            searchState = rememberTextFieldState(),
            onThemeClick = onThemeClick
        )
        return
    }

    val communityViewModel: CommunityViewModel = viewModel()
    val themes by communityViewModel.allThemes.collectAsStateWithLifecycle()
    val sort by communityViewModel.sort.collectAsStateWithLifecycle()
    val searchState = rememberTextFieldState(communityViewModel.query.value)

    LaunchedEffect(searchState) {
        snapshotFlow { searchState.text.toString() }.collect(communityViewModel::setQuery)
    }

    LifecycleResumeEffect(Unit) {
        communityViewModel.refreshFromCache()
        communityViewModel.refreshIfStale()
        onPauseOrDispose { }
    }

    CommunityScreenContent(
        themes = themes,
        sort = sort,
        onSortChange = communityViewModel::setSort,
        searchState = searchState,
        onThemeClick = onThemeClick
    )
}

private const val CARD_MIN_WIDTH_DP = 160
private const val CARD_HEIGHT_DP = 180

@Composable
private fun CommunityScreenContent(
    themes: List<CommunityTheme>?,
    sort: CommunitySort,
    onSortChange: (CommunitySort) -> Unit,
    searchState: TextFieldState,
    onThemeClick: (String) -> Unit
) {
    val query = searchState.text.toString()
    val hazeState = remember { HazeState() }
    val gridState = rememberLazyGridState()
    val toolbarLifted by remember {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0
        }
    }
    var lastSort by remember { mutableStateOf(sort) }
    LaunchedEffect(sort) {
        if (sort != lastSort) {
            lastSort = sort
            gridState.scrollToItem(0)
        }
    }

    val sorted = remember(themes, sort, query) {
        val base = when (sort) {
            CommunitySort.TRENDING -> themes?.let { CommunityTrending.sort(it) }
            CommunitySort.UPVOTES -> themes?.sortedByDescending { it.upvotes }
            CommunitySort.DOWNLOADS -> themes?.sortedByDescending { it.downloads }
            CommunitySort.LATEST -> themes?.sortedByDescending { it.createdAt }
        }
        val trimmed = query.trim()
        val colorQuery = CommunityColorMatch.parseQuery(trimmed)
        when {
            trimmed.isEmpty() -> base
            colorQuery != null -> base?.filter {
                CommunityColorMatch.matches(it.seedColor, colorQuery)
            }

            else -> base?.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                        it.author.contains(trimmed, ignoreCase = true)
            }
        }
    }

    var showColorPicker by remember { mutableStateOf(false) }
    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = CommunityColorMatch.parseQuery(query) ?: 0xFF51BDFF.toInt(),
            onDismissRequest = { showColorPicker = false },
            onColorPicked = { color ->
                showColorPicker = false
                searchState.setTextAndPlaceCursorAtEnd(String.format("#%06X", 0xFFFFFF and color))
            },
            alphaEnabled = false,
            pickers = listOf(
                ColorPickerType.WHEEL,
                ColorPickerType.RGB,
                ColorPickerType.HSV
            ),
            cornerRadius = 24.dp
        )
    }

    var showSortDialog by rememberSaveable { mutableStateOf(false) }
    if (showSortDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.sort_by),
            options = listOf(
                stringResource(R.string.sort_trending),
                stringResource(R.string.sort_upvotes),
                stringResource(R.string.sort_downloads),
                stringResource(R.string.sort_latest)
            ),
            selectedIndex = sort.ordinal,
            onSelect = { onSortChange(CommunitySort.entries[it]) },
            onDismiss = { showSortDialog = false }
        )
    }

    val rootMode = if (LocalInspectionMode.current) true else remember { isRootMode() }
    var showShareDialog by rememberSaveable { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.community_themes),
                showBackButton = true,
                lifted = toolbarLifted,
                actions = {
                    ToolbarIconPill(
                        icon = rememberVectorPainter(Icons.Rounded.IosShare),
                        width = 40.dp,
                        enabled = rootMode,
                        contentDescription = stringResource(R.string.share_theme),
                        onClick = { showShareDialog = true }
                    )
                }
            )

            if (showShareDialog) {
                ShareThemeDialog(onDismiss = { showShareDialog = false })
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    sorted == null -> SkeletonGrid(hazeState = hazeState)

                    sorted.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeSource(hazeState)
                        ) {
                            EmptyState()
                        }
                    }

                    else -> LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(CARD_MIN_WIDTH_DP.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = previewContentPadding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 72.dp,
                            bottom = 16.dp
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState)
                    ) {
                        items(sorted, key = { it.id }) { theme ->
                            CommunityThemeCard(
                                theme = theme,
                                onClick = { onThemeClick(theme.id) },
                                modifier = Modifier.fillMaxWidth(),
                                showDownloads = true
                            )
                        }
                    }
                }

                SearchBar(
                    state = searchState,
                    onFilterClick = { showSortDialog = true },
                    hazeState = hazeState,
                    onColorPickClick = { showColorPicker = true },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun SkeletonGrid(hazeState: HazeState) {
    val pulse = rememberSkeletonPulse()
    val containerSize = LocalWindowInfo.current.containerSize
    val heightDp = with(LocalDensity.current) { containerSize.height.toDp() }
    val widthDp = with(LocalDensity.current) { containerSize.width.toDp() }
    val columns = maxOf(1, ((widthDp.value - 32) / (CARD_MIN_WIDTH_DP + 10)).toInt())
    val rows = (heightDp.value / CARD_HEIGHT_DP).toInt() + 1

    LazyVerticalGrid(
        columns = GridCells.Adaptive(CARD_MIN_WIDTH_DP.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = false,
        contentPadding = previewContentPadding(
            start = 16.dp,
            end = 16.dp,
            top = 84.dp,
            bottom = 16.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .hazeSource(hazeState)
    ) {
        items(columns * rows) {
            CommunityThemeCardSkeleton(
                pulse = { pulse.value },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Empty state for the community grid.
@Composable
private fun EmptyState() {
    ExpressiveEmptyState(
        icon = Icons.Rounded.Palette,
        title = stringResource(R.string.community_empty),
        description = stringResource(R.string.community_empty_desc)
    )
}

// Share current selections as a community theme: name/description dialog ->
// Turnstile -> worker opens a moderated PR.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShareThemeDialog(onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var showChallenge by remember { mutableStateOf(false) }
    val uploading by CommunityUploader.uploading.collectAsState()
    var challengeSolved by remember { mutableStateOf(false) }
    val submitting = uploading || challengeSolved
    val submissionsAtOpen = rememberSaveable { CommunityUploader.submissions.value }
    val nameError = CommunityTextRules.nameError(name)
    val descriptionError = CommunityTextRules.descriptionError(description)
    val submissions by CommunityUploader.submissions.collectAsState()

    LaunchedEffect(submissions) {
        if (submissions > submissionsAtOpen) onDismiss()
    }

    if (showChallenge) {
        TurnstileChallenge(
            onToken = { token ->
                showChallenge = false
                challengeSolved = true
                AppScope.launch {
                    val seed = getSeedColorValue(
                        getWallpaperColorList().firstOrNull() ?: AndroidColor.BLUE
                    )
                    val payload = CommunityThemeCodec.currentSettingsToUploadJson(
                        name = CommunityTextRules.normalizeName(name),
                        description = description.trim(),
                        author = author.trim(),
                        seedColor = seed
                    )
                    val queued = CommunityUploader.upload(payload, token)
                    challengeSolved = false
                    AppSnackbar.show(
                        appContext.getString(
                            if (queued) R.string.theme_submitted else R.string.theme_submit_failed
                        )
                    )
                }
            },
            onDismiss = { showChallenge = false }
        )
    }

    Dialog(onDismissRequest = { if (!submitting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.widthIn(max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.share_theme),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.share_theme_info),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    shape = RoundedCornerShape(16.dp),
                    value = name,
                    onValueChange = { name = it.take(CommunityTextRules.NAME_MAX_LENGTH) },
                    label = { Text(text = stringResource(R.string.theme_name)) },
                    singleLine = true,
                    enabled = !submitting,
                    isError = name.isNotEmpty() && nameError != null,
                    supportingText = {
                        FieldSupportingText(
                            error = nameError.takeIf { name.isNotEmpty() },
                            minLength = CommunityTextRules.NAME_MIN_LENGTH,
                            length = name.length,
                            maxLength = CommunityTextRules.NAME_MAX_LENGTH
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
                OutlinedTextField(
                    shape = RoundedCornerShape(16.dp),
                    value = description,
                    onValueChange = { description = it.take(CommunityTextRules.DESCRIPTION_MAX_LENGTH) },
                    label = { Text(text = stringResource(R.string.theme_description)) },
                    minLines = 2,
                    maxLines = 6,
                    enabled = !submitting,
                    isError = description.isNotEmpty() && descriptionError != null,
                    supportingText = {
                        FieldSupportingText(
                            error = descriptionError.takeIf { description.isNotEmpty() },
                            minLength = CommunityTextRules.DESCRIPTION_MIN_LENGTH,
                            length = description.length,
                            maxLength = CommunityTextRules.DESCRIPTION_MAX_LENGTH
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                OutlinedTextField(
                    shape = RoundedCornerShape(16.dp),
                    value = author,
                    onValueChange = { author = it.take(CommunityTextRules.AUTHOR_MAX_LENGTH) },
                    label = { Text(text = stringResource(R.string.author_optional)) },
                    singleLine = true,
                    enabled = !submitting,
                    supportingText = {
                        FieldSupportingText(
                            error = null,
                            minLength = 0,
                            length = author.length,
                            maxLength = CommunityTextRules.AUTHOR_MAX_LENGTH
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !submitting,
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                    Button(
                        onClick = { showChallenge = true },
                        enabled = !submitting && nameError == null && descriptionError == null,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (submitting) R.string.submitting_theme else R.string.submit
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldSupportingText(
    @StringRes error: Int?,
    minLength: Int,
    length: Int,
    maxLength: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = error?.let { stringResource(it, minLength) }.orEmpty(),
            modifier = Modifier.weight(1f)
        )
        Text(text = stringResource(R.string.community_text_counter, length, maxLength))
    }
}

@Preview
@Composable
private fun FieldSupportingTextPreview() {
    ColorBlendrTheme {
        FieldSupportingText(
            error = R.string.community_text_too_short,
            minLength = CommunityTextRules.NAME_MIN_LENGTH,
            length = 2,
            maxLength = CommunityTextRules.NAME_MAX_LENGTH
        )
    }
}

@AdaptivePreviews
@Composable
private fun SkeletonGridPreview() {
    ColorBlendrTheme {
        SkeletonGrid(hazeState = remember { HazeState() })
    }
}

@AdaptivePreviews
@Composable
private fun CommunityScreenPreview() {
    ColorBlendrTheme {
        CommunityScreen()
    }
}