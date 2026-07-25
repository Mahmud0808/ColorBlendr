package com.drdisagree.colorblendr.ui.compose.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyOff
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ConfirmDialog
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.ui.compose.components.ToolbarIconPill
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.AiThemeViewModel
import com.drdisagree.colorblendr.utils.ai.AiThemeHolder
import com.drdisagree.colorblendr.utils.ai.GeminiError
import com.drdisagree.colorblendr.utils.ai.KeyCheckResult
import com.drdisagree.colorblendr.utils.community.CommunityThemeApplier
import kotlinx.coroutines.launch

@Composable
fun AiThemeBuilderScreen() {
    if (LocalInspectionMode.current) {
        AiKeySetupContent(
            verifying = false,
            keyError = null,
            onSaveKey = {},
            onClearKeyError = {}
        )
        return
    }

    val aiViewModel: AiThemeViewModel = viewModel()
    val savedKey by aiViewModel.apiKey.collectAsState()
    val verifying by aiViewModel.verifying.collectAsState()
    val keyError by aiViewModel.keyError.collectAsState()
    val generating by aiViewModel.generating.collectAsState()
    val generateError by aiViewModel.generateError.collectAsState()

    if (savedKey.isEmpty()) {
        AiKeySetupContent(
            verifying = verifying,
            keyError = keyError,
            onSaveKey = aiViewModel::saveKey,
            onClearKeyError = aiViewModel::clearKeyError
        )
    } else {
        AiBuilderContent(
            generating = generating,
            error = generateError,
            onGenerate = aiViewModel::generate,
            onKeyReset = aiViewModel::resetKey
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AiKeySetupContent(
    verifying: Boolean,
    keyError: KeyCheckResult?,
    onSaveKey: (String) -> Unit,
    onClearKeyError: () -> Unit
) {
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }

    var apiKey by rememberSaveable { mutableStateOf("") }
    var revealed by rememberSaveable { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.ai_theme_builder_title),
                showBackButton = true,
                lifted = toolbarLifted
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .contentWidthLimit()
                    .verticalScroll(scrollState)
                    .padding(bottom = LocalPreviewBottomInset.current)
                    .padding(
                        horizontal = dimensionResource(R.dimen.container_margin_horizontal)
                    )
                    .padding(top = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
                    color = MaterialTheme.colorScheme.surface,
                    border = AppCardDefaults.outlinedBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(22.dp)) {
                        Text(
                            text = stringResource(R.string.ai_setup_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.ai_setup_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        val linkColor = MaterialTheme.colorScheme.primary
                        val step1Template = stringResource(R.string.ai_setup_step_1)
                        val step1 = remember(step1Template, linkColor) {
                            buildAnnotatedString {
                                val parts = step1Template.split("%1\$s")
                                append(parts.getOrElse(0) { "" })
                                withLink(
                                    LinkAnnotation.Url("https://aistudio.google.com/apikey")
                                ) {
                                    withStyle(
                                        SpanStyle(
                                            color = linkColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    ) {
                                        append("aistudio.google.com/apikey")
                                    }
                                }
                                append(parts.getOrElse(1) { "" })
                            }
                        }
                        SetupStep(number = 1, text = step1)
                        listOf(
                            R.string.ai_setup_step_2,
                            R.string.ai_setup_step_3,
                            R.string.ai_setup_step_4
                        ).forEachIndexed { index, step ->
                            SetupStep(
                                number = index + 2,
                                text = AnnotatedString(stringResource(step))
                            )
                        }
                    }
                }

                OutlinedTextField(
                    shape = RoundedCornerShape(20.dp),
                    value = apiKey,
                    onValueChange = {
                        apiKey = it.filterNot(Char::isISOControl)
                        onClearKeyError()
                    },
                    label = { Text(text = stringResource(R.string.ai_api_key_hint)) },
                    singleLine = true,
                    enabled = !verifying,
                    isError = keyError != null,
                    supportingText = keyError?.let { result ->
                        {
                            Text(
                                text = stringResource(
                                    if (result == KeyCheckResult.INVALID) {
                                        R.string.ai_key_invalid
                                    } else {
                                        R.string.ai_key_network
                                    }
                                )
                            )
                        }
                    },
                    visualTransformation = if (revealed) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = { revealed = !revealed }) {
                            Icon(
                                painter = rememberVectorPainter(
                                    if (revealed) Icons.Rounded.VisibilityOff
                                    else Icons.Rounded.Visibility
                                ),
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                )

                Button(
                    onClick = { onSaveKey(apiKey) },
                    enabled = !verifying && apiKey.isNotBlank(),
                    shapes = ButtonDefaults.shapes(),
                    contentPadding = ButtonDefaults.contentPaddingFor(52.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(52.dp)
                ) {
                    if (verifying) {
                        LoadingIndicator(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        Text(text = stringResource(R.string.ai_save_key))
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupStep(number: Int, text: AnnotatedString) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AiBuilderContent(
    generating: Boolean,
    error: GeminiError?,
    onGenerate: (String) -> Unit,
    onKeyReset: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val toolbarLifted by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val rootMode = remember { isRootMode() }

    var prompt by rememberSaveable { mutableStateOf("") }
    val themes = AiThemeHolder.themes
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var activeThemeId by remember {
        mutableStateOf(AiThemeHolder.stagedThemeIfCurrent()?.id)
    }

    LaunchedEffect(Unit) {
        RefreshCoordinator.refreshEvent.collect {
            activeThemeId = AiThemeHolder.stagedThemeIfCurrent()?.id
        }
    }

    if (showResetDialog) {
        ConfirmDialog(
            title = stringResource(R.string.ai_reset_key),
            message = stringResource(R.string.ai_reset_key_message),
            confirmText = stringResource(R.string.ai_reset_key_confirm),
            onConfirm = {
                showResetDialog = false
                onKeyReset()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.ai_theme_builder_title),
                showBackButton = true,
                lifted = toolbarLifted,
                actions = {
                    ToolbarIconPill(
                        icon = rememberVectorPainter(Icons.Rounded.KeyOff),
                        shape = CircleShape,
                        width = 40.dp,
                        onClick = { showResetDialog = true }
                    )
                }
            )
            val horizontalPadding =
                dimensionResource(R.dimen.container_margin_horizontal)

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    top = 12.dp,
                    bottom = LocalPreviewBottomInset.current + 16.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .contentWidthLimit()
            ) {
                item(key = "prompt") {
                    Column(
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    ) {
                        OutlinedTextField(
                            shape = RoundedCornerShape(20.dp),
                            value = prompt,
                            onValueChange = { prompt = it },
                            label = {
                                Text(text = stringResource(R.string.ai_prompt_hint))
                            },
                            minLines = 4,
                            maxLines = 8,
                            enabled = !generating,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                onGenerate(prompt)
                            },
                            enabled = !generating && prompt.isNotBlank(),
                            shapes = ButtonDefaults.shapes(),
                            contentPadding = ButtonDefaults.contentPaddingFor(52.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .height(52.dp)
                        ) {
                            if (generating) {
                                LoadingIndicator(
                                    color = MaterialTheme.colorScheme.onSurface
                                        .copy(alpha = 0.38f),
                                    modifier = Modifier.size(30.dp)
                                )
                            } else {
                                Icon(
                                    painter = rememberVectorPainter(
                                        Icons.Rounded.AutoAwesome
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(text = stringResource(R.string.ai_generate))
                            }
                        }
                    }
                }

                error?.let { failure ->
                    item(key = "error") {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = horizontalPadding)
                                .padding(top = 14.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    when (failure) {
                                        GeminiError.AUTH -> R.string.ai_error_auth
                                        GeminiError.RATE_LIMIT ->
                                            R.string.ai_error_rate_limit

                                        GeminiError.TIMEOUT -> R.string.ai_error_timeout
                                        GeminiError.NETWORK -> R.string.ai_error_network
                                        GeminiError.INVALID_RESPONSE ->
                                            R.string.ai_error_invalid

                                        GeminiError.SERVER -> R.string.ai_error_server
                                    }
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(
                                    horizontal = 18.dp,
                                    vertical = 14.dp
                                )
                            )
                        }
                    }
                }

                items(themes, key = { it.id }) { generated ->
                    GeneratedThemeCard(
                        theme = generated,
                        rootMode = rootMode,
                        active = generated.id == activeThemeId,
                        onApply = {
                            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                            AiThemeHolder.stagedTheme = generated
                            activeThemeId = generated.id
                            CommunityThemeApplier.stageForPreview(generated)
                            scope.launch { PreviewController.updatePreview() }
                        },
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = horizontalPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GeneratedThemeCard(
    theme: CommunityTheme,
    rootMode: Boolean,
    active: Boolean,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
    ) {
        Box {
            if (active) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 14.dp, end = 14.dp)
                        .size(24.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Check),
                        contentDescription = stringResource(R.string.ai_theme_active),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-10).dp)
                ) {
                    listOfNotNull(
                        theme.seedColor, theme.secondaryColor, theme.tertiaryColor
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(3.dp)
                                .background(Color(color), CircleShape)
                        )
                    }
                }
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp)
                )
                if (theme.description.isNotEmpty()) {
                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onApply,
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
            }
        }
    }
}

@Preview
@Composable
private fun AiThemeBuilderScreenPreview() {
    ColorBlendrTheme {
        AiThemeBuilderScreen()
    }
}