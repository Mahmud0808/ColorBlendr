package com.drdisagree.colorblendr.ui.compose.navigation

import android.net.Uri
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.WORKING_METHOD
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.common.Utilities.setFirstRunCompleted
import com.drdisagree.colorblendr.data.common.Utilities.setWorkingMethod
import com.drdisagree.colorblendr.data.domain.RefreshCoordinator
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.ui.activities.MainActivity
import com.drdisagree.colorblendr.ui.compose.screens.home.HomeScreen
import com.drdisagree.colorblendr.ui.compose.screens.onboarding.OnboardingActionState
import com.drdisagree.colorblendr.ui.compose.screens.onboarding.OnboardingScreen
import com.drdisagree.colorblendr.ui.compose.screens.pairing.PairingScreen
import com.drdisagree.colorblendr.ui.viewmodels.ColorPaletteViewModel
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.StylesViewModel
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.updateFabricatedAppList
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.bindUserService
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.getUserServiceArgs
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.isShizukuAvailable
import com.drdisagree.colorblendr.utils.shizuku.ShizukuUtil.requestShizukuPermission
import com.drdisagree.colorblendr.utils.wallpaper.WallpaperColorUtil.updateWallpaperColorList
import com.drdisagree.colorblendr.utils.wifiadb.WifiAdbShell
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    success: Boolean,
    restoreUri: Uri?,
    deepLinkThemeId: String? = null,
    colorsViewModel: ColorsViewModel,
    stylesViewModel: StylesViewModel,
    colorPaletteViewModel: ColorPaletteViewModel
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    // Saveable so recreation doesn't re-trigger restore navigation.
    var pendingRestoreUri by rememberSaveable { mutableStateOf(restoreUri) }
    var pendingThemeId by rememberSaveable { mutableStateOf(deepLinkThemeId) }

    // Onboarding hands off with success=true.
    var onboardedSuccess by rememberSaveable { mutableStateOf(false) }

    var onboardingAction by remember {
        mutableStateOf<OnboardingActionState>(OnboardingActionState.Idle)
    }

    val startDestination = rememberSaveable {
        if (isFirstRun() || isWorkMethodUnknown() || !success) {
            Routes.ONBOARDING
        } else {
            Routes.HOME
        }
    }

    fun goToHome() {
        scope.launch {
            try {
                updateWallpaperColorList(context)
                updateFabricatedAppList(context)

                setFirstRunCompleted()
                setWorkingMethod(WORKING_METHOD)
                onboardedSuccess = true

                // ViewModels skipped initial load before onboarding; load
                // real data now.
                RefreshCoordinator.triggerRefresh()

                navController.navigate(Routes.HOME) {
                    popUpTo(0) { inclusive = true }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun checkRootConnection() {
        onboardingAction = OnboardingActionState.Connecting
        RootConnectionProvider
            .builder(context)
            .onSuccess { goToHome() }
            .onFailure {
                onboardingAction = OnboardingActionState.Error(
                    context.getString(R.string.root_service_not_found)
                )
            }
            .run()
    }

    fun checkShizukuConnection() {
        if (isShizukuAvailable) {
            val fragmentActivity = activity as? FragmentActivity ?: return
            onboardingAction = OnboardingActionState.Connecting
            requestShizukuPermission(fragmentActivity) { granted ->
                if (granted) {
                    bindUserService(
                        getUserServiceArgs(ShizukuConnection::class.java),
                        ShizukuConnectionProvider.serviceConnection
                    )
                    goToHome()
                } else {
                    onboardingAction = OnboardingActionState.Error(
                        context.getString(R.string.shizuku_service_not_found)
                    )
                }
            }
        } else {
            onboardingAction = OnboardingActionState.Error(
                context.getString(R.string.shizuku_service_not_found)
            )
        }
    }

    fun checkAdbConnection() {
        if (WifiAdbShell.isMyDeviceConnected()) {
            onboardingAction = OnboardingActionState.Connecting
            goToHome()
        } else {
            onboardingAction = OnboardingActionState.Error(
                context.getString(R.string.wireless_adb_not_connected)
            )
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(spatialSpec) { it }
        },
        exitTransition = {
            slideOutHorizontally(spatialSpec) { -it }
        },
        popEnterTransition = {
            slideInHorizontally(spatialSpec) { -it }
        },
        popExitTransition = {
            slideOutHorizontally(spatialSpec) { it }
        }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                actionState = onboardingAction,
                onError = { onboardingAction = OnboardingActionState.Error(it) },
                onErrorDismissed = { onboardingAction = OnboardingActionState.Idle },
                onCheckRootConnection = ::checkRootConnection,
                onCheckShizukuConnection = ::checkShizukuConnection,
                onCheckAdbConnection = ::checkAdbConnection,
                onNavigateToPairing = { navController.navigate(Routes.PAIRING) },
                popActivityBackStack = { false },
                onFinishActivity = { activity?.finish() }
            )
        }
        composable(Routes.PAIRING) {
            PairingScreen(
                onPairDevice = { (activity as? MainActivity)?.pairThisDevice() },
                onDeviceConnected = { navController.popBackStack() }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                success = success || onboardedSuccess,
                pendingRestoreUri = pendingRestoreUri,
                onRestoreUriHandled = { pendingRestoreUri = null },
                pendingThemeId = pendingThemeId,
                onThemeIdHandled = { pendingThemeId = null },
                colorsViewModel = colorsViewModel,
                stylesViewModel = stylesViewModel,
                colorPaletteViewModel = colorPaletteViewModel
            )
        }
    }
}
