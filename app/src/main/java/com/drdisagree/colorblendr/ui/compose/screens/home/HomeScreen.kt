package com.drdisagree.colorblendr.ui.compose.screens.home

import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Utilities
import com.drdisagree.colorblendr.data.common.Utilities.clearAllOverriddenColors
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuMode
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.service.AutoStartService.Companion.isServiceNotRunning
import com.drdisagree.colorblendr.service.RestartBroadcastReceiver.Companion.scheduleJob
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbarHost
import com.drdisagree.colorblendr.ui.compose.components.LoadingOverlay
import com.drdisagree.colorblendr.ui.compose.components.PreviewActionButtons
import com.drdisagree.colorblendr.ui.compose.components.SnackbarVisibility
import com.drdisagree.colorblendr.ui.compose.components.showSnackbarReplacing
import com.drdisagree.colorblendr.ui.compose.navigation.Routes
import com.drdisagree.colorblendr.ui.compose.navigation.tabGroup
import com.drdisagree.colorblendr.ui.compose.screens.about.AboutScreen
import com.drdisagree.colorblendr.ui.compose.screens.colors.ColorsScreen
import com.drdisagree.colorblendr.ui.compose.screens.palette.ColorPaletteScreen
import com.drdisagree.colorblendr.ui.compose.screens.perapp.PerAppThemeScreen
import com.drdisagree.colorblendr.ui.compose.screens.privacypolicy.PrivacyPolicyScreen
import com.drdisagree.colorblendr.ui.compose.screens.settings.SettingsAdvancedScreen
import com.drdisagree.colorblendr.ui.compose.screens.settings.SettingsScreen
import com.drdisagree.colorblendr.ui.compose.screens.styles.StylesScreen
import com.drdisagree.colorblendr.ui.compose.screens.theme.ThemeScreen
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.viewmodels.ColorPaletteViewModel
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel
import com.drdisagree.colorblendr.utils.app.AppUtil
import com.drdisagree.colorblendr.utils.app.AppUtil.hasStoragePermission
import com.drdisagree.colorblendr.utils.app.AppUtil.openAppSettings
import com.drdisagree.colorblendr.utils.app.AppUtil.permissionsGranted
import com.drdisagree.colorblendr.utils.app.AppUtil.requestStoragePermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    success: Boolean,
    pendingRestoreUri: Uri?,
    onRestoreUriHandled: () -> Unit,
    colorsViewModel: ColorsViewModel,
    stylesViewModel: StylesViewModel,
    colorPaletteViewModel: ColorPaletteViewModel
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val nestedNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val backStackEntry by nestedNavController.currentBackStackEntryAsState()

    val previewColors by PreviewController.previewColors.collectAsStateWithLifecycle()
    val isApplying by PreviewController.isApplying.collectAsStateWithLifecycle()
    var lastGroup by rememberSaveable { mutableIntStateOf(1) }
    val routeGroup = tabGroup(backStackEntry?.destination?.route)
    val currentGroup = if (routeGroup != 0) routeGroup else lastGroup
    SideEffect {
        if (routeGroup != 0) lastGroup = routeGroup
    }

    val permissionMustBeGrantedText = stringResource(R.string.permission_must_be_granted)
    val fileAccessText = stringResource(R.string.file_access_permission_required)
    val grantText = stringResource(R.string.grant)

    var permissionToRetry by remember { mutableStateOf<String?>(null) }

    fun showStoragePermissionSnackbar() {
        scope.launch {
            val result = snackbarHostState.showSnackbarReplacing(
                message = fileAccessText,
                actionLabel = grantText,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                requestStoragePermission(context)
            }
        }
    }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        for ((permission, granted) in result) {
            if (!granted) {
                val permanentlyDenied = activity != null &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                scope.launch {
                    val snackResult = snackbarHostState.showSnackbarReplacing(
                        message = permissionMustBeGrantedText,
                        actionLabel = grantText,
                        duration = SnackbarDuration.Indefinite
                    )
                    if (snackResult == SnackbarResult.ActionPerformed) {
                        if (!permanentlyDenied) {
                            permissionToRetry = permission
                        } else {
                            openAppSettings(context)
                        }
                    }
                }
                return@rememberLauncherForActivityResult
            }
        }

        if (!hasStoragePermission()) {
            showStoragePermissionSnackbar()
            return@rememberLauncherForActivityResult
        }

        if (isServiceNotRunning && success) {
            scheduleJob(context)
        }
    }

    LaunchedEffect(permissionToRetry) {
        permissionToRetry?.let {
            permissionToRetry = null
            requestPermissionsLauncher.launch(arrayOf(it))
        }
    }

    LaunchedEffect(Unit) {
        delay(2000)
        try {
            if (permissionsGranted(context)) {
                if (isServiceNotRunning && success) {
                    scheduleJob(context)
                }
            } else {
                requestPermissionsLauncher.launch(AppUtil.REQUIRED_PERMISSIONS)
            }
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(pendingRestoreUri) {
        pendingRestoreUri?.let { uri ->
            nestedNavController.navigate(
                "${Routes.SETTINGS_BASE}?restoreUri=${Uri.encode(uri.toString())}"
            ) {
                popUpTo(nestedNavController.graph.findStartDestination().id)
                launchSingleTop = true
            }
            onRestoreUriHandled()
        }
    }

    // M3 Expressive motion: spatial springs for slides, effects springs for
    // fades.
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val scaleSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    // Direction depends only on tab group order, never push vs pop: incoming
    // screen enters from side of its group.
    fun AnimatedContentTransitionScope<NavBackStackEntry>.enter(): EnterTransition {
        val fromGroup = tabGroup(initialState.destination.route)
        val toGroup = tabGroup(targetState.destination.route)
        return when {
            fromGroup == toGroup || fromGroup == 0 || toGroup == 0 ->
                fadeIn(effectsSpec) + scaleIn(scaleSpec, initialScale = 0.96f)

            toGroup > fromGroup ->
                slideInHorizontally(spatialSpec) { it }

            else ->
                slideInHorizontally(spatialSpec) { -it }
        }
    }

    fun AnimatedContentTransitionScope<NavBackStackEntry>.exit(): ExitTransition {
        val fromGroup = tabGroup(initialState.destination.route)
        val toGroup = tabGroup(targetState.destination.route)
        return when {
            fromGroup == toGroup || fromGroup == 0 || toGroup == 0 ->
                fadeOut(effectsSpec)

            toGroup > fromGroup ->
                slideOutHorizontally(spatialSpec) { -it }

            else ->
                slideOutHorizontally(spatialSpec) { it }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = nestedNavController,
                    startDestination = Routes.COLORS,
                    enterTransition = { enter() },
                    exitTransition = { exit() },
                    popEnterTransition = { enter() },
                    popExitTransition = { exit() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.COLORS) {
                        LaunchedEffect(Unit) {
                            if (isShizukuMode()) {
                                clearAllOverriddenColors()
                            }
                        }
                        ColorsScreen(
                            colorsViewModel = colorsViewModel,
                            onNavigateToColorPalette = {
                                nestedNavController.navigate(Routes.COLOR_PALETTE)
                            }
                        )
                    }
                    composable(Routes.THEME) { ThemeScreen() }
                    composable(Routes.STYLES) {
                        StylesScreen(stylesViewModel = stylesViewModel)
                    }
                    composable(
                        route = Routes.SETTINGS,
                        arguments = listOf(
                            navArgument("restoreUri") {
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { entry ->
                        var restoreConsumed by rememberSaveable { mutableStateOf(false) }
                        val restoreUriArg = entry.arguments?.getString("restoreUri")

                        SettingsScreen(
                            restoreUri = if (!restoreConsumed) {
                                restoreUriArg?.let { Uri.parse(it) }
                            } else {
                                null
                            },
                            onRestoreUriConsumed = { restoreConsumed = true },
                            onNavigateToAbout = { nestedNavController.navigate(Routes.ABOUT) },
                            onNavigateToAdvanced = {
                                nestedNavController.navigate(Routes.SETTINGS_ADVANCED)
                            },
                            onNavigateToPrivacyPolicy = {
                                nestedNavController.navigate(Routes.PRIVACY_POLICY)
                            }
                        )
                    }
                    composable(Routes.COLOR_PALETTE) {
                        ColorPaletteScreen(
                            colorPaletteViewModel = colorPaletteViewModel
                        )
                    }
                    composable(Routes.PER_APP_THEME) { PerAppThemeScreen() }
                    composable(Routes.SETTINGS_ADVANCED) {
                        SettingsAdvancedScreen(
                            onNavigateToPerAppTheme = {
                                nestedNavController.navigate(Routes.PER_APP_THEME)
                            }
                        )
                    }
                    composable(Routes.ABOUT) { AboutScreen() }
                    composable(Routes.PRIVACY_POLICY) { PrivacyPolicyScreen() }
                }

                val fabBottomPadding by animateDpAsState(
                    targetValue = if (SnackbarVisibility.visible) 76.dp else 12.dp,
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                    label = "previewFabSnackbarPush"
                )
                PreviewActionButtons(
                    visible = previewColors != null && !isApplying,
                    onApply = { scope.launch { PreviewController.applyChanges() } },
                    onDiscard = { scope.launch { PreviewController.discardChanges() } },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = fabBottomPadding)
                )
            }

            NavigationBar(modifier = Modifier.fillMaxWidth()) {
                tabs.forEach { tab ->
                    val selected = currentGroup == tab.group
                    NavigationBarItem(
                        selected = selected,
                        alwaysShowLabel = false,
                        icon = {
                            Icon(
                                painter = painterResource(
                                    if (selected) tab.filledIconResId else tab.outlineIconResId
                                ),
                                contentDescription = null
                            )
                        },
                        label = { Text(text = stringResource(tab.labelResId)) },
                        onClick = {
                            if (selected) return@NavigationBarItem

                            nestedNavController.navigate(tab.route) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) {
                                    inclusive = tab.route == Routes.COLORS
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }

        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        LoadingOverlay(
            visible = isApplying,
            text = stringResource(R.string.preview_applying)
        )
    }
}

@Suppress("ViewModelConstructorInComposable")
@Preview
@Composable
private fun HomeScreenPreview() {
    ColorBlendrTheme {
        HomeScreen(
            success = true,
            pendingRestoreUri = null,
            onRestoreUriHandled = {},
            colorsViewModel = ColorsViewModel(),
            stylesViewModel = StylesViewModel(Utilities.getCustomStyleRepository()),
            colorPaletteViewModel = ColorPaletteViewModel()
        )
    }
}
