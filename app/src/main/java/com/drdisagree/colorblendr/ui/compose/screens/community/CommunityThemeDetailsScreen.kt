package com.drdisagree.colorblendr.ui.compose.screens.community

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import com.drdisagree.colorblendr.data.common.Utilities.getCommunityThemeRepository
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.enums.ThemeModeSupport
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbar
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ConfirmDialog
import com.drdisagree.colorblendr.ui.compose.components.ToolbarIconPill
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.components.previewCommunityTheme
import com.drdisagree.colorblendr.ui.compose.components.previewContentPadding
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.compose.utils.LocalWidthClass
import com.drdisagree.colorblendr.ui.compose.utils.WidthClass
import com.drdisagree.colorblendr.ui.compose.utils.icon
import com.drdisagree.colorblendr.ui.compose.utils.labelRes
import com.drdisagree.colorblendr.utils.community.CommunityCount
import com.drdisagree.colorblendr.utils.community.CommunityReporter
import com.drdisagree.colorblendr.utils.community.CommunityThemeApplier
import com.drdisagree.colorblendr.utils.community.CommunityThemePalette
import com.drdisagree.colorblendr.utils.community.CommunityVotes
import com.drdisagree.colorblendr.utils.community.TestThemeHolder
import com.drdisagree.colorblendr.utils.community.communityColorScheme
import kotlinx.coroutines.launch

// Theme details, rendered entirely in the theme's own colors (scoped
// MaterialTheme — nothing staged, gone on back). Large swatch, name,
// description, vote stats; apply is root-only and starts a normal staged
// preview.
@Composable
fun CommunityThemeDetailsScreen(themeId: String) {
    val inspection = LocalInspectionMode.current
    val isTestTheme = TestThemeHolder.isPreviewId(themeId)
    var theme by remember {
        mutableStateOf(if (inspection) previewCommunityTheme else null)
    }
    var upvoted by remember { mutableStateOf(false) }
    var upvotes by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    if (!inspection) {
        LaunchedEffect(themeId) {
            theme = if (isTestTheme) {
                TestThemeHolder.theme
            } else {
                val repository = getCommunityThemeRepository()
                // Deep link on cold cache: fetch the index, then retry.
                repository.getThemeById(themeId) ?: run {
                    repository.refreshIndex()
                    repository.getThemeById(themeId)
                }
            }

            if (!isTestTheme) {
                upvoted = themeId in CommunityVotes.votedIds()
                // Server state wins — restores votes after reinstall.
                CommunityVotes.sync()?.let { upvoted = themeId in it }
            }
        }
    }

    if (theme == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            ContainedLoadingIndicator()
        }
    }

    theme?.let {
        DetailsContent(
            theme = it,
            shareable = !isTestTheme,
            upvoted = upvoted,
            upvotes = upvotes ?: it.upvotes,
            onUpvoteToggle = if (isTestTheme) {
                null
            } else {
                {
                    scope.launch {
                        CommunityVotes.toggle(themeId)?.let { result ->
                            upvoted = result.voted
                            upvotes = result.upvotes
                            getCommunityThemeRepository()
                                .updateUpvotes(themeId, result.upvotes)
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DetailsContent(
    theme: CommunityTheme,
    shareable: Boolean,
    upvoted: Boolean,
    upvotes: Int,
    onUpvoteToggle: (() -> Unit)?
) {
    val systemDark = isSystemInDarkTheme()
    val modeSupport = remember(theme) { CommunityThemePalette.modeSupport(theme) }
    val isDark = remember(theme, systemDark) { CommunityThemePalette.displayDark(theme, systemDark) }
    val baseScheme = MaterialTheme.colorScheme
    val palette = remember(theme, isDark) { CommunityThemePalette.derive(theme, isDark) }
    val scopedScheme = remember(theme, isDark, baseScheme) {
        communityColorScheme(theme, isDark, baseScheme)
    }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val rootMode = if (LocalInspectionMode.current) true else remember { isRootMode() }
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }

    SystemBarAppearance(isDark = isDark)

    // Scoped theming: only this screen wears the theme; navigating away
    // simply recomposes the rest of the app untouched.
    MaterialTheme(colorScheme = scopedScheme) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            val context = LocalContext.current
            val shareMessage = stringResource(R.string.share_theme_message, theme.name)
            val reportSentText = stringResource(R.string.report_sent)
            val reportFailedText = stringResource(R.string.report_failed)
            var showReportDialog by rememberSaveable { mutableStateOf(false) }

            if (showReportDialog) {
                ConfirmDialog(
                    title = stringResource(R.string.report_theme_title),
                    message = stringResource(R.string.report_theme_desc),
                    confirmText = stringResource(R.string.report),
                    onConfirm = {
                        showReportDialog = false
                        scope.launch {
                            AppSnackbar.show(
                                if (CommunityReporter.report(theme.id)) {
                                    reportSentText
                                } else {
                                    reportFailedText
                                }
                            )
                        }
                    },
                    onDismiss = { showReportDialog = false }
                )
            }

            Column {
                AppToolbar(
                    showBackButton = true,
                    lifted = toolbarLifted,
                    actions = {
                        if (shareable) {
                            ToolbarIconPill(
                                icon = rememberVectorPainter(Icons.Rounded.Flag),
                                width = 40.dp,
                                contentDescription = stringResource(R.string.report),
                                onClick = { showReportDialog = true }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ToolbarIconPill(
                                icon = rememberVectorPainter(Icons.Rounded.Share),
                                width = 40.dp,
                                contentDescription = stringResource(R.string.share_theme),
                                onClick = {
                                    val link = "$COMMUNITY_WORKER_URL/theme/${theme.id}"
                                    context.startActivity(
                                        Intent.createChooser(
                                            Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "$shareMessage\n$link"
                                                )
                                            },
                                            null
                                        )
                                    )
                                }
                            )
                        }
                    }
                )

                val twoPane = LocalWidthClass.current == WidthClass.Expanded
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .contentWidthLimit(max = if (twoPane) 1100.dp else 640.dp)
                        .verticalScroll(scrollState)
                        .padding(previewContentPadding())
                        .padding(horizontal = 16.dp)
                ) {
                    val info: @Composable () -> Unit = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = theme.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = theme.author.ifEmpty {
                                            stringResource(R.string.anonymous)
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .basicMarquee(iterations = Int.MAX_VALUE)
                                    )
                                    Text(
                                        text = pluralStringResource(
                                            R.plurals.community_applies,
                                            theme.downloads,
                                            CommunityCount.compact(theme.downloads)
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                                ModeSupportBadge(
                                    support = modeSupport,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            VoteButton(
                                upvoted = upvoted,
                                upvotes = upvotes,
                                onToggle = onUpvoteToggle?.let { toggle ->
                                    {
                                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                        toggle()
                                    }
                                },
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }

                        if (theme.description.isNotEmpty()) {
                            Text(
                                text = theme.description,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                    CommunityThemeApplier.stageForPreview(theme)
                                    scope.launch { PreviewController.updatePreview() }
                                },
                                enabled = rootMode,
                                shapes = ButtonDefaults.shapes(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = stringResource(R.string.try_this_creation))
                            }

                            if (!rootMode) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.root_required),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (twoPane) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) {
                                LargeSwatch(palette = palette)
                            }
                            Spacer(modifier = Modifier.size(24.dp))
                            Column(modifier = Modifier.weight(1f)) { info() }
                        }
                    } else {
                        LargeSwatch(palette = palette)
                        info()
                    }
                }
            }
        }
    }
}

// All five palette rows as large overlapping shade circles.
@Composable
private fun LargeSwatch(palette: List<List<Int>>) {
    val shadeIndices = listOf(3, 4, 6, 8, 10)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            (0..4).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    shadeIndices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(palette[row][index]))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemBarAppearance(isDark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    DisposableEffect(view, isDark) {
        val window = view.context.findActivity()?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val lightStatusBars = controller.isAppearanceLightStatusBars
        val lightNavigationBars = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
        onDispose {
            controller.isAppearanceLightStatusBars = lightStatusBars
            controller.isAppearanceLightNavigationBars = lightNavigationBars
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun ModeSupportBadge(
    support: ThemeModeSupport,
    modifier: Modifier = Modifier
) {
    val icon = support.icon ?: return
    val label = support.labelRes ?: return
    val warning = support == ThemeModeSupport.LOW_CONTRAST

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (warning) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        contentColor = if (warning) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                painter = rememberVectorPainter(icon),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(label),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VoteButton(
    upvoted: Boolean,
    upvotes: Int,
    onToggle: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = { onToggle?.invoke() },
        enabled = onToggle != null,
        shapes = ButtonDefaults.shapes(),
        modifier = modifier
    ) {
        Icon(
            painter = rememberVectorPainter(
                if (upvoted) Icons.Rounded.ThumbUp else Icons.Outlined.ThumbUp
            ),
            contentDescription = stringResource(R.string.upvote),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = CommunityCount.compact(upvotes))
    }
}

@Preview
@Composable
private fun VoteButtonPreview() {
    ColorBlendrTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VoteButton(upvoted = false, upvotes = 42, onToggle = {})
            VoteButton(upvoted = true, upvotes = 43, onToggle = {})
        }
    }
}

@Preview
@Composable
private fun ModeSupportBadgePreview() {
    ColorBlendrTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeSupportBadge(support = ThemeModeSupport.DARK_ONLY)
            ModeSupportBadge(support = ThemeModeSupport.LOW_CONTRAST)
        }
    }
}

@AdaptivePreviews
@Composable
private fun CommunityThemeDetailsScreenPreview() {
    ColorBlendrTheme {
        CommunityThemeDetailsScreen(themeId = "preview-theme")
    }
}