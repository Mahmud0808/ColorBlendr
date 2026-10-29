package com.drdisagree.colorblendr.ui.compose.screens.perapp

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.data.models.AppThemedColors
import com.drdisagree.colorblendr.data.models.AppColorResource
import com.drdisagree.colorblendr.theme.ThemeEngine
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.override.ColorOverride
import com.drdisagree.colorblendr.theme.override.ResourceOverrides
import com.drdisagree.colorblendr.theme.scheme.ResolvedTheme
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbarHost
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ColorOverrideRow
import com.drdisagree.colorblendr.ui.compose.components.ColorOverrideSheet
import com.drdisagree.colorblendr.ui.compose.components.ConfirmDialog
import com.drdisagree.colorblendr.ui.compose.components.ExpressiveEmptyState
import com.drdisagree.colorblendr.ui.compose.components.InfoHint
import com.drdisagree.colorblendr.ui.compose.components.SearchBar
import com.drdisagree.colorblendr.ui.compose.components.SingleChoiceDialog
import com.drdisagree.colorblendr.ui.compose.components.ThemeModeToggle
import com.drdisagree.colorblendr.ui.compose.components.ToolbarIconPill
import com.drdisagree.colorblendr.ui.compose.components.WidgetPosition
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.components.navBottomInset
import com.drdisagree.colorblendr.ui.compose.components.previewContentPadding
import com.drdisagree.colorblendr.ui.compose.components.showSnackbarReplacing
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.viewmodels.AppColorsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.collectLatest

private const val FILTER_ALL = 0
private const val FILTER_EDITED = 1
private const val FILTER_THEMED = 2
private const val FILTER_OWN = 3

private val LIBRARY_PREFIXES = listOf(
    "abc_", "androidx_", "material_", "m3_", "mtrl_", "design_", "notification_",
    "browser_actions_", "cardview_", "common_google_", "googleg_", "gm3_", "gm_", "google_",
    "quantum_", "exo_", "preference_", "call_notification_", "tooltip_", "switch_thumb_",
    "ripple_material_", "background_material_", "bright_foreground_", "dim_foreground_",
    "foreground_material_", "highlighted_text_material_", "hint_foreground_material_",
    "primary_material_", "primary_dark_material_", "primary_text_", "secondary_text_",
    "accent_material_", "button_material_", "error_color_material_", "background_floating_material_"
)

@Composable
fun AppColorsScreen(packageName: String) {
    val viewModel: AppColorsViewModel = viewModel(key = packageName) {
        AppColorsViewModel(packageName)
    }
    val context = LocalContext.current
    val colors by viewModel.colors.collectAsStateWithLifecycle()
    val themed by viewModel.themed.collectAsStateWithLifecycle()
    val themingEnabled by viewModel.themingEnabled.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val overrides by viewModel.overrides.collectAsStateWithLifecycle()
    val label by viewModel.label.collectAsStateWithLifecycle()
    val appName = label ?: packageName
    val currentAppName by rememberUpdatedState(appName)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            Toast.makeText(context, context.getString(message, currentAppName), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.applied.collectLatest {
            val result = snackbarHostState.showSnackbarReplacing(
                message = context.getString(R.string.app_restart_hint, currentAppName),
                actionLabel = context.getString(R.string.app_restart),
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.restartApp()
        }
    }

    AppColorsContent(
        title = appName,
        colors = colors,
        themed = if (themingEnabled) themed else AppThemedColors(),
        theme = theme,
        overrides = overrides,
        snackbarHostState = snackbarHostState,
        onOverride = viewModel::setOverride,
        onClear = viewModel::clearOverrides
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppColorsContent(
    title: String,
    colors: List<AppColorResource>?,
    themed: AppThemedColors,
    theme: ResolvedTheme?,
    overrides: ResourceOverrides,
    snackbarHostState: SnackbarHostState,
    onOverride: (isDark: Boolean, name: String, override: ColorOverride?) -> Unit,
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
    var editingName by rememberSaveable { mutableStateOf<String?>(null) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    val modeOverrides = overrides.forMode(isDark)
    val modeThemed = themed.forMode(isDark)
    val scheme = theme?.scheme(isDark)

    val entries = remember(colors, modeThemed, themed.presetNames, isDark) {
        colors?.let { resources ->
            val known = resources.associate { it.name to if (isDark) it.dark else it.light }
            val presets = themed.presetNames.filter { it in modeThemed }
            (known.keys + presets).sorted().map { name ->
                AppColorEntry(
                    name = name,
                    color = modeThemed[name] ?: known[name],
                    themed = name in modeThemed,
                    preset = name in themed.presetNames
                )
            }
        }
    }

    val visible = remember(entries, query, filter, modeOverrides) {
        entries.orEmpty().filter { entry ->
            val matchesFilter = when (filter) {
                FILTER_EDITED -> entry.name in modeOverrides
                FILTER_THEMED -> entry.themed
                FILTER_OWN -> LIBRARY_PREFIXES.none(entry.name::startsWith)
                else -> true
            }
            matchesFilter && (query.isEmpty() || entry.name.contains(query, ignoreCase = true))
        }
    }

    if (showFilterDialog) {
        SingleChoiceDialog(
            title = stringResource(R.string.app_colors_filter_title),
            options = listOf(
                stringResource(R.string.app_colors_filter_all),
                stringResource(R.string.color_tokens_filter_edited),
                stringResource(R.string.app_colors_filter_themed),
                stringResource(R.string.app_colors_filter_own)
            ),
            selectedIndex = filter,
            onSelect = { filter = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (showResetDialog) {
        ConfirmDialog(
            title = stringResource(R.string.app_colors_reset_title),
            message = stringResource(
                R.string.app_colors_reset_message,
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

    val editing = editingName?.let { name -> entries?.firstOrNull { it.name == name } }
    if (editing != null && scheme != null) {
        val override = modeOverrides[editing.name]
        val baseColor = editing.color
        ColorOverrideSheet(
            title = editing.name,
            scheme = scheme,
            baseColor = baseColor,
            origin = baseColor?.let(scheme::closestShade),
            override = override,
            onOverride = { onOverride(isDark, editing.name, it) },
            onDismiss = { editingName = null }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = title,
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
                    item(key = "hint") {
                        InfoHint(
                            text = stringResource(R.string.app_colors_hint),
                            modifier = Modifier.padding(
                                bottom = dimensionResource(R.dimen.container_margin_bottom)
                            )
                        )
                    }
                    item(key = "mode") {
                        ThemeModeToggle(
                            isDark = isDark,
                            onChange = { dark ->
                                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                                isDark = dark
                            },
                            modifier = Modifier.padding(
                                bottom = dimensionResource(R.dimen.container_margin_bottom)
                            )
                        )
                    }

                    if (scheme != null) {
                        itemsIndexed(visible, key = { _, entry -> entry.name }) { index, entry ->
                            val override = modeOverrides[entry.name]
                            ColorOverrideRow(
                                title = entry.name,
                                color = override?.let(scheme::colorOf) ?: entry.color,
                                override = override,
                                note = when {
                                    entry.preset -> stringResource(R.string.app_colors_preset)
                                    entry.themed -> stringResource(R.string.app_colors_themed)
                                    else -> null
                                },
                                position = when {
                                    visible.size == 1 -> WidgetPosition.Single
                                    index == 0 -> WidgetPosition.Top
                                    index == visible.lastIndex -> WidgetPosition.Bottom
                                    else -> WidgetPosition.Middle
                                },
                                onClick = { editingName = entry.name },
                                onReset = {
                                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    onOverride(isDark, entry.name, null)
                                }
                            )
                        }
                    }
                }

                when {
                    colors == null || scheme == null -> ContainedLoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )

                    visible.isEmpty() -> ExpressiveEmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.app_colors_empty),
                        description = stringResource(
                            if (entries.isNullOrEmpty()) {
                                R.string.app_colors_empty_desc
                            } else {
                                R.string.color_tokens_empty_desc
                            }
                        )
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

                AppSnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = navBottomInset())
                )
            }
        }
    }
}

@AdaptivePreviews
@Composable
private fun AppColorsContentPreview() {
    val theme = ThemeEngine.resolve(
        source = ThemeSource(style = MONET.TONAL_SPOT, seedColor = 0xFF4285F4.toInt()),
        adjustments = ThemeAdjustments()
    )
    ColorBlendrTheme {
        AppColorsContent(
            title = "Settings",
            colors = listOf(
                AppColorResource("accent_color", 0xFF3F6AA0.toInt(), 0xFFA6C8FF.toInt()),
                AppColorResource("settingslib_materialColorSurface", 0xFFF8F9FF.toInt(), 0xFF111318.toInt())
            ),
            themed = AppThemedColors(
                dark = mapOf("settingslib_materialColorSurface" to 0xFF000000.toInt()),
                presetNames = setOf("settingslib_materialColorSurface")
            ),
            theme = theme,
            overrides = ResourceOverrides(
                light = mapOf("accent_color" to ColorOverride.Shade(PaletteRole.PRIMARY, 8))
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onOverride = { _, _, _ -> },
            onClear = {}
        )
    }
}
