package com.drdisagree.colorblendr.dev.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.colorblendr.dev.AdminApi
import com.drdisagree.colorblendr.dev.BlockedEntry
import com.drdisagree.colorblendr.dev.DevPrefs
import com.drdisagree.colorblendr.dev.PendingSubmission
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.ThemeForwarder
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevScreen() {
    val context = LocalContext.current
    val resources = LocalResources.current
    val inspection = LocalInspectionMode.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var adminKey by rememberSaveable {
        mutableStateOf(if (inspection) "" else DevPrefs.adminKey(context))
    }
    var authorized by rememberSaveable { mutableStateOf(false) }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var pending by remember { mutableStateOf<List<PendingSubmission>?>(null) }
    var blocked by remember { mutableStateOf<List<BlockedEntry>?>(null) }
    var loading by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var blockTarget by remember { mutableStateOf<PendingSubmission?>(null) }

    fun message(resId: Int) {
        scope.launch { snackbarHostState.showSnackbar(resources.getString(resId)) }
    }

    fun refresh() {
        loading = true
        scope.launch {
            val key = adminKey.trim()
            val fetchedPending = AdminApi.fetchPending(key)
            val fetchedBlocked = AdminApi.fetchBlocked(key)
            loading = false
            if (fetchedPending == null || fetchedBlocked == null) {
                if (pending == null && blocked == null) authorized = false
                message(R.string.request_failed)
            } else {
                authorized = true
                adminKey = key
                DevPrefs.setAdminKey(context, key)
                pending = fetchedPending
                blocked = fetchedBlocked
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!inspection && adminKey.isNotEmpty()) refresh()
    }

    Scaffold(
        topBar = {
            if (authorized) {
                LargeTopAppBar(
                    title = { Text(text = stringResource(R.string.app_name)) },
                    actions = {
                        IconButton(
                            onClick = ::refresh,
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
                                DevPrefs.clearAdminKey(context)
                                adminKey = ""
                                authorized = false
                                pending = null
                                blocked = null
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (!authorized) {
            KeyGate(
                adminKey = adminKey,
                onKeyChange = { adminKey = it },
                loading = loading,
                onUnlock = ::refresh,
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            ) {
                ToggleButton(
                    checked = tab == 0,
                    onCheckedChange = {
                        if (it && tab != 0) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            tab = 0
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.pending), fontSize = 13.sp)
                }
                ToggleButton(
                    checked = tab == 1,
                    onCheckedChange = {
                        if (it && tab != 1) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            tab = 1
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.blocked), fontSize = 13.sp)
                }
            }

            OutlinedTextField(
                shape = RoundedCornerShape(999.dp),
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(text = stringResource(R.string.search_hint)) },
                leadingIcon = {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Search),
                        contentDescription = null
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            )

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
                                            val opened = ThemeForwarder.openPreview(
                                                context, item.payloadJson
                                            )
                                            if (!opened) {
                                                message(R.string.colorblendr_not_installed)
                                            }
                                        },
                                        onApprove = {
                                            busy = true
                                            scope.launch {
                                                val prUrl = AdminApi.approve(adminKey, item.id)
                                                busy = false
                                                if (prUrl != null) {
                                                    pending = pending?.filterNot {
                                                        it.id == item.id
                                                    }
                                                    message(R.string.theme_approved)
                                                } else {
                                                    message(R.string.request_failed)
                                                }
                                            }
                                        },
                                        onReject = {
                                            busy = true
                                            scope.launch {
                                                val rejected = AdminApi.reject(adminKey, item.id)
                                                busy = false
                                                if (rejected) {
                                                    pending = pending?.filterNot {
                                                        it.id == item.id
                                                    }
                                                    message(R.string.theme_rejected)
                                                } else {
                                                    message(R.string.request_failed)
                                                }
                                            }
                                        },
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
                                        onUnblock = {
                                            busy = true
                                            scope.launch {
                                                val result = AdminApi.unblock(adminKey, item.device)
                                                busy = false
                                                if (result) {
                                                    blocked = blocked?.filterNot {
                                                        it.device == item.device
                                                    }
                                                    message(R.string.uploader_unblocked)
                                                } else {
                                                    message(R.string.request_failed)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    blockTarget?.let { target ->
        BlockReasonDialog(
            target = target,
            onDismiss = { blockTarget = null },
            onConfirm = { reason ->
                blockTarget = null
                busy = true
                scope.launch {
                    val result = AdminApi.block(adminKey, target.device, reason)
                    busy = false
                    if (result) {
                        pending = pending?.filterNot { it.device == target.device }
                        blocked = blocked?.plus(
                            BlockedEntry(
                                device = target.device,
                                reason = reason,
                                created = System.currentTimeMillis()
                            )
                        )
                        message(R.string.uploader_blocked)
                    } else {
                        message(R.string.request_failed)
                    }
                }
            }
        )
    }
}

@Composable
private fun BlockReasonDialog(
    target: PendingSubmission,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val anonymous = stringResource(R.string.anonymous)
    var reason by rememberSaveable(target.device) {
        mutableStateOf("${target.name} by ${target.author.ifEmpty { anonymous }}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Block),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(text = stringResource(R.string.block_uploader)) },
        text = {
            OutlinedTextField(
                shape = RoundedCornerShape(16.dp),
                value = reason,
                onValueChange = { reason = it },
                label = { Text(text = stringResource(R.string.block_reason)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason.trim()) },
                enabled = reason.isNotBlank(),
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(text = stringResource(R.string.block))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun KeyGate(
    adminKey: String,
    onKeyChange: (String) -> Unit,
    loading: Boolean,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Palette),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp)
            )
        }
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = stringResource(R.string.moderation_console),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        OutlinedTextField(
            shape = RoundedCornerShape(20.dp),
            value = adminKey,
            onValueChange = onKeyChange,
            label = { Text(text = stringResource(R.string.admin_key)) },
            singleLine = true,
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        )
        Button(
            onClick = onUnlock,
            enabled = !loading && adminKey.isNotBlank(),
            shapes = ButtonDefaults.shapes(),
            contentPadding = ButtonDefaults.contentPaddingFor(56.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(56.dp)
        ) {
            Text(text = stringResource(R.string.unlock))
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun EmptyState(text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Inbox),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun PendingCard(
    item: PendingSubmission,
    busy: Boolean,
    onPreview: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onBlock: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        label = "pendingCardScale"
    )

    Surface(
        onClick = onPreview,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
    ) {
        Column(
            modifier = Modifier.padding(
                start = 18.dp, end = 12.dp, top = 16.dp, bottom = 10.dp
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp),
                    modifier = Modifier.width(62.dp)
                ) {
                    listOfNotNull(
                        item.seedColor, item.secondaryColor, item.tertiaryColor
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .border(
                                    2.dp,
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    CircleShape
                                )
                                .padding(2.dp)
                                .background(Color(color), CircleShape)
                        )
                    }
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.author.ifEmpty { stringResource(R.string.anonymous) }} · " +
                                DateFormat.getDateInstance(DateFormat.MEDIUM)
                                    .format(Date(item.created)) +
                                " · ${item.device.take(12)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                FilledTonalIconButton(
                    onClick = onApprove,
                    enabled = !busy,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Check),
                        contentDescription = stringResource(R.string.approve)
                    )
                }
                FilledTonalIconButton(
                    onClick = onReject,
                    enabled = !busy,
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Close),
                        contentDescription = stringResource(R.string.reject)
                    )
                }
                FilledTonalIconButton(
                    onClick = onBlock,
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
    }
}

@Composable
private fun BlockedCard(
    item: BlockedEntry,
    busy: Boolean,
    onUnblock: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 18.dp, end = 10.dp, top = 12.dp, bottom = 12.dp
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.Block),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = item.reason.ifEmpty { item.device.take(12) },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.device.take(12)} · " +
                            DateFormat.getDateInstance(DateFormat.MEDIUM)
                                .format(Date(item.created)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            FilledTonalIconButton(
                onClick = onUnblock,
                enabled = !busy,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.LockOpen),
                    contentDescription = stringResource(R.string.unblock)
                )
            }
        }
    }
}

@Preview
@Composable
private fun DevScreenPreview() {
    DevScreen()
}

@Preview
@Composable
private fun KeyGatePreview() {
    KeyGate(
        adminKey = "",
        onKeyChange = {},
        loading = false,
        onUnlock = {}
    )
}

@Preview
@Composable
private fun EmptyStatePreview() {
    EmptyState(text = "Queue is empty")
}

@Preview
@Composable
private fun PendingCardPreview() {
    PendingCard(
        item = PendingSubmission(
            id = "ocean-breeze-abc123",
            name = "Ocean Breeze",
            author = "DrDisagree",
            device = "a1b2c3d4e5f6",
            created = 1752800000000L,
            seedColor = 0xFF51BDFF.toInt(),
            secondaryColor = 0xFF7C4DFF.toInt(),
            tertiaryColor = 0xFF26A69A.toInt(),
            payloadJson = "{}"
        ),
        busy = false,
        onPreview = {},
        onApprove = {},
        onReject = {},
        onBlock = {}
    )
}

@Preview
@Composable
private fun BlockedCardPreview() {
    BlockedCard(
        item = BlockedEntry(
            device = "a1b2c3d4e5f6",
            reason = "Ocean Breeze by DrDisagree",
            created = 1752800000000L
        ),
        busy = false,
        onUnblock = {}
    )
}