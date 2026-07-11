package com.drdisagree.colorblendr.ui.compose.screens.community

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.developerModeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.getSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.getWallpaperColorList
import com.drdisagree.colorblendr.data.enums.CommunitySort
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.CommunityThemeCard
import com.drdisagree.colorblendr.ui.compose.components.SearchBar
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.ui.compose.components.TurnstileChallenge
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.CommunityViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.drdisagree.colorblendr.utils.community.CommunityThemeCodec
import com.drdisagree.colorblendr.utils.community.CommunityUploader
import com.drdisagree.colorblendr.utils.community.TestThemeHolder
import kotlinx.coroutines.launch
import org.json.JSONObject
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
            onThemeClick = onThemeClick
        )
        return
    }

    val communityViewModel: CommunityViewModel = viewModel()
    val themes by communityViewModel.allThemes.collectAsStateWithLifecycle()
    val sort by communityViewModel.sort.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        communityViewModel.refreshFromCache()
        communityViewModel.refreshIfStale()
        onPauseOrDispose { }
    }

    CommunityScreenContent(
        themes = themes,
        sort = sort,
        onSortChange = communityViewModel::setSort,
        onThemeClick = onThemeClick
    )
}

@Composable
private fun CommunityScreenContent(
    themes: List<CommunityTheme>?,
    sort: CommunitySort,
    onSortChange: (CommunitySort) -> Unit,
    onThemeClick: (String) -> Unit
) {
    val context = LocalContext.current
    val hazeState = remember { HazeState() }
    var query by remember { mutableStateOf("") }

    val sorted = remember(themes, sort, query) {
        val base = when (sort) {
            CommunitySort.UPVOTES -> themes?.sortedByDescending { it.upvotes }
            CommunitySort.DOWNLOADS -> themes?.sortedByDescending { it.downloads }
            CommunitySort.LATEST -> themes?.sortedByDescending { it.createdAt }
        }
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            base
        } else {
            base?.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                        it.author.contains(trimmed, ignoreCase = true)
            }
        }
    }

    fun showSortDialog() {
        val items = arrayOf(
            context.getString(R.string.sort_upvotes),
            context.getString(R.string.sort_downloads),
            context.getString(R.string.sort_latest)
        )
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.sort_by))
            .setSingleChoiceItems(items, sort.ordinal) { dialog, which ->
                onSortChange(CommunitySort.entries[which])
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    val developerMode = if (LocalInspectionMode.current) {
        false
    } else {
        remember { developerModeEnabled() }
    }
    var showTestDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.community_themes),
                showBackButton = true,
                actions = {
                    if (developerMode) {
                        IconButton(onClick = { showTestDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_edit),
                                contentDescription = stringResource(R.string.test_theme),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = { showShareDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_share),
                            contentDescription = stringResource(R.string.share_theme),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            if (showShareDialog) {
                ShareThemeDialog(onDismiss = { showShareDialog = false })
            }

            if (showTestDialog) {
                TestThemeDialog(
                    onDismiss = { showTestDialog = false },
                    onLoaded = { theme ->
                        showTestDialog = false
                        TestThemeHolder.theme = theme
                        onThemeClick(TestThemeHolder.TEST_THEME_ID)
                    }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    sorted == null || sorted.isEmpty() -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (sorted != null) {
                            Text(
                                text = stringResource(R.string.community_empty),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            // Room for the floating search bar (48dp + margins).
                            top = 84.dp,
                            bottom = 16.dp + LocalPreviewBottomInset.current
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
                    query = query,
                    onQueryChange = { query = it },
                    onFilterClick = ::showSortDialog,
                    hazeState = hazeState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

// Developer-mode helper for PR review: paste a theme JSON, view it exactly
// like a published theme (scoped preview + apply).
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TestThemeDialog(
    onDismiss: () -> Unit,
    onLoaded: (CommunityTheme) -> Unit
) {
    var input by remember { mutableStateOf("") }
    var invalid by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.test_theme),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        invalid = false
                    },
                    placeholder = { Text(text = stringResource(R.string.test_theme_hint)) },
                    isError = invalid,
                    supportingText = if (invalid) {
                        { Text(text = stringResource(R.string.invalid_theme_json)) }
                    } else {
                        null
                    },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                    Button(
                        onClick = {
                            val theme = parseTestTheme(input)
                            if (theme == null) {
                                invalid = true
                            } else {
                                onLoaded(theme)
                            }
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}

// Accepts a full theme file or a bare upload payload (id injected).
private fun parseTestTheme(input: String): CommunityTheme? = try {
    val json = JSONObject(input)
    if (!json.has("id")) json.put("id", TestThemeHolder.TEST_THEME_ID)
    CommunityThemeCodec.parseTheme(json)
} catch (_: Exception) {
    null
}

// Share current selections as a community theme: name/description dialog ->
// Turnstile -> worker opens a moderated PR.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShareThemeDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var showChallenge by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }

    if (showChallenge) {
        TurnstileChallenge(
            onToken = { token ->
                showChallenge = false
                submitting = true
                scope.launch {
                    val seed = getSeedColorValue(
                        getWallpaperColorList().firstOrNull() ?: AndroidColor.BLUE
                    )
                    val payload = CommunityThemeCodec.currentSettingsToUploadJson(
                        name = name,
                        description = description,
                        author = author,
                        seedColor = seed
                    )
                    val prUrl = CommunityUploader.upload(payload, token)
                    submitting = false
                    Toast.makeText(
                        context,
                        if (prUrl != null) R.string.theme_submitted else R.string.theme_submit_failed,
                        Toast.LENGTH_LONG
                    ).show()
                    if (prUrl != null) onDismiss()
                }
            },
            onDismiss = { showChallenge = false }
        )
    }

    Dialog(onDismissRequest = { if (!submitting) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.share_theme),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text(text = stringResource(R.string.theme_name)) },
                    singleLine = true,
                    enabled = !submitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it.take(500) },
                    label = { Text(text = stringResource(R.string.theme_description)) },
                    minLines = 2,
                    maxLines = 6,
                    enabled = !submitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it.take(40) },
                    label = { Text(text = stringResource(R.string.author_optional)) },
                    singleLine = true,
                    enabled = !submitting,
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
                        enabled = !submitting && name.isNotBlank(),
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (submitting) R.string.submitting_theme else android.R.string.ok
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CommunityScreenPreview() {
    ColorBlendrTheme {
        CommunityScreen()
    }
}
