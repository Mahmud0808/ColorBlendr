package com.drdisagree.colorblendr.utils.fabricated

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Constant.LINEAGE_PARTS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_LINEAGEOS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_SEARCH
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_SEARCH_AOSP
import com.drdisagree.colorblendr.data.common.Constant.THEME_PICKER
import com.drdisagree.colorblendr.data.common.Constant.THEME_PICKER_GOOGLE
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.perAppDefaultsSeeded
import com.drdisagree.colorblendr.data.common.Utilities.setPerAppDefaultsSeeded
import com.drdisagree.colorblendr.data.common.Utilities.setSelectedFabricatedApps
import com.drdisagree.colorblendr.utils.app.SystemUtil.isAppInstalled
import com.drdisagree.colorblendr.utils.manager.OverlayManager

object FabricatedUtil {

    private val SETTINGS_PACKAGES = listOf(SETTINGS, SETTINGS_LINEAGEOS, LINEAGE_PARTS)

    fun selectSettingsForPerAppTheme() {
        val selectedApps = getSelectedFabricatedApps()
        SETTINGS_PACKAGES.filter(::isAppInstalled).forEach { selectedApps[it] = true }
        setSelectedFabricatedApps(selectedApps)
    }

    fun updateFabricatedAppList(context: Context) {
        if (!isRootMode() || !isThemingEnabled()) return

        val packageManager = context.packageManager
        val applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val enabled = OverlayManager.areOverlaysEnabled(
            applications.map { String.format(FABRICATED_OVERLAY_NAME_APPS, it.packageName) }
        ) ?: return

        val selectedApps = HashMap<String, Boolean>().apply {
            applications.forEachIndexed { index, appInfo ->
                if (enabled[index]) {
                    put(appInfo.packageName, true)
                }
            }
        }

        //        selectedApps.put(BuildConfig.APPLICATION_ID, true)

        // Themed by default for android 15+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM &&
            !perAppDefaultsSeeded()
        ) {
            listOf(
                SETTINGS,
                SETTINGS_LINEAGEOS,
                SETTINGS_SEARCH,
                SETTINGS_SEARCH_AOSP,
                LINEAGE_PARTS,
                THEME_PICKER,
                THEME_PICKER_GOOGLE
            ).forEach { packageName ->
                if (selectedApps[packageName] != java.lang.Boolean.TRUE &&
                    isAppInstalled(packageName)
                ) {
                    selectedApps[packageName] = true
                }
            }
            setPerAppDefaultsSeeded(true)
        }

        setSelectedFabricatedApps(selectedApps)
    }
}
