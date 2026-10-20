package com.drdisagree.colorblendr.utils.app

import android.annotation.SuppressLint
import android.content.res.Resources
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant.FRAMEWORK_PACKAGE
import com.drdisagree.colorblendr.data.common.Constant.LAUNCHER3_LAUNCHER

object ResourceUtil {

    private val resourcesCache = HashMap<String, Resources?>()
    private val packageCache = HashMap<String, String?>()

    @Synchronized
    fun clearLookupCache() {
        resourcesCache.clear()
        packageCache.clear()
    }

    private fun resourcesFor(packageName: String): Resources? =
        resourcesCache.getOrPut(packageName) {
            runCatching {
                appContext.packageManager.getResourcesForApplication(packageName)
            }.getOrNull()
        }

    /**
     * Checks if a resource exists in a given package.
     *
     * @param packageName The package name to check.
     * @param resourceName The name of the resource.
     * @param type The type of the resource (e.g., "color", "bool").
     * @return True if the resource exists, false otherwise.
     */
    @SuppressLint("DiscouragedApi")
    @Synchronized
    fun getResourcePackage(packageName: String, resourceName: String, type: String): String? {
        val key = "$packageName\u0000$resourceName\u0000$type"
        if (packageCache.containsKey(key)) return packageCache[key]

        val resources = resourcesFor(packageName)
        val result = if (resources == null) null else runCatching {
            // Try the provided package name
            var resId = resources.getIdentifier(resourceName, type, packageName)

            // If not found, and it's a launcher, try com.android.launcher3
            if (resId == 0 && packageName.contains("launcher")) {
                resId = resources.getIdentifier(resourceName, type, LAUNCHER3_LAUNCHER)
            }

            if (resId != 0) {
                val foundPackage = resources.getResourcePackageName(resId)

                // If targeting framework, we want framework resources.
                // If targeting an app, we only want resources belonging to that app (or its base launcher class),
                // not framework resources inherited by the app.
                if (packageName == FRAMEWORK_PACKAGE && foundPackage == FRAMEWORK_PACKAGE) {
                    FRAMEWORK_PACKAGE
                } else if (packageName != FRAMEWORK_PACKAGE && foundPackage != FRAMEWORK_PACKAGE) {
                    foundPackage
                } else {
                    null
                }
            } else {
                null
            }
        }.getOrNull()

        packageCache[key] = result
        return result
    }

    fun resourceExists(packageName: String, resourceName: String, type: String): Boolean {
        return getResourcePackage(packageName, resourceName, type) != null
    }
}
