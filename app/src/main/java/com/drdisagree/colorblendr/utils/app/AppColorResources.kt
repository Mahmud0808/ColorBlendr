package com.drdisagree.colorblendr.utils.app

import android.content.pm.ApplicationInfo
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import android.util.SparseArray
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.models.AppColorResource
import java.util.concurrent.ConcurrentHashMap
import android.R as AndroidR

object AppColorResources {

    private const val APP_PACKAGE_ID = 0x7f
    private const val FIRST_TYPE_ID = 1
    private const val MAX_TYPE_ID = 0xff
    private const val COLOR_TYPE = "color"
    private const val MAX_ENTRIES = 20_000
    private const val MAX_GAP = 2048
    private const val OBFUSCATED_NAME = "0_resource_name_obfuscated"
    private const val DEFAULT_THEME = AndroidR.style.Theme_DeviceDefault_DayNight
    private val OVERLAY_FIELDS = listOf("resourceDirs", "overlayPaths")

    private val availability = ConcurrentHashMap<String, Boolean>()

    fun hasColors(packageName: String): Boolean = availability.getOrPut(packageName) {
        runCatching {
            val info = appContext.packageManager.getApplicationInfo(packageName, 0)
            if (info.isResourceOverlay()) return@runCatching false
            val resources = appContext.packageManager.getResourcesForApplication(info)
            colorTypes(resources, packageName).any { base ->
                (0 until MAX_GAP).any { index -> resources.entryName(base or index).isUsable() }
            }
        }.getOrDefault(false)
    }

    fun load(packageName: String): List<AppColorResource> = runCatching {
        val info = appContext.packageManager.getApplicationInfo(packageName, 0)
        if (info.isResourceOverlay()) return emptyList()
        val resources = appContext.packageManager.getResourcesForApplication(info.withoutOverlays())
        val light = resources.themed(info, night = false)
        val dark = resources.themed(info, night = true)

        val entryCounts = ResourceTableSpec.entryCounts(info.sourceDir)
        val ids = LinkedHashMap<String, Int>()
        colorTypes(light.first, packageName).forEach { base ->
            colorIds(light.first, base, entryCounts[base]).forEach { (name, id) -> ids.putIfAbsent(name, id) }
        }
        availability[packageName] = ids.isNotEmpty()

        ids.map { (name, id) -> AppColorResource(name, light.colorOrNull(id), dark.colorOrNull(id)) }
            .sortedBy { it.name }
    }.getOrDefault(emptyList())

    fun label(packageName: String): String? = runCatching {
        val packageManager = appContext.packageManager
        packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager).toString()
    }.getOrNull()

    private fun ApplicationInfo.isResourceOverlay(): Boolean = runCatching {
        ApplicationInfo::class.java.getMethod("isResourceOverlay").invoke(this) as Boolean
    }.getOrDefault(false)

    private fun ApplicationInfo.withoutOverlays(): ApplicationInfo = ApplicationInfo(this).apply {
        OVERLAY_FIELDS.forEach { field ->
            runCatching { ApplicationInfo::class.java.getField(field).set(this, null) }
        }
    }

    @Suppress("DEPRECATION")
    private fun Resources.themed(info: ApplicationInfo, night: Boolean): Pair<Resources, Resources.Theme?> {
        val configuration = Configuration(appContext.resources.configuration).apply {
            val mode = if (night) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or mode
        }
        val resources = Resources(assets, displayMetrics, configuration)
        val theme = runCatching {
            resources.newTheme().apply {
                applyStyle(info.theme.takeIf { it != 0 } ?: DEFAULT_THEME, true)
            }
        }.getOrNull()
        return resources to theme
    }

    private fun colorTypes(resources: Resources, packageName: String): List<Int> =
        packageIds(resources, packageName).flatMap { packageId ->
            (FIRST_TYPE_ID..MAX_TYPE_ID)
                .filter { type -> resources.typeName(resourceId(packageId, type, 0)) == COLOR_TYPE }
                .map { type -> resourceId(packageId, type, 0) }
        }

    private fun packageIds(resources: Resources, packageName: String): List<Int> {
        val assigned = assignedPackageIds(resources) ?: return listOf(APP_PACKAGE_ID)
        return assigned.filter { (id, name) -> id == APP_PACKAGE_ID || name == packageName }.keys.toList()
    }

    private fun assignedPackageIds(resources: Resources): Map<Int, String>? = runCatching {
        val identifiers = AssetManager::class.java
            .getMethod("getAssignedPackageIdentifiers")
            .invoke(resources.assets) as SparseArray<*>
        buildMap {
            for (index in 0 until identifiers.size()) {
                put(identifiers.keyAt(index), identifiers.valueAt(index).toString())
            }
        }
    }.getOrNull()

    private fun colorIds(resources: Resources, base: Int, entryCount: Int?): Map<String, Int> {
        val ids = LinkedHashMap<String, Int>()
        val limit = entryCount?.coerceIn(0, MAX_ENTRIES) ?: MAX_ENTRIES
        var index = 0
        var misses = 0
        while (index < limit && (entryCount != null || misses < MAX_GAP)) {
            val id = base or index
            val name = resources.entryName(id)
            if (name == null) {
                misses++
            } else {
                misses = 0
                if (name.isUsable()) ids.putIfAbsent(name, id)
            }
            index++
        }
        return ids
    }

    private fun Resources.entryName(id: Int): String? =
        runCatching { getResourceEntryName(id) }.getOrNull()

    private fun String?.isUsable(): Boolean = this != null && this != OBFUSCATED_NAME

    private fun Resources.typeName(id: Int): String? = runCatching { getResourceTypeName(id) }.getOrNull()

    private fun Pair<Resources, Resources.Theme?>.colorOrNull(id: Int): Int? =
        runCatching { first.getColor(id, second) }.getOrNull()

    private fun resourceId(packageId: Int, type: Int, entry: Int): Int =
        (packageId shl 24) or (type shl 16) or entry
}
