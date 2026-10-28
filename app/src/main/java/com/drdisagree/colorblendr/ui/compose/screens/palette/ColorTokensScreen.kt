package com.drdisagree.colorblendr.ui.compose.screens.palette

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.ThemeEngine
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.override.ColorOverride
import com.drdisagree.colorblendr.theme.override.TokenOverrides
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenCategory
import com.drdisagree.colorblendr.theme.token.TokenContrast
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ColorOverrideRow
import com.drdisagree.colorblendr.ui.compose.components.ColorOverrideSheet
import com.drdisagree.colorblendr.ui.compose.components.ConfirmDialog
import com.drdisagree.colorblendr.ui.compose.components.ExpressiveEmptyState
import com.drdisagree.colorblendr.ui.compose.components.ListSectionHeader
import com.drdisagree.colorblendr.ui.compose.components.SearchBar
import com.drdisagree.colorblendr.ui.compose.components.SingleChoiceDialog
import com.drdisagree.colorblendr.ui.compose.components.ThemeModeToggle
import com.drdisagree.colorblendr.ui.compose.components.ToolbarIconPill
import com.drdisagree.colorblendr.ui.compose.components.WidgetPosition
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.components.previewContentPadding
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.viewmodels.ColorTokensViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

private const val FILTER_ALL = 0
private const val FILTER_EDITED = 1
private const val FILTER_CATEGORY_OFFSET = 2

@Composable
fun ColorTokensScreen(
    viewModel: ColorTokensViewModel = viewModel(),
    onOpenGuide: () -> Unit = {}
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val overrides by viewModel.overrides.collectAsStateWithLifecycle()

    ColorTokensContent(
        theme = theme,
        overrides = overrides,
        onOverride = viewModel::setOverride,
        onClear = viewModel::clearOverrides,
        onOpenGuide = onOpenGuide
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorTokensContent(
    theme: ResolvedTheme?,
    overrides: TokenOverrides,
    onOverride: (isDark: Boolean, token: Token, override: ColorOverride?) -> Unit,
    onClear: (isDark: Boolean) -> Unit,
    onOpenGuide: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val resources = LocalContext.current.resources
    val usages = remember(resources) {
        Token.entries.associateWith { resources.getString(it.usageRes) }
    }
    val listState = rememberLazyListState()
    val hazeState = remember { HazeState() }
    val searchState = rememberTextFieldState()
    val query = searchState.text.toString().trim()
    val toolbarLifted by remember { derivedStateOf { listState.canScrollBackward } }

    val appDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    var isDark by rememberSaveable { mutableStateOf(appDark) }
    var filter by rememberSaveable { mutableIntStateOf(FILTER_ALL) }
    var editingToken by rememberSaveable { mutableStateOf<String?>(null) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    val modeOverrides = overrides.forMode(isDark)
    val scheme = theme?.scheme(isDark)
    val lowContrastTokens = remember(scheme, modeOverrides) {
        scheme?.let { TokenContrast.lowContrastTokens(it, modeOverrides.keys) }.orEmpty()
    }

    val sections = remember(query, filter, modeOverrides) {
        TokenCategory.entries.mapNotNull { category ->
            val tokens = category.tokens.filter { token ->
                val matchesFilter = when (filter) {
                    FILTER_ALL -> true
                    FILTER_EDITED -> token in modeOverrides
                    else -> category.ordinal == filter - FILTER_CATEGORY_OFFSET
                }
                matchesFilter && (query.isEmpty() ||
                        token.displayName.contains(query, ignoreCase = true) ||
                        token.resourceName.contains(query, ignoreCase = true) ||
                        usages[token].orEmpty().contains(query, ignoreCase = true))
            }
            if (tokens.isEmpty()) null else category to tokens
        }
    }

    val filterOptions = listOf(
        stringResource(R.string.color_tokens_filter_all),
        stringResource(R.string.color_tokens_filter_edited)
    ) + TokenCategory.entries.map { stringResource(it.labelRes) }

    if (showFilterDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.color_tokens_filter_title),
            options = filterOptions,
            selectedIndex = filter,
            onSelect = { filter = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (showResetDialog) {
        ConfirmDialog(
            title = stringResource(R.string.color_tokens_reset_title),
            message = stringResource(
                R.string.color_tokens_reset_message,
                stringResource(
                    if (isDark) R.string.color_tokens_mode_dark else R.string.color_tokens_mode_light
                ).lowercase()
            ),
            confirmText = stringResource(R.string.reset),
            destructive = true,
            onConfirm = {
                showResetDialog = false
                onClear(isDark)
            },
            onDismiss = { showResetDialog = false }
        )
    }

    val editing = editingToken?.let { name -> Token.entries.firstOrNull { it.name == name } }
    if (editing != null && scheme != null) {
        ColorOverrideSheet(
            title = editing.displayName,
            scheme = scheme,
            baseColor = scheme.base(editing),
            origin = scheme.originShade(editing),
            override = modeOverrides[editing],
            onOverride = { onOverride(isDark, editing, it) },
            onDismiss = { editingToken = null }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.color_tokens_title),
                showBackButton = true,
                lifted = toolbarLifted,
                actions = {
                    ToolbarIconPill(
                        icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.HelpOutline),
                        width = 40.dp,
                        contentDescription = stringResource(R.string.color_tokens_guide_title),
                        onClick = onOpenGuide
                    )
                    if (modeOverrides.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        ToolbarIconPill(
                            icon = rememberVectorPainter(Icons.Rounded.RestartAlt),
                            width = 40.dp,
                            destructive = true,
                            contentDescription = stringResource(R.string.reset),
                            onClick = { showResetDialog = true }
                        )
                    }
                }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    contentPadding = previewContentPadding(top = 72.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .contentWidthLimit()
                        .hazeSource(hazeState)
                ) {
                    item(key = "mode") {
                        ThemeModeToggle(
                            isDark = isDark,
                            onChange = { dark ->
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                isDark = dark
                            }
                        )
                    }

                    if (scheme != null) {
                        sections.forEach { (category, tokens) ->
                            item(key = "header_${category.name}") {
                                ListSectionHeader(text = stringResource(category.labelRes))
                            }
                            itemsIndexed(tokens, key = { _, token -> token.name }) { index, token ->
                                ColorOverrideRow(
                                    title = token.displayName,
                                    color = scheme[token],
                                    override = modeOverrides[token],
                                    lowContrast = token in lowContrastTokens,
                                    position = when {
                                        tokens.size == 1 -> WidgetPosition.Single
                                        index == 0 -> WidgetPosition.Top
                                        index == tokens.lastIndex -> WidgetPosition.Bottom
                                        else -> WidgetPosition.Middle
                                    },
                                    onClick = { editingToken = token.name },
                                    onReset = {
                                        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        onOverride(isDark, token, null)
                                    }
                                )
                            }
                        }
                    }
                }

                when {
                    scheme == null -> ContainedLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )

                    sections.isEmpty() -> ExpressiveEmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.color_tokens_empty),
                        description = stringResource(R.string.color_tokens_empty_desc)
                    )
                }

                SearchBar(
                    state = searchState,
                    onFilterClick = { showFilterDialog = true },
                    hazeState = hazeState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .contentWidthLimit()
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun ColorTokensContentPreview() {
    val theme = ThemeEngine.resolve(
        source = ThemeSource(style = MONET.TONAL_SPOT, seedColor = 0xFF4285F4.toInt()),
        adjustments = ThemeAdjustments()
    )
    ColorBlendrTheme {
        ColorTokensContent(
            theme = theme,
            overrides = TokenOverrides(
                light = mapOf(Token.SURFACE to ColorOverride.Custom(0xFFFAF7F2.toInt()))
            ),
            onOverride = { _, _, _ -> },
            onClear = {},
            onOpenGuide = {}
        )
    }
}
