package com.drdisagree.colorblendr.ui.compose.screens.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.WORKING_METHOD
import com.drdisagree.colorblendr.data.enums.WorkMethod
import com.drdisagree.colorblendr.ui.compose.components.ErrorDialog
import com.drdisagree.colorblendr.ui.compose.components.SelectableCard
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.compose.utils.LocalWidthClass
import com.drdisagree.colorblendr.ui.compose.utils.WidthClass
import com.drdisagree.colorblendr.utils.app.AppUtil.hasStoragePermission
import com.drdisagree.colorblendr.utils.app.AppUtil.permissionsGranted
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell
import kotlinx.coroutines.launch
import com.google.android.material.R as MaterialR

@Composable
fun OnboardingScreen(
    actionState: OnboardingActionState,
    onError: (String) -> Unit,
    onErrorDismissed: () -> Unit,
    onCheckRootConnection: () -> Unit,
    onCheckShizukuConnection: () -> Unit,
    onCheckAdbConnection: () -> Unit,
    onNavigateToPairing: () -> Unit,
    popActivityBackStack: () -> Boolean,
    onFinishActivity: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { 4 }

    BackHandler {
        if (!popActivityBackStack()) {
            if (pagerState.currentPage == 0) {
                onFinishActivity()
            } else {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.safeDrawingPadding()) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 3,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> OnboardingPage1()
                    1 -> OnboardingPage2()
                    2 -> OnboardingPage3()
                    3 -> OnboardingPage4(onNavigateToPairing = onNavigateToPairing)
                }
            }

            AnimatedVisibility(
                visible = pagerState.currentPage != 0,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300)),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(text = stringResource(R.string.back), maxLines = 1)
                }
            }

            val connecting = actionState is OnboardingActionState.Connecting

            Button(
                onClick = {
                    if (pagerState.currentPage == 3) {
                        if (!permissionsGranted(context) || !hasStoragePermission()) {
                            onError(context.getString(R.string.grant_all_permissions))
                            return@Button
                        }

                        when (WORKING_METHOD) {
                            WorkMethod.NULL ->
                                onError(context.getString(R.string.select_method))

                            WorkMethod.ROOT -> onCheckRootConnection()
                            WorkMethod.SHIZUKU -> onCheckShizukuConnection()
                            WorkMethod.WIRELESS_ADB -> onCheckAdbConnection()
                        }
                        return@Button
                    }

                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                enabled = !connecting,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                AnimatedContent(
                    targetState = when {
                        connecting -> ContinueButtonState.LOADING
                        pagerState.currentPage == 3 -> ContinueButtonState.START
                        else -> ContinueButtonState.CONTINUE
                    },
                    transitionSpec = {
                        (fadeIn(tween(220)) togetherWith fadeOut(tween(90)))
                            .using(SizeTransform(clip = false))
                    },
                    label = "continueButton"
                ) { state ->
                    when (state) {
                        ContinueButtonState.LOADING -> LoadingIndicator(
                            modifier = Modifier.size(24.dp)
                        )

                        ContinueButtonState.START -> Text(
                            text = stringResource(R.string.start),
                            maxLines = 1
                        )

                        ContinueButtonState.CONTINUE -> Text(
                            text = stringResource(R.string.btn_continue),
                            maxLines = 1
                        )
                    }
                }
            }

            PagerIndicator(
                pageCount = pagerState.pageCount,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 44.dp)
            )

            if (actionState is OnboardingActionState.Error) {
                ErrorDialog(
                    message = actionState.message,
                    onDismiss = onErrorDismissed
                )
            }
        }
    }
}

// Expressive dots: active page stretches into a pill.
@Composable
private fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (selected) 24.dp else 8.dp,
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "indicatorWidth"
            )
            val color by animateColorAsState(
                targetValue = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                },
                label = "indicatorColor"
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun OnboardingPageScaffold(
    title: String,
    content: @Composable () -> Unit
) {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                LocalWidthClass.current == WidthClass.Expanded

    if (isLandscape) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = maxWidth
            val height = maxHeight
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(start = width * 0.16f, top = height * 0.03f)
                        .fillMaxWidth(0.34f / 0.84f)
                        .height(height * 0.67f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(top = height * 0.03f, end = width * 0.12f)
                        .fillMaxWidth()
                        .height(height * 0.67f)
                ) {
                    content()
                }
            }
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = maxWidth
            val height = maxHeight
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.height(height * 0.1f))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = width * 0.13f)
                        .fillMaxWidth()
                        .height(height * 0.7f)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage1() {
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                LocalWidthClass.current == WidthClass.Expanded

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.7f)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.app_moto),
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            Image(
                painter = painterResource(R.drawable.ic_onboarding_img),
                contentDescription = stringResource(R.string.onboarding_image),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(0.7f)
                    .padding(horizontal = 32.dp, vertical = 40.dp)
            )
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val height = maxHeight
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.height(height * 0.1f))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.app_moto),
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Image(
                    painter = painterResource(R.drawable.ic_onboarding_img),
                    contentDescription = stringResource(R.string.onboarding_image),
                    modifier = Modifier
                        .height(height * 0.7f)
                        .padding(horizontal = 32.dp, vertical = 40.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage2() {
    val context = LocalContext.current

    fun checkSelfPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun notificationGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        true
    }

    fun mediaGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    var hasNotificationPermission by remember { mutableStateOf(notificationGranted()) }
    var hasMediaPermission by remember { mutableStateOf(mediaGranted()) }
    var hasAllFilesPermission by remember { mutableStateOf(hasStoragePermission()) }

    LifecycleResumeEffect(Unit) {
        hasNotificationPermission = notificationGranted()
        hasMediaPermission = mediaGranted()
        hasAllFilesPermission = hasStoragePermission()
        onPauseOrDispose {}
    }

    val requestNotificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }
    val requestMediaPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasMediaPermission = result.isNotEmpty() && result.all { it.value }
    }
    val requestAllFilesPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasAllFilesPermission = hasStoragePermission()
    }

    OnboardingPageScaffold(title = stringResource(R.string.permissions)) {
        val scrollState = rememberScrollState()

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalFadingEdges(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp)
                .padding(top = 16.dp)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                SelectableCard(
                    title = stringResource(R.string.perm_one_title),
                    description = stringResource(R.string.perm_one_desc),
                    selected = hasNotificationPermission,
                    onSelect = {
                        requestNotificationPermission.launch(
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            SelectableCard(
                title = stringResource(R.string.perm_two_title),
                description = stringResource(R.string.perm_two_desc),
                selected = hasMediaPermission,
                onSelect = {
                    requestMediaPermission.launch(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    )
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SelectableCard(
                title = stringResource(R.string.perm_three_title),
                description = stringResource(R.string.perm_three_desc),
                selected = hasAllFilesPermission,
                onSelect = {
                    requestAllFilesPermission.launch(
                        Intent().apply {
                            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        }
                    )
                }
            )
        }
    }
}

@Suppress("BatteryLife")
@Composable
private fun OnboardingPage3() {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    fun isBatteryOptimizationDisabled(): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .isIgnoringBatteryOptimizations(context.packageName)
    }

    var optimizationDisabled by remember { mutableStateOf(isBatteryOptimizationDisabled()) }

    LifecycleResumeEffect(Unit) {
        optimizationDisabled = isBatteryOptimizationDisabled()
        onPauseOrDispose {}
    }

    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        optimizationDisabled = isBatteryOptimizationDisabled()
    }

    val batteryBackground = if (optimizationDisabled) {
        themeAttrColor(
            if (isDark) {
                MaterialR.attr.colorSurfaceContainerHigh
            } else {
                MaterialR.attr.colorSurfaceContainerHighest
            }
        )
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val batteryForeground = themeAttrColor(MaterialR.attr.colorPrimaryVariant)

    OnboardingPageScaffold(title = stringResource(R.string.optimization)) {
        val scrollState = rememberScrollState()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalFadingEdges(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp)
                .padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .padding(bottom = 24.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_battery_landscape_bg),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    colorFilter = ColorFilter.tint(batteryBackground),
                    modifier = Modifier.fillMaxHeight()
                )
                Image(
                    painter = painterResource(R.drawable.ic_battery_landscape_fg),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight,
                    colorFilter = ColorFilter.tint(batteryForeground),
                    modifier = Modifier.fillMaxHeight()
                )
            }
            Text(
                text = stringResource(R.string.disable_battery_optimization_hint),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 24.dp)
                    .alpha(0.5f)
            )
            SelectableCard(
                title = stringResource(R.string.perm_four_title),
                description = stringResource(R.string.perm_four_desc),
                selected = optimizationDisabled,
                onSelect = {
                    if (!isBatteryOptimizationDisabled()) {
                        batteryOptimizationLauncher.launch(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun OnboardingPage4(onNavigateToPairing: () -> Unit) {
    var selectedMethod by remember { mutableStateOf(WORKING_METHOD) }

    LifecycleResumeEffect(Unit) {
        if (WifiAdbShell.isMyDeviceConnected()) {
            WORKING_METHOD = WorkMethod.WIRELESS_ADB
            selectedMethod = WorkMethod.WIRELESS_ADB
        } else {
            if (WORKING_METHOD == WorkMethod.WIRELESS_ADB) {
                WORKING_METHOD = WorkMethod.NULL
            }
            selectedMethod = WORKING_METHOD
        }
        onPauseOrDispose {}
    }

    OnboardingPageScaffold(title = stringResource(R.string.choose_method)) {
        val scrollState = rememberScrollState()

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalFadingEdges(scrollState)
                .verticalScroll(scrollState)
                .padding(horizontal = 12.dp)
                .padding(top = 16.dp)
        ) {
            SelectableCard(
                title = stringResource(R.string.mode_one_title),
                description = stringResource(R.string.mode_one_desc),
                selected = selectedMethod == WorkMethod.ROOT,
                onSelect = {
                    WORKING_METHOD = WorkMethod.ROOT
                    selectedMethod = WorkMethod.ROOT
                    WifiAdbShell.disconnect()
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SelectableCard(
                title = stringResource(R.string.mode_two_title),
                description = stringResource(R.string.mode_two_desc),
                selected = selectedMethod == WorkMethod.SHIZUKU,
                onSelect = {
                    WORKING_METHOD = WorkMethod.SHIZUKU
                    selectedMethod = WorkMethod.SHIZUKU
                    WifiAdbShell.disconnect()
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
            SelectableCard(
                title = stringResource(R.string.mode_three_title),
                description = stringResource(R.string.mode_three_desc),
                selected = selectedMethod == WorkMethod.WIRELESS_ADB,
                onSelect = {
                    WORKING_METHOD = WorkMethod.WIRELESS_ADB

                    if (!WifiAdbShell.isMyDeviceConnected()) {
                        selectedMethod = WorkMethod.NULL
                        onNavigateToPairing()
                    } else {
                        selectedMethod = WorkMethod.WIRELESS_ADB
                    }
                }
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun OnboardingScreenPreview() {
    ColorBlendrTheme {
        OnboardingScreen(
            actionState = OnboardingActionState.Idle,
            onError = {},
            onErrorDismissed = {},
            onCheckRootConnection = {},
            onCheckShizukuConnection = {},
            onCheckAdbConnection = {},
            onNavigateToPairing = {},
            popActivityBackStack = { false },
            onFinishActivity = {}
        )
    }
}

// Fade edge only while more content scrollable in that direction.
private fun Modifier.verticalFadingEdges(
    scrollState: ScrollState,
    edgeLength: Dp = 16.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val edgePx = edgeLength.toPx()

        if (scrollState.value > 0) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = edgePx
                ),
                size = Size(size.width, edgePx),
                blendMode = BlendMode.DstIn
            )
        }
        if (scrollState.value < scrollState.maxValue) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - edgePx,
                    endY = size.height
                ),
                topLeft = Offset(0f, size.height - edgePx),
                size = Size(size.width, edgePx),
                blendMode = BlendMode.DstIn
            )
        }
    }
