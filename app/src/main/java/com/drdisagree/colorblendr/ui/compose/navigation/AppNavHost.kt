package com.drdisagree.colorblendr.ui.compose.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.WORKING_METHOD
import com.drdisagree.colorblendr.data.common.Utilities.isFirstRun
import com.drdisagree.colorblendr.data.common.Utilities.isWorkMethodUnknown
import com.drdisagree.colorblendr.data.common.Utilities.setFirstRunCompleted
import com.drdisagree.colorblendr.data.common.Utilities.setWorkingMethod
import com.drdisagree.colorblendr.provider.RootConnectionProvider
import com.drdisagree.colorblendr.provider.ShizukuConnectionProvider
import com.drdisagree.colorblendr.service.ShizukuConnection
import com.drdisagree.colorblendr.ui.activities.MainActivity
import com.drdisagree.colorblendr.ui.compose.screens.home.HomeScreen
import com.drdisagree.colorblendr.ui.compose.screens.onboarding.OnboardingScreen
import com.drdisagree.colorblendr.ui.compose.screens.pairing.PairingScreen
import com.drdisagree.colorblendr.ui.compose.theme.DecelerateEasing
import com.drdisagree.colorblendr.ui.compose.theme.shortAnimTime
import com.drdisagree.colorblendr.ui.viewmodels.ColorPaletteViewModel
import com.drdisagree.colorblendr.ui.viewmodels.ColorsViewModel
import com.drdisagree.colorblendr.ui.viewmodels.SharedViewModel
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
    colorsViewModel: ColorsViewModel,
    stylesViewModel: StylesViewModel,
    colorPaletteViewModel: ColorPaletteViewModel,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val animTime = shortAnimTime()

    var pendingRestoreUri by remember { mutableStateOf(restoreUri) }

    val startDestination = remember {
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

                navController.navigate(Routes.HOME) {
                    popUpTo(0) { inclusive = true }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun checkRootConnection() {
        RootConnectionProvider
            .builder(context)
            .onSuccess { goToHome() }
            .run()
    }

    fun checkShizukuConnection() {
        if (isShizukuAvailable) {
            val fragmentActivity = activity as? FragmentActivity ?: return
            requestShizukuPermission(fragmentActivity) { granted ->
                if (granted) {
                    bindUserService(
                        getUserServiceArgs(ShizukuConnection::class.java),
                        ShizukuConnectionProvider.serviceConnection
                    )
                    goToHome()
                } else {
                    Toast.makeText(
                        appContext,
                        R.string.shizuku_service_not_found,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                appContext,
                R.string.shizuku_service_not_found,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun checkAdbConnection() {
        if (WifiAdbShell.isMyDeviceConnected()) {
            goToHome()
        } else {
            Toast.makeText(
                context,
                R.string.wireless_adb_not_connected,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(tween(animTime, easing = DecelerateEasing)) { it }
        },
        exitTransition = {
            slideOutHorizontally(tween(animTime, easing = DecelerateEasing)) { -it }
        },
        popEnterTransition = {
            slideInHorizontally(tween(animTime, easing = DecelerateEasing)) { -it }
        },
        popExitTransition = {
            slideOutHorizontally(tween(animTime, easing = DecelerateEasing)) { it }
        }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
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
                success = success,
                pendingRestoreUri = pendingRestoreUri,
                onRestoreUriHandled = { pendingRestoreUri = null },
                colorsViewModel = colorsViewModel,
                stylesViewModel = stylesViewModel,
                colorPaletteViewModel = colorPaletteViewModel,
                sharedViewModel = sharedViewModel
            )
        }
    }
}
