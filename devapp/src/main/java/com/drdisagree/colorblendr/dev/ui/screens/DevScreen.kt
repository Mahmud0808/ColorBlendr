package com.drdisagree.colorblendr.dev.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.models.BlockedEntry
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.ui.components.BlockReasonDialog
import com.drdisagree.colorblendr.dev.ui.components.BlockedCard
import com.drdisagree.colorblendr.dev.ui.components.CompactSearchField
import com.drdisagree.colorblendr.dev.ui.components.ConfirmDialog
import com.drdisagree.colorblendr.dev.ui.components.EmptyState
import com.drdisagree.colorblendr.dev.ui.components.KeyGate
import com.drdisagree.colorblendr.dev.ui.components.PendingCard
import com.drdisagree.colorblendr.dev.ui.components.SegmentedTabs
import com.drdisagree.colorblendr.dev.ui.components.StackedSnackbarHost
import com.drdisagree.colorblendr.dev.ui.navigation.Routes
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme
import com.drdisagree.colorblendr.dev.ui.viewmodels.DevViewModel

@Composable
fun DevScreen(openPendingTick: Int = 0) {
    if (LocalInspectionMode.current) {
        HomeContent(
            authorized = false,
            loading = false,
            busy = false,
            pending = null,
            blocked = null,
            initialKey = "",
            openPendingTick = 0,
            onUnlock = {},
            onRefresh = {},
            onPreview = {},
            onApprove = {},
            onReject = {},
            onBlock = { _, _ -> },
            onApproveAll = {},
            onRejectAll = {},
            onBlockAll = { _, _ -> },
            onUnblock = {},
            onOpenSettings = {},
            onOpenDetail = {}
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

    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        if (devViewModel.savedKey.isNotEmpty()) devViewModel.refresh()
    }

    LaunchedEffect(openPendingTick) {
        if (openPendingTick > 0) {
            navController.popBackStack(Routes.HOME, inclusive = false)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            enterTransition = { slideInHorizontally { it } },
            exitTransition = { slideOutHorizontally { -it } },
            popEnterTransition = { slideInHorizontally { -it } },
            popExitTransition = { slideOutHorizontally { it } }
        ) {
            composable(Routes.HOME) {
                HomeContent(
                    authorized = authorized,
                    loading = loading,
                    busy = busy,
                    pending = pending,
                    blocked = blocked,
                    initialKey = devViewModel.savedKey,
                    openPendingTick = openPendingTick,
                    onUnlock = devViewModel::refresh,
                    onRefresh = devViewModel::refresh,
                    onPreview = devViewModel::openPreview,
                    onApprove = devViewModel::approve,
                    onReject = devViewModel::reject,
                    onBlock = devViewModel::block,
                    onApproveAll = devViewModel::approveAll,
                    onRejectAll = devViewModel::rejectAll,
                    onBlockAll = devViewModel::blockAll,
                    onUnblock = devViewModel::unblock,
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenDetail = { navController.navigate(Routes.detail(it.id)) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        devViewModel.logout()
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.DETAIL) { entry ->
                val id = entry.arguments?.getString(Routes.ARG_SUBMISSION_ID)
                val item = pending?.find { it.id == id }
                if (item == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    SubmissionDetailScreen(
                        item = item,
                        onBack = { navController.popBackStack() },
                        onPreview = { devViewModel.openPreview(item) },
                        onApprove = {
                            navController.popBackStack()
                            devViewModel.approve(item)
                        },
                        onReject = {
                            navController.popBackStack()
                            devViewModel.reject(item)
                        },
                        onBlock = { reason ->
                            navController.popBackStack()
                            devViewModel.block(item, reason)
                        }
                    )
                }
            }
        }

        StackedSnackbarHost(
            messages = messages,
            onDismiss = devViewModel::dismissMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    authorized: Boolean,
    loading: Boolean,
    busy: Boolean,
    pending: List<PendingSubmission>?,
    blocked: List<BlockedEntry>?,
    initialKey: String,
    openPendingTick: Int,
    onUnlock: (String) -> Unit,
    onRefresh: () -> Unit,
    onPreview: (PendingSubmission) -> Unit,
    onApprove: (PendingSubmission) -> Unit,
    onReject: (PendingSubmission) -> Unit,
    onBlock: (PendingSubmission, String) -> Unit,
    onApproveAll: (List<PendingSubmission>) -> Unit,
    onRejectAll: (List<PendingSubmission>) -> Unit,
    onBlockAll: (List<PendingSubmission>, String) -> Unit,
    onUnblock: (BlockedEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenDetail: (PendingSubmission) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val pendingListState = rememberLazyListState()
    val blockedListState = rememberLazyListState()

    var adminKey by rememberSaveable { mutableStateOf(initialKey) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var newestFirst by rememberSaveable { mutableStateOf(true) }
    var blockTarget by remember { mutableStateOf<PendingSubmission?>(null) }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var bulkBlock by remember { mutableStateOf(false) }
    var confirmApprove by remember { mutableStateOf<PendingSubmission?>(null) }
    var confirmReject by remember { mutableStateOf<PendingSubmission?>(null) }
    var confirmApproveAll by remember { mutableStateOf<List<PendingSubmission>?>(null) }
    var confirmRejectAll by remember { mutableStateOf<List<PendingSubmission>?>(null) }

    val selectedItems = pending.orEmpty().filter { it.id in selectedIds }
    val selectionMode = selectedIds.isNotEmpty()

    val lifted by remember {
        derivedStateOf {
            val state = if (tab == 0) pendingListState else blockedListState
            state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 0
        }
    }
    val toolbarColor by animateColorAsState(
        targetValue = if (lifted) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "toolbarLift"
    )
    val toolbarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = toolbarColor,
        scrolledContainerColor = toolbarColor
    )

    LaunchedEffect(openPendingTick) {
        if (openPendingTick > 0) {
            tab = 0
            selectedIds = emptySet()
        }
    }

    LaunchedEffect(tab) { selectedIds = emptySet() }

    BackHandler(enabled = selectionMode) { selectedIds = emptySet() }

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

    if (bulkBlock && selectedItems.isNotEmpty()) {
        BlockReasonDialog(
            target = selectedItems.first(),
            onDismiss = { bulkBlock = false },
            onConfirm = { reason ->
                val targets = selectedItems
                bulkBlock = false
                selectedIds = emptySet()
                onBlockAll(targets, reason)
            }
        )
    }

    confirmApprove?.let { target ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_approve_title),
            message = stringResource(R.string.confirm_approve_message),
            confirmLabel = stringResource(R.string.approve),
            destructive = false,
            onConfirm = {
                confirmApprove = null
                onApprove(target)
            },
            onDismiss = { confirmApprove = null }
        )
    }

    confirmReject?.let { target ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_reject_title),
            message = stringResource(R.string.confirm_reject_message),
            confirmLabel = stringResource(R.string.reject),
            destructive = true,
            onConfirm = {
                confirmReject = null
                onReject(target)
            },
            onDismiss = { confirmReject = null }
        )
    }

    confirmApproveAll?.let { targets ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_approve_all_title, targets.size),
            message = stringResource(R.string.confirm_approve_message),
            confirmLabel = stringResource(R.string.approve),
            destructive = false,
            onConfirm = {
                confirmApproveAll = null
                selectedIds = emptySet()
                onApproveAll(targets)
            },
            onDismiss = { confirmApproveAll = null }
        )
    }

    confirmRejectAll?.let { targets ->
        ConfirmDialog(
            title = stringResource(R.string.confirm_reject_all_title, targets.size),
            message = stringResource(R.string.confirm_reject_message),
            confirmLabel = stringResource(R.string.reject),
            destructive = true,
            onConfirm = {
                confirmRejectAll = null
                selectedIds = emptySet()
                onRejectAll(targets)
            },
            onDismiss = { confirmRejectAll = null }
        )
    }

    Scaffold(
        topBar = {
            if (authorized) {
                if (selectionMode) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.selected_count, selectedItems.size)
                            )
                        },
                        colors = toolbarColors,
                        navigationIcon = {
                            IconButton(
                                onClick = { selectedIds = emptySet() },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.Close),
                                    contentDescription = stringResource(R.string.clear_selection)
                                )
                            }
                        },
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                FilledIconButton(
                                    onClick = { confirmApproveAll = selectedItems },
                                    enabled = !busy,
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        painter = rememberVectorPainter(Icons.Rounded.Check),
                                        contentDescription = stringResource(R.string.approve)
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = { confirmRejectAll = selectedItems },
                                    enabled = !busy,
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        painter = rememberVectorPainter(Icons.Rounded.Close),
                                        contentDescription = stringResource(R.string.reject)
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = { bulkBlock = true },
                                    enabled = !busy,
                                    shapes = IconButtonDefaults.shapes(),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Icon(
                                        painter = rememberVectorPainter(Icons.Rounded.Block),
                                        contentDescription = stringResource(R.string.block)
                                    )
                                }
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.app_name)) },
                        colors = toolbarColors,
                        actions = {
                            IconButton(
                                onClick = {
                                    searchVisible = !searchVisible
                                    if (!searchVisible) query = ""
                                },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(
                                        if (searchVisible) Icons.Rounded.SearchOff
                                        else Icons.Rounded.Search
                                    ),
                                    contentDescription = stringResource(
                                        if (searchVisible) R.string.hide_search
                                        else R.string.search_action
                                    )
                                )
                            }
                            IconButton(
                                onClick = onOpenSettings,
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Rounded.Settings),
                                    contentDescription = stringResource(R.string.settings)
                                )
                            }
                        }
                    )
                }
            }
        }
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

        val listContentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = innerPadding.calculateBottomPadding() + 14.dp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
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
                badges = listOf(pending?.size ?: 0, blocked?.size ?: 0),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
            )

            AnimatedVisibility(visible = searchVisible) {
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
            }

            PullToRefreshBox(
                isRefreshing = loading,
                onRefresh = {
                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onRefresh()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedContent(
                    targetState = Pair(tab, query.trim()),
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "devTabs"
                ) { (currentTab, trimmedQuery) ->
                    if (currentTab == 0) {
                        val filtered = pending.orEmpty().filter {
                            trimmedQuery.isEmpty() ||
                                    it.name.contains(trimmedQuery, ignoreCase = true) ||
                                    it.author.contains(trimmedQuery, ignoreCase = true) ||
                                    it.device.startsWith(trimmedQuery, ignoreCase = true)
                        }.let {
                            if (newestFirst) it.sortedByDescending { s -> s.created }
                            else it.sortedBy { s -> s.created }
                        }
                        LazyColumn(
                            state = pendingListState,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = listContentPadding,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (filtered.isEmpty()) {
                                item {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillParentMaxSize()
                                    ) {
                                        EmptyState(
                                            text = stringResource(R.string.no_pending),
                                            icon = Icons.Rounded.Inbox,
                                            subtitle = stringResource(R.string.no_pending_desc)
                                        )
                                    }
                                }
                            } else {
                                items(filtered, key = { it.id }) { item ->
                                    PendingCard(
                                        item = item,
                                        busy = busy,
                                        selectionMode = selectionMode,
                                        selected = item.id in selectedIds,
                                        onClick = {
                                            if (selectionMode) {
                                                selectedIds = selectedIds.toggle(item.id)
                                            } else {
                                                onOpenDetail(item)
                                            }
                                        },
                                        onLongClick = {
                                            haptics.performHapticFeedback(
                                                HapticFeedbackType.LongPress
                                            )
                                            selectedIds = selectedIds.toggle(item.id)
                                        },
                                        onApprove = { confirmApprove = item },
                                        onReject = { confirmReject = item },
                                        onBlock = { blockTarget = item }
                                    )
                                }
                            }
                        }
                    } else {
                        val filtered = blocked.orEmpty().filter {
                            trimmedQuery.isEmpty() ||
                                    it.reason.contains(trimmedQuery, ignoreCase = true) ||
                                    it.device.startsWith(trimmedQuery, ignoreCase = true)
                        }.let {
                            if (newestFirst) it.sortedByDescending { s -> s.created }
                            else it.sortedBy { s -> s.created }
                        }
                        LazyColumn(
                            state = blockedListState,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = listContentPadding,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (filtered.isEmpty()) {
                                item {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillParentMaxSize()
                                    ) {
                                        EmptyState(
                                            text = stringResource(R.string.no_blocked),
                                            icon = Icons.Rounded.Block,
                                            subtitle = stringResource(R.string.no_blocked_desc)
                                        )
                                    }
                                }
                            } else {
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

private fun Set<String>.toggle(id: String): Set<String> =
    if (id in this) this - id else this + id

@Preview
@Composable
private fun DevScreenPreview() {
    DevTheme {
        DevScreen()
    }
}
