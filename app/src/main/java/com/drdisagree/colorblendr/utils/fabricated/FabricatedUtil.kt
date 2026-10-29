package com.drdisagree.colorblendr.utils.fabricated

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.drdisagree.colorblendr.data.common.Constant.DOLBY_ATMOS
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_APPS
import com.drdisagree.colorblendr.data.common.Constant.GOOGLE_FEEDS
import com.drdisagree.colorblendr.data.common.Constant.GOOGLE_NEWS
import com.drdisagree.colorblendr.data.common.Constant.LINEAGE_PARTS
import com.drdisagree.colorblendr.data.common.Constant.PIXEL_LAUNCHER
import com.drdisagree.colorblendr.data.common.Constant.PLAY_GAMES
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_LINEAGEOS
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_SEARCH
import com.drdisagree.colorblendr.data.common.Constant.SETTINGS_SEARCH_AOSP
import com.drdisagree.colorblendr.data.common.Constant.THEME_PICKER
import com.drdisagree.colorblendr.data.common.Constant.THEME_PICKER_GOOGLE
import com.drdisagree.colorblendr.data.common.Utilities.getAppColorOverrides
import com.drdisagree.colorblendr.data.common.Utilities.getPitchBlackAutoSelectedApps
import com.drdisagree.colorblendr.data.common.Utilities.getPitchBlackDeclinedApps
import com.drdisagree.colorblendr.data.common.Utilities.getSelectedFabricatedApps
import com.drdisagree.colorblendr.data.common.Utilities.isRootMode
import com.drdisagree.colorblendr.data.common.Utilities.isThemingEnabled
import com.drdisagree.colorblendr.data.common.Utilities.pitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setPitchBlackAutoSelectedApps
import com.drdisagree.colorblendr.data.common.Utilities.setPitchBlackDeclinedApps
import com.drdisagree.colorblendr.data.common.Utilities.setSelectedFabricatedApps
import com.drdisagree.colorblendr.utils.app.SystemUtil.isAppInstalled
import com.drdisagree.colorblendr.utils.manager.OverlayManager

object FabricatedUtil {

    private val PITCH_BLACK_PACKAGES = listOf(
        PIXEL_LAUNCHER,
        GOOGLE_FEEDS,
        GOOGLE_NEWS,
        PLAY_GAMES
    )

    private val PITCH_BLACK_PACKAGES_A15 = listOf(
        SETTINGS,
        SETTINGS_LINEAGEOS,
        SETTINGS_SEARCH,
        SETTINGS_SEARCH_AOSP,
        LINEAGE_PARTS,
        THEME_PICKER,
        THEME_PICKER_GOOGLE,
        DOLBY_ATMOS
    )

    private fun pitchBlackPackages(): List<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            PITCH_BLACK_PACKAGES + PITCH_BLACK_PACKAGES_A15
        } else {
            PITCH_BLACK_PACKAGES
        }

    fun syncPitchBlackApps(): List<String> {
        if (!isRootMode()) return emptyList()

        val autoSelected = getPitchBlackAutoSelectedApps()
        val pitchBlack = pitchBlackThemeEnabled()
        if (pitchBlack == (autoSelected != null)) return emptyList()

        val selectedApps = getSelectedFabricatedApps()

        if (pitchBlack) {
            val declined = getPitchBlackDeclinedApps()
            val added = pitchBlackPackages().filter {
                selectedApps[it] != true && it !in declined && isAppInstalled(it)
            }
            added.forEach { selectedApps[it] = true }
            setSelectedFabricatedApps(selectedApps)
            setPitchBlackAutoSelectedApps(added.toSet())
            return emptyList()
        }

        val customized = getAppColorOverrides().keys
        val removed = autoSelected.orEmpty().filter { it !in customized }
        removed.forEach { selectedApps[it] = false }
        setSelectedFabricatedApps(selectedApps)
        setPitchBlackAutoSelectedApps(null)
        return removed
    }

    fun selectPitchBlackApp(packageName: String): Boolean {
        if (!isRootMode() || packageName !in pitchBlackPackages()) return false
        if (packageName in getPitchBlackDeclinedApps()) return false

        val autoSelected = getPitchBlackAutoSelectedApps() ?: return false
        val selectedApps = getSelectedFabricatedApps()

        if (selectedApps[packageName] == true) return false

        selectedApps[packageName] = true
        setSelectedFabricatedApps(selectedApps)
        setPitchBlackAutoSelectedApps(autoSelected + packageName)

        return true
    }

    fun forgetPitchBlackAutoSelection(packageName: String) {
        val autoSelected = getPitchBlackAutoSelectedApps() ?: return
        if (packageName in autoSelected) setPitchBlackAutoSelectedApps(autoSelected - packageName)
    }

    fun onPerAppSelectionChanged(packageName: String, selected: Boolean) {
        forgetPitchBlackAutoSelection(packageName)
        if (packageName !in pitchBlackPackages()) return

        val declined = getPitchBlackDeclinedApps()
        setPitchBlackDeclinedApps(if (selected) declined - packageName else declined + packageName)
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

        setSelectedFabricatedApps(selectedApps)
    }
}
