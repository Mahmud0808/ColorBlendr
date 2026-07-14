package com.drdisagree.colorblendr.ui.compose.screens.community

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Share
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import com.drdisagree.colorblendr.data.common.Utilities.getCommunityThemeRepository
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbar
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ConfirmDialog
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.ui.compose.components.previewCommunityTheme
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
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
    val isTestTheme = themeId == TestThemeHolder.TEST_THEME_ID
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
    val isDark = isSystemInDarkTheme()
    val baseScheme = MaterialTheme.colorScheme
    val scopedScheme = remember(theme, isDark, baseScheme) {
        communityColorScheme(theme, isDark, baseScheme)
    }
    val palette = remember(theme, isDark) { CommunityThemePalette.derive(theme, isDark) }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val rootMode = if (LocalInspectionMode.current) true else remember { isRootMode() }

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
                    title = theme.name,
                    showBackButton = true,
                    actions = {
                        if (shareable) {
                            IconButton(onClick = { showReportDialog = true }) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.Flag),
                                    contentDescription = stringResource(R.string.report),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
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
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.Share),
                                    contentDescription = stringResource(R.string.share_theme),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = LocalPreviewBottomInset.current)
                        .padding(horizontal = 16.dp)
                ) {
                    LargeSwatch(palette = palette, isDark = isDark)

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
                            Text(
                                text = theme.author.ifEmpty {
                                    stringResource(R.string.anonymous)
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }

                        StatChip(
                            icon = if (upvoted) {
                                rememberVectorPainter(Icons.Rounded.ThumbUp)
                            } else {
                                rememberVectorPainter(Icons.Outlined.ThumbUp)
                            },
                            value = upvotes,
                            onClick = onUpvoteToggle?.let { toggle ->
                                {
                                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                    toggle()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        StatChip(
                            icon = rememberVectorPainter(Icons.Rounded.Download),
                            value = theme.downloads,
                            onClick = null
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

                    Box(modifier = Modifier.fillMaxWidth()) {
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
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 0.dp, y = (-8).dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.root_required),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// All five palette rows as large overlapping shade circles.
@Composable
private fun LargeSwatch(
    palette: ArrayList<ArrayList<Int>>,
    isDark: Boolean
) {
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatChip(
    icon: Painter,
    value: Int,
    onClick: (() -> Unit)?
) {
    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(text = value.toString())
        }
    }

    if (onClick != null) {
        OutlinedButton(
            onClick = onClick,
            shapes = ButtonDefaults.shapes()
        ) {
            content()
        }
    } else {
        OutlinedButton(
            onClick = {},
            enabled = false,
            shapes = ButtonDefaults.shapes()
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun CommunityThemeDetailsScreenPreview() {
    ColorBlendrTheme {
        CommunityThemeDetailsScreen(themeId = "preview-theme")
    }
}
