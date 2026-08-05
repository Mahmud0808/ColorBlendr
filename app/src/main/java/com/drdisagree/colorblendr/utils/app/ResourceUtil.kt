package com.drdisagree.colorblendr.utils.app

import android.content.pm.PackageManager
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext

object ResourceUtil {

    /**
     * Checks if a resource exists in a given package.
     *
     * @param packageName The package name to check.
     * @param resourceName The name of the resource.
     * @param type The type of the resource (e.g., "color", "bool").
     * @return True if the resource exists, false otherwise.
     */
    fun getResourcePackage(packageName: String, resourceName: String, type: String): String? {
        return try {
            val resources = appContext.packageManager.getResourcesForApplication(packageName)
            
            // Try the provided package name
            var resId = resources.getIdentifier(resourceName, type, packageName)
            
            // If not found and it's a launcher, try com.android.launcher3
            if (resId == 0 && packageName.contains("launcher")) {
                resId = resources.getIdentifier(resourceName, type, "com.android.launcher3")
            }

            if (resId != 0) {
                val foundPackage = resources.getResourcePackageName(resId)
                
                // If targeting framework, we want framework resources.
                // If targeting an app, we only want resources belonging to that app (or its base launcher class),
                // not framework resources inherited by the app.
                if (packageName == "android" && foundPackage == "android") {
                    return "android"
                } else if (packageName != "android" && foundPackage != "android") {
                    return foundPackage
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun resourceExists(packageName: String, resourceName: String, type: String): Boolean {
        return getResourcePackage(packageName, resourceName, type) != null
    }
}
