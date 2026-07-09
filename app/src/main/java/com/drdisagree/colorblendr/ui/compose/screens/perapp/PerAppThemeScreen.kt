package com.drdisagree.colorblendr.ui.compose.screens.perapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Utilities.getAppListFilteringMethod
import com.drdisagree.colorblendr.data.common.Utilities.isShizukuThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.isWirelessAdbThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setAppListFilteringMethod
import com.drdisagree.colorblendr.data.common.Utilities.setSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.setShowPerAppThemeWarning
import com.drdisagree.colorblendr.data.common.Utilities.showPerAppThemeWarning
import com.drdisagree.colorblendr.data.enums.AppType
import com.drdisagree.colorblendr.data.models.AppInfoModel
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.SearchBar
import com.drdisagree.colorblendr.ui.compose.components.WarningCard
import com.drdisagree.colorblendr.ui.compose.components.WidgetPosition
import com.drdisagree.colorblendr.ui.compose.theme.AppCardDefaults
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import com.drdisagree.colorblendr.utils.fabricated.FabricatedUtil.updateFabricatedAppList
import com.drdisagree.colorblendr.utils.manager.OverlayManager.applyFabricatedColorsPerApp
import com.drdisagree.colorblendr.utils.manager.OverlayManager.isOverlayEnabled
import com.drdisagree.colorblendr.utils.manager.OverlayManager.unregisterFabricatedOverlay
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.google.android.material.R as MaterialR

@Composable
fun PerAppThemeScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val toolbarLifted by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val hazeState = remember { HazeState() }

    var filterMethod by remember { mutableIntStateOf(getAppListFilteringMethod()) }
    var appList by remember { mutableStateOf<List<AppInfoModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var warningVisible by remember { mutableStateOf(showPerAppThemeWarning()) }
    var reloadTrigger by remember { mutableIntStateOf(0) }
    val selections = remember { mutableStateMapOf<String, Boolean>() }
    val selectedApps = remember { HashMap<String, Boolean>() }

    LaunchedEffect(filterMethod, reloadTrigger) {
        isLoading = true
        withContext(Dispatchers.IO) {
            updateFabricatedAppList(appContext)
            val apps = getAllInstalledApps(AppType.entries[filterMethod])

            if (isThemingEnabled(false)
                || isShizukuThemingEnabled(false)
                || isWirelessAdbThemingEnabled(false)
            ) {
                apps.asSequence()
                    .filter { it.isSelected }
                    .forEach { selectedApps[it.packageName] = true }
                setSelectedFabricatedApps(selectedApps)
            }

            withContext(Dispatchers.Main) {
                selections.clear()
                apps.forEach { selections[it.packageName] = it.isSelected }
                appList = apps
                isLoading = false
            }
        }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                reloadTrigger++
            }
        }
        val manager = LocalBroadcastManager.getInstance(context)
        manager.registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
            }
        )
        manager.registerReceiver(
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }
        )
        onDispose {
            try {
                manager.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    val filteredList = remember(appList, query) { filterList(appList, query) }

    fun showFilterDialog() {
        val items = arrayOf(
            context.getString(R.string.filter_system_apps),
            context.getString(R.string.filter_user_apps),
            context.getString(R.string.filter_launchable_apps),
            context.getString(R.string.filter_all)
        )

        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.filter_app_category))
            .setSingleChoiceItems(items, getAppListFilteringMethod()) { dialog, which ->
                setAppListFilteringMethod(which)
                filterMethod = which
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    fun toggleApp(app: AppInfoModel) {
        val isSelected = selections[app.packageName] == true
        selections[app.packageName] = !isSelected
        app.isSelected = !isSelected

        selectedApps[app.packageName] = !isSelected
        setSelectedFabricatedApps(selectedApps)

        scope.launch {
            if (isSelected) {
                unregisterFabricatedOverlay(
                    String.format(FABRICATED_OVERLAY_NAME_APPS, app.packageName)
                )
            } else {
                applyFabricatedColorsPerApp(app.packageName, null)
            }
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.per_app_theme),
                showBackButton = true,
                lifted = toolbarLifted
            )
            Column {
                AnimatedVisibility(
                    visible = warningVisible,
                    exit = slideOutHorizontally { it * 2 } + fadeOut()
                ) {
                    WarningCard(
                        warningText = stringResource(R.string.per_app_theme_warn),
                        onClose = {
                            scope.launch {
                                delay(50)
                                setShowPerAppThemeWarning(false)
                                warningVisible = false
                            }
                        },
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(top = 72.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .hazeSource(hazeState)
                    ) {
                        itemsIndexed(
                            filteredList,
                            key = { _, app -> app.packageName }
                        ) { index, app ->
                            AppListItem(
                                app = app,
                                selected = selections[app.packageName] == true,
                                position = when {
                                    filteredList.size == 1 -> WidgetPosition.Single
                                    index == 0 -> WidgetPosition.Top
                                    index == filteredList.lastIndex -> WidgetPosition.Bottom
                                    else -> WidgetPosition.Middle
                                },
                                onClick = { toggleApp(app) }
                            )
                        }
                    }

                    if (isLoading) {
                        LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    SearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        onFilterClick = ::showFilterDialog,
                        hazeState = hazeState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: AppInfoModel,
    selected: Boolean,
    position: WidgetPosition,
    onClick: () -> Unit
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val strokeWidth = with(LocalDensity.current) { 2.toDp() }
    val appIcon = remember(app.packageName) {
        app.appIcon?.toBitmap()?.asImageBitmap()
    }

    val radius = dimensionResource(R.dimen.container_corner_radius)
    val radiusSmall = dimensionResource(R.dimen.container_corner_radius_small)
    val (topRadius, bottomRadius) = when (position) {
        WidgetPosition.Single -> radius to radius
        WidgetPosition.Top -> radius to radiusSmall
        WidgetPosition.Middle -> radiusSmall to radiusSmall
        WidgetPosition.Bottom -> radiusSmall to radius
    }

    Surface(
        shape = RoundedCornerShape(topRadius, topRadius, bottomRadius, bottomRadius),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = if (selected) {
            null
        } else {
            BorderStroke(strokeWidth, AppCardDefaults.outlinedBorder().brush)
        },
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = dimensionResource(R.dimen.container_margin_horizontal),
                end = dimensionResource(R.dimen.container_margin_horizontal),
                bottom = dimensionResource(
                    when (position) {
                        WidgetPosition.Single, WidgetPosition.Bottom ->
                            R.dimen.container_margin_bottom

                        WidgetPosition.Top, WidgetPosition.Middle ->
                            R.dimen.container_margin_bottom_small
                    }
                )
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 16.dp)
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                        .size(48.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                painter = painterResource(
                    if (selected) R.drawable.ic_checked_filled else R.drawable.ic_checked_outline
                ),
                contentDescription = null,
                tint = if (selected) {
                    themeAttrColor(MaterialR.attr.colorPrimaryVariant)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }.copy(alpha = if (selected) 1f else 0.2f)
            )
        }
    }
}

private fun filterList(appList: List<AppInfoModel>, query: String): List<AppInfoModel> {
    if (query.trim().isEmpty()) return appList

    val lowerQuery = query.trim().lowercase(Locale.getDefault())
    val startsWithNameList = mutableListOf<AppInfoModel>()
    val containsNameList = mutableListOf<AppInfoModel>()
    val startsWithPackageNameList = mutableListOf<AppInfoModel>()
    val containsPackageNameList = mutableListOf<AppInfoModel>()

    for (app in appList) {
        val appName = app.appName.lowercase(Locale.getDefault())
        val packageName = app.packageName.lowercase(Locale.getDefault())
        when {
            appName.startsWith(lowerQuery) -> startsWithNameList.add(app)
            appName.contains(lowerQuery) -> containsNameList.add(app)
            packageName.startsWith(lowerQuery) -> startsWithPackageNameList.add(app)
            packageName.contains(lowerQuery) -> containsPackageNameList.add(app)
        }
    }

    return startsWithNameList + containsNameList + startsWithPackageNameList + containsPackageNameList
}

private fun getAllInstalledApps(appType: AppType): List<AppInfoModel> {
    val appList: MutableList<AppInfoModel> = ArrayList()
    val packageManager = appContext.packageManager

    val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    for (appInfo in applications) {
        val packageName = appInfo.packageName

        if (appType == AppType.LAUNCHABLE) {
            packageManager.getLaunchIntentForPackage(packageName) ?: continue
        }

        val appName = appInfo.loadLabel(packageManager).toString()
        val appIcon = appInfo.loadIcon(packageManager)
        val isSelected = isOverlayEnabled(
            String.format(FABRICATED_OVERLAY_NAME_APPS, packageName)
        )

        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val includeApp = when (appType) {
            AppType.SYSTEM -> isSystemApp
            AppType.USER -> !isSystemApp
            AppType.LAUNCHABLE, AppType.ALL -> true
        }

        if (includeApp) {
            val app = AppInfoModel(appName, packageName, appIcon)
            app.isSelected = isSelected
            appList.add(app)
        }
    }

    appList.sortWith(compareBy<AppInfoModel> { !it.isSelected }.thenBy { it.appName.lowercase() })

    return appList
}

@Preview
@Composable
private fun PerAppThemeScreenPreview() {
    ColorBlendrTheme {
        PerAppThemeScreen()
    }
}
