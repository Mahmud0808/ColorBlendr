package com.drdisagree.colorblendr.dev.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.models.BlockedEntry
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.data.models.StackedMessage
import com.drdisagree.colorblendr.dev.ui.components.BlockReasonDialog
import com.drdisagree.colorblendr.dev.ui.components.BlockedCard
import com.drdisagree.colorblendr.dev.ui.components.CompactSearchField
import com.drdisagree.colorblendr.dev.ui.components.EmptyState
import com.drdisagree.colorblendr.dev.ui.components.KeyGate
import com.drdisagree.colorblendr.dev.ui.components.PendingCard
import com.drdisagree.colorblendr.dev.ui.components.SegmentedTabs
import com.drdisagree.colorblendr.dev.ui.components.StackedSnackbarHost
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme
import com.drdisagree.colorblendr.dev.ui.viewmodels.DevViewModel

@Composable
fun DevScreen() {
    if (LocalInspectionMode.current) {
        DevScreenContent(
            authorized = false,
            loading = false,
            busy = false,
            pending = null,
            blocked = null,
            messages = emptyList(),
            initialKey = "",
            onUnlock = {},
            onRefresh = {},
            onLogout = {},
            onPreview = {},
            onApprove = {},
            onReject = {},
            onBlock = { _, _ -> },
            onUnblock = {},
            onDismissMessage = {}
        )
        return
    }

    val devViewModel: DevViewModel = viewModel()
    val authorized by devViewModel.authorized.collectAsState()
    val loading by devViewModel.loading.collectAsState()
    val busy by devViewModel.busy.collectAsState()
    val pending by devViewModel.pending.collectAsState()
    val blocked by devViewModel.blocked.collectAsState()
    val messages by devViewModel.messages.collectAsState()

    LaunchedEffect(Unit) {
        if (devViewModel.savedKey.isNotEmpty()) devViewModel.refresh()
    }

    DevScreenContent(
        authorized = authorized,
        loading = loading,
        busy = busy,
        pending = pending,
        blocked = blocked,
        messages = messages,
        initialKey = devViewModel.savedKey,
        onUnlock = devViewModel::refresh,
        onRefresh = devViewModel::refresh,
        onLogout = devViewModel::logout,
        onPreview = devViewModel::openPreview,
        onApprove = devViewModel::approve,
        onReject = devViewModel::reject,
        onBlock = devViewModel::block,
        onUnblock = devViewModel::unblock,
        onDismissMessage = devViewModel::dismissMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DevScreenContent(
    authorized: Boolean,
    loading: Boolean,
    busy: Boolean,
    pending: List<PendingSubmission>?,
    blocked: List<BlockedEntry>?,
    messages: List<StackedMessage>,
    initialKey: String,
    onUnlock: (String) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onPreview: (PendingSubmission) -> Unit,
    onApprove: (PendingSubmission) -> Unit,
    onReject: (PendingSubmission) -> Unit,
    onBlock: (PendingSubmission, String) -> Unit,
    onUnblock: (BlockedEntry) -> Unit,
    onDismissMessage: (Long) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var adminKey by rememberSaveable { mutableStateOf(initialKey) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var newestFirst by rememberSaveable { mutableStateOf(true) }
    var blockTarget by remember { mutableStateOf<PendingSubmission?>(null) }

    blockTarget?.let { target ->
        BlockReasonDialog(
            target = target,
            onDismiss = { blockTarget = null },
            onConfirm = { reason ->
                blockTarget = null
                onBlock(target, reason)
            }
        )
    }

    Scaffold(
        topBar = {
            if (authorized) {
                TopAppBar(
                    title = { Text(text = stringResource(R.string.app_name)) },
                    actions = {
                        IconButton(
                            onClick = onRefresh,
                            enabled = !loading,
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Rounded.Refresh),
                                contentDescription = stringResource(R.string.refresh)
                            )
                        }
                        IconButton(
                            onClick = {
                                adminKey = ""
                                onLogout()
                            },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.Logout),
                                contentDescription = stringResource(R.string.logout)
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        },
        snackbarHost = {
            StackedSnackbarHost(
                messages = messages,
                onDismiss = onDismissMessage
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (!authorized) {
            KeyGate(
                adminKey = adminKey,
                onKeyChange = { adminKey = it },
                loading = loading,
                onUnlock = { onUnlock(adminKey) },
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SegmentedTabs(
                options = listOf(
                    stringResource(R.string.pending),
                    stringResource(R.string.blocked)
                ),
                selected = tab,
                onSelect = {
                    if (it != tab) {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        tab = it
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            ) {
                CompactSearchField(
                    query = query,
                    onQueryChange = { query = it },
                    modifier = Modifier.weight(1f)
                )
                Box {
                    var showSortMenu by remember { mutableStateOf(false) }
                    FilledTonalIconButton(
                        onClick = { showSortMenu = true },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.SwapVert),
                            contentDescription = stringResource(R.string.sort_by),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.sort_newest)) },
                            leadingIcon = {
                                Icon(
                                    painter = rememberVectorPainter(
                                        if (newestFirst) Icons.Rounded.Check
                                        else Icons.Rounded.ArrowDownward
                                    ),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showSortMenu = false
                                newestFirst = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.sort_oldest)) },
                            leadingIcon = {
                                Icon(
                                    painter = rememberVectorPainter(
                                        if (!newestFirst) Icons.Rounded.Check
                                        else Icons.Rounded.ArrowUpward
                                    ),
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showSortMenu = false
                                newestFirst = false
                            }
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = Triple(tab, loading && pending == null, query.trim()),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "devTabs"
            ) { (currentTab, initialLoad, trimmedQuery) ->
                when {
                    initialLoad -> Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LoadingIndicator()
                    }

                    currentTab == 0 -> {
                        val filtered = pending.orEmpty().filter {
                            trimmedQuery.isEmpty() ||
                                    it.name.contains(trimmedQuery, ignoreCase = true) ||
                                    it.author.contains(trimmedQuery, ignoreCase = true) ||
                                    it.device.startsWith(trimmedQuery, ignoreCase = true)
                        }.let {
                            if (newestFirst) it.sortedByDescending { s -> s.created }
                            else it.sortedBy { s -> s.created }
                        }
                        if (filtered.isEmpty()) {
                            EmptyState(text = stringResource(R.string.no_pending))
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                items(filtered, key = { it.id }) { item ->
                                    PendingCard(
                                        item = item,
                                        busy = busy,
                                        onPreview = {
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.ContextClick
                                            )
                                            onPreview(item)
                                        },
                                        onApprove = { onApprove(item) },
                                        onReject = { onReject(item) },
                                        onBlock = { blockTarget = item }
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        val filtered = blocked.orEmpty().filter {
                            trimmedQuery.isEmpty() ||
                                    it.reason.contains(trimmedQuery, ignoreCase = true) ||
                                    it.device.startsWith(trimmedQuery, ignoreCase = true)
                        }.let {
                            if (newestFirst) it.sortedByDescending { s -> s.created }
                            else it.sortedBy { s -> s.created }
                        }
                        if (filtered.isEmpty()) {
                            EmptyState(text = stringResource(R.string.no_blocked))
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                items(filtered, key = { it.device }) { item ->
                                    BlockedCard(
                                        item = item,
                                        busy = busy,
                                        onUnblock = { onUnblock(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DevScreenPreview() {
    DevTheme {
        DevScreen()
    }
}