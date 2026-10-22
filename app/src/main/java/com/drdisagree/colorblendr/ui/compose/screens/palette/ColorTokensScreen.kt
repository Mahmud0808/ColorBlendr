package com.drdisagree.colorblendr.ui.compose.screens.palette

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.ThemeEngine
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.override.TokenOverride
import com.drdisagree.colorblendr.theme.override.TokenOverrides
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenCategory
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ConfirmDialog
import com.drdisagree.colorblendr.ui.compose.components.ExpressiveEmptyState
import com.drdisagree.colorblendr.ui.compose.components.PositionedCard
import com.drdisagree.colorblendr.ui.compose.components.SearchBar
import com.drdisagree.colorblendr.ui.compose.components.SingleChoiceDialog
import com.drdisagree.colorblendr.ui.compose.components.ToolbarIconPill
import com.drdisagree.colorblendr.ui.compose.components.WidgetPosition
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.components.previewContentPadding
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.viewmodels.ColorTokensViewModel
import com.drdisagree.colorblendr.utils.colors.ColorUtil.intToHexColor
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

private const val FILTER_ALL = 0
private const val FILTER_EDITED = 1
private const val FILTER_CATEGORY_OFFSET = 2

@Composable
fun ColorTokensScreen(
    viewModel: ColorTokensViewModel = viewModel()
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val overrides by viewModel.overrides.collectAsStateWithLifecycle()

    ColorTokensContent(
        theme = theme,
        overrides = overrides,
        onOverride = viewModel::setOverride,
        onClear = viewModel::clearOverrides
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorTokensContent(
    theme: ResolvedTheme?,
    overrides: TokenOverrides,
    onOverride: (isDark: Boolean, token: Token, override: TokenOverride?) -> Unit,
    onClear: (isDark: Boolean) -> Unit
) {
    val haptics = LocalHapticFeedback.current
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
                        token.resourceName.contains(query, ignoreCase = true))
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
        TokenEditorSheet(
            token = editing,
            scheme = scheme,
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
                    if (modeOverrides.isNotEmpty()) {
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
                        ModeToggle(
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
                                SectionHeader(text = stringResource(category.labelRes))
                            }
                            itemsIndexed(tokens, key = { _, token -> token.name }) { index, token ->
                                TokenRow(
                                    token = token,
                                    color = scheme[token],
                                    override = modeOverrides[token],
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ModeToggle(
    isDark: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf(false, true).forEach { dark ->
            ToggleButton(
                checked = isDark == dark,
                onCheckedChange = { if (isDark != dark) onChange(dark) },
                modifier = Modifier.widthIn(min = 120.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(
                        if (dark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        if (dark) R.string.color_tokens_mode_dark else R.string.color_tokens_mode_light
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = dimensionResource(R.dimen.container_margin_horizontal) + 16.dp,
            top = 12.dp,
            bottom = 8.dp
        )
    )
}

@Composable
private fun TokenRow(
    token: Token,
    @ColorInt color: Int,
    override: TokenOverride?,
    position: WidgetPosition,
    onClick: () -> Unit,
    onReset: () -> Unit
) {
    val hex = intToHexColor(color)
    val subtitle = when (override) {
        is TokenOverride.Shade -> stringResource(
            R.string.color_tokens_from_shade,
            hex,
            stringResource(override.role.labelRes),
            ShadeScale.shades[override.index]
        )

        is TokenOverride.Custom -> "$hex · ${stringResource(R.string.color_tokens_source_custom)}"
        null -> hex
    }

    PositionedCard(position = position, onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .padding(1.dp)
                    .background(Color(color), CircleShape)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = token.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (override != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (override != null) {
                IconButton(onClick = onReset) {
                    Icon(
                        painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.Undo),
                        contentDescription = stringResource(R.string.reset),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
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
                light = mapOf(Token.SURFACE to TokenOverride.Custom(0xFFFAF7F2.toInt()))
            ),
            onOverride = { _, _, _ -> },
            onClear = {}
        )
    }
}
