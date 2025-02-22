package com.drdisagree.colorblendr.service

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.app.IProcessObserver
import android.content.Intent
import android.content.om.IOverlayManager
import android.content.om.OverlayIdentifier
import android.content.om.OverlayManagerTransaction
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.IUserManager
import android.os.Looper
import android.os.Process
import android.os.RemoteException
import android.os.UserHandle
import android.util.Log
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.data.common.Constant.FABRICATED_OVERLAY_SOURCE_PACKAGE
import com.drdisagree.colorblendr.data.common.Constant.SYSTEMUI_PACKAGE
import com.drdisagree.colorblendr.utils.fabricated.FabricatedOverlayResource
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.internal.Utils
import com.topjohnwu.superuser.ipc.RootService
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class RootConnection : RootService() {

    override fun onBind(intent: Intent): IBinder {
        return RootConnectionImpl()
    }

    class RootConnectionImpl : IRootConnection.Stub() {

        /**
         * A listener that observes process lifecycle events, specifically used to detect when
         * the System UI process dies.
         *
         * This listener implements the [IProcessObserver] interface and is registered with
         * the Activity Manager to receive notifications about process state changes.
         */
        private val processListener: IProcessObserver.Stub = object : IProcessObserver.Stub() {
            @Throws(RemoteException::class)
            override fun onForegroundActivitiesChanged(
                pid: Int,
                uid: Int,
                foregroundActivities: Boolean
            ) {
                // Do nothing
            }

            @Throws(RemoteException::class)
            override fun onForegroundServicesChanged(pid: Int, uid: Int, serviceTypes: Int) {
                // Do nothing
            }

            override fun onProcessDied(pid: Int, uid: Int) {
                if (uid == SystemUI_UID) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            enableOverlayWithIdentifier(listOf(FABRICATED_OVERLAY_NAME_SYSTEM))
                            Log.d(TAG, "SystemUI restarted, re-enabling overlay")
                        } catch (ignored: RemoteException) {
                            // Overlay was never registered
                        }
                    }, 3000)
                }
            }
        }

        /**
         * Checks if the device is rooted.
         *
         * This method utilizes the `Shell.isAppGrantedRoot()` function to determine if the app has root access,
         * which is an indication of a rooted device.
         *
         * @return `true` if the device is rooted, `false` otherwise.
         * @throws RemoteException if there is an issue communicating with the remote service used for root detection.
         */
        @Throws(RemoteException::class)
        override fun isRooted(): Boolean {
            return Shell.isAppGrantedRoot() ?: false
        }

        /**
         * Registers a listener to be notified when SystemUI restarts.
         *
         * This function registers a process observer that monitors the SystemUI process.
         * When the SystemUI process is restarted, the registered listener will be notified.
         *
         * @throws RemoteException if there is an error communicating with the ActivityManager service.
         */
        @Throws(RemoteException::class)
        override fun setSystemUIRestartListener() {
            mActivityManager.registerProcessObserver(processListener)
        }

        /**
         * Checks if an overlay package is currently installed for the current user.
         *
         * @param packageName The package name of the overlay to check.
         * @return `true` if the overlay package is installed, `false` otherwise.
         * @throws RemoteException if there is a communication error with the system service.
         */
        @Throws(RemoteException::class)
        override fun isOverlayInstalled(packageName: String): Boolean {
            return mOverlayManager.getOverlayInfo(packageName, currentUserId) != null
        }

        /**
         * Checks if an overlay package is enabled for the current user.
         *
         * This method retrieves overlay information using the provided package name and
         * checks its enabled status using reflection to access the `isEnabled` method
         * of the overlay object.
         *
         * @param packageName The package name of the overlay to check.
         * @return `true` if the overlay is enabled, `false` otherwise. This also returns `false`
         * if the overlay is not found or if any reflection-related errors occur.
         *
         * @throws RemoteException if there is a communication error with the overlay manager service.
         */
        @Throws(RemoteException::class)
        override fun isOverlayEnabled(packageName: String): Boolean {
            val overlay = mOverlayManager.getOverlayInfoByIdentifier(
                generateOverlayIdentifier(packageName),
                currentUserId
            )

            if (overlay == null) {
                return false
            }

            try {
                val isEnabled = overlay.javaClass.getDeclaredMethod("isEnabled")
                return isEnabled.invoke(overlay) as Boolean
            } catch (e: NoSuchMethodException) {
                Log.e(TAG, "Failed to get isEnabled method", e)
                return false
            } catch (e: InvocationTargetException) {
                Log.e(TAG, "Failed to invoke isEnabled method", e)
                return false
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "Failed to access isEnabled method", e)
                return false
            }
        }

        /**
         * Enables the specified overlay packages.
         *
         * This function requests that the given overlay packages be enabled.
         * The system will attempt to enable the overlays when it is safe and possible to do so.
         *
         * @param packages A list of package names representing the overlays to be enabled.
         */
        override fun enableOverlay(packages: List<String>) {
            packages.forEach { p ->
                switchOverlay(p, true)
            }
        }

        /**
         * Enables one or more overlay packages by their package names.
         *
         * This function iterates through the provided list of package names,
         * generates an identifier for each package, and then requests for the
         * corresponding overlay to be enabled.
         *
         * @param packages A list of package names for the overlays to be enabled.
         * @throws RemoteException If a remote exception occurs during the operation.
         */
        @Throws(RemoteException::class)
        override fun enableOverlayWithIdentifier(packages: List<String>) {
            packages.forEach { p ->
                val identifier = generateOverlayIdentifier(p)
                switchOverlayWithIdentifier(identifier, true)
            }
        }

        /**
         * Enables an overlay package exclusively for all user profiles associated with the current user.
         *
         * This function requests that the specified overlay package is enabled and any other overlay
         * packages with the same target package are disabled. The operation is performed for the
         * current user's profile and all associated managed profiles.
         *
         * @param packageName The package name of the overlay to enable exclusively.
         * @return `true` if the overlay was successfully enabled exclusively, `false` otherwise.
         *
         * @throws RemoteException if there is a communication error with the system service.
         */
        @Throws(RemoteException::class)
        override fun enableOverlayExclusive(packageName: String): Boolean {
            var result = false
            var currentUserListed = false
            val profiles = mUserManager.getProfiles(currentUserId, true)

            profiles.forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        val userId = userInfo.userHandle.getUserIdentifier()
                        val tempResult = mOverlayManager.setEnabledExclusive(
                            packageName,
                            true,
                            userId
                        )
                        if (userId == currentUserId) {
                            currentUserListed = true
                            result = tempResult
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error enabling overlay for user ${userInfo.userHandle}",
                        e
                    )
                }
            }

            if (!currentUserListed) {
                result = mOverlayManager.setEnabledExclusive(packageName, true, currentUserId)
            }

            return result
        }

        /**
         * Enables an overlay package exclusively within its category.
         *
         * This function requests that the specified overlay package be enabled and any other overlay packages
         * with the same target package and category be disabled. The operation is performed for the current user
         * and all associated profiles.
         *
         * @param packageName The package name of the overlay to enable exclusively.
         * @return `true` if the operation was successful, `false` otherwise.
         *
         * @throws RemoteException if a remote exception occurs during the operation.
         */
        @Throws(RemoteException::class)
        override fun enableOverlayExclusiveInCategory(packageName: String): Boolean {
            var result = false
            var currentUserListed = false
            val profiles = mUserManager.getProfiles(currentUserId, true)

            profiles.forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        val userId = userInfo.userHandle.getUserIdentifier()
                        val tempResult = mOverlayManager.setEnabledExclusiveInCategory(
                            packageName,
                            userInfo.userHandle.getUserIdentifier()
                        )
                        if (userId == currentUserId) {
                            currentUserListed = true
                            result = tempResult
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error enabling overlay for user ${userInfo.userHandle}",
                        e
                    )
                }
            }

            if (!currentUserListed) {
                result = mOverlayManager.setEnabledExclusiveInCategory(packageName, currentUserId)
            }

            return result
        }

        /**
         * Requests that the specified overlay packages be disabled when possible.
         *
         * This function iterates through the provided list of package names and attempts to disable each one.
         * The disabling process may not be immediate and depends on the system's ability to handle overlay changes.
         *
         * @param packages A list of package names representing the overlays to be disabled.
         *
         * @throws RemoteException if there is an error communicating with the remote service responsible for managing overlays.
         */
        @Throws(RemoteException::class)
        override fun disableOverlay(packages: List<String>) {
            packages.forEach { p ->
                switchOverlay(p, false)
            }
        }

        /**
         * Requests that the specified overlay packages be disabled when possible.
         *
         * This function iterates through the provided list of package names,
         * generates an overlay identifier for each package, and then attempts to
         * disable the overlay associated with that identifier.
         *
         * @param packages A list of package names representing the overlays to disable.
         * @throws RemoteException If a remote exception occurs during the operation.
         */
        @Throws(RemoteException::class)
        override fun disableOverlayWithIdentifier(packages: List<String>) {
            packages.forEach { p ->
                val identifier = generateOverlayIdentifier(p)
                switchOverlayWithIdentifier(identifier, false)
            }
        }

        /**
         * Registers the fabricated overlay with the overlay manager so it can be enabled and
         * disabled for any user.
         *
         *
         * The fabricated overlay is initialized in a disabled state. If an overlay is re-registered
         * the existing overlay will be replaced by the newly registered overlay and the enabled
         * state of the overlay will be left unchanged if the target package and target overlayable
         * have not changed.
         *
         * @param overlay the overlay to register with the overlay manager
         */
        @Suppress("DEPRECATION")
        @Throws(RemoteException::class)
        override fun registerFabricatedOverlay(overlay: FabricatedOverlayResource) {
            try {
                val fobInstance = fobClass.getConstructor(
                    String::class.java,
                    String::class.java,
                    String::class.java
                ).newInstance(
                    overlay.sourcePackage,
                    overlay.overlayName,
                    overlay.targetPackage
                )

                val setResourceValueMethod = fobClass.getMethod(
                    "setResourceValue",
                    String::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )

                val isA14orHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                var setResourceValueMethodWithConfig: Method? = null

                if (isA14orHigher) {
                    setResourceValueMethodWithConfig = fobClass.getMethod(
                        "setResourceValue",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        Int::class.javaPrimitiveType,
                        String::class.java
                    )
                }

                for ((_, overlayEntry) in overlay.getEntries()) {
                    if (isA14orHigher &&
                        overlayEntry.getConfiguration() != null &&
                        setResourceValueMethodWithConfig != null
                    ) {
                        setResourceValueMethodWithConfig.invoke(
                            fobInstance,
                            overlayEntry.resourceName,
                            overlayEntry.resourceType,
                            overlayEntry.resourceValue,
                            overlayEntry.getConfiguration()
                        )
                    } else {
                        setResourceValueMethod.invoke(
                            fobInstance,
                            overlayEntry.resourceName,
                            overlayEntry.resourceType,
                            overlayEntry.resourceValue
                        )
                    }
                }

                val foInstance = fobClass.getMethod(
                    "build"
                ).invoke(fobInstance)

                val omtbInstance = omtbClass.newInstance()

                omtbClass.getMethod(
                    "registerFabricatedOverlay",
                    foClass
                ).invoke(
                    omtbInstance,
                    foInstance
                )

                val omtInstance = omtbClass.getMethod(
                    "build"
                ).invoke(omtbInstance)

                commit(omtInstance)
            } catch (e: Exception) {
                Log.e(TAG, "registerFabricatedOverlay: ", e)
            }
        }

        /**
         * Disables and removes a fabricated overlay from the overlay manager for all users.
         *
         * This function unregisters a previously registered fabricated overlay, effectively disabling
         * and removing it from the system. Fabricated overlays are created dynamically and are not
         * part of the standard APK installation.
         *
         * The function first generates an overlay identifier based on the provided package name.
         * It then uses reflection to access the hidden OverlayManager (OM) API to unregister
         * the overlay. Finally, it commits the changes to the OM.
         *
         * @param packageName The package name used to identify the fabricated overlay to be unregistered.
         *
         * @throws RemoteException If there is an error communicating with the OverlayManagerService.
         */
        @Suppress("DEPRECATION")
        @Throws(RemoteException::class)
        override fun unregisterFabricatedOverlay(packageName: String) {
            try {
                val overlay = generateOverlayIdentifier(packageName) ?: return

                val omtbInstance = omtbClass.newInstance()

                omtbClass.getMethod(
                    "unregisterFabricatedOverlay",
                    oiClass
                ).invoke(
                    omtbInstance,
                    overlay
                )

                val omtInstance = omtbClass.getMethod(
                    "build"
                ).invoke(omtbInstance)

                commit(omtInstance)
            } catch (e: Exception) {
                Log.e(TAG, "unregisterFabricatedOverlay: ", e)
            }
        }

        /**
         * Changes the priority of the given overlay to the highest priority relative to
         * other overlays targeting the same resources and belonging to the same user.
         *
         * This method iterates through all user profiles, including managed profiles,
         * and attempts to set the overlay as the highest priority for each profile.
         * It then sets the overlay as the highest priority for the current user.
         *
         * @param packageName The package name of the overlay.
         * @return `true` if the priority was successfully changed, `false` otherwise.
         * @throws RemoteException if a remote exception occurs during the operation.
         */
        @Throws(RemoteException::class)
        override fun setHighestPriority(packageName: String): Boolean {
            var result = false
            var currentUserListed = false
            val profiles = mUserManager.getProfiles(currentUserId, true)

            profiles.forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        val userId = userInfo.userHandle.getUserIdentifier()
                        val tempResult = mOverlayManager.setHighestPriority(
                            packageName,
                            userId
                        )
                        if (userId == currentUserId) {
                            currentUserListed = true
                            result = tempResult
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error setting overlay priority for user ${userInfo.userHandle}",
                        e
                    )
                }
            }

            if (!currentUserListed) {
                result = mOverlayManager.setHighestPriority(packageName, currentUserId)
            }

            return result
        }

        /**
         * Changes the priority of the given overlay to the lowest priority relative to
         * other overlays targeting the same resources and belonging to the same user.
         *
         * This method iterates through all user profiles, including managed profiles,
         * and attempts to set the lowest priority for the given overlay package within each profile.
         * If an exception occurs during the process for a specific profile, it is caught and printed to the stack trace,
         * but the execution continues for other profiles.
         *
         * @param packageName The package name of the overlay to modify.
         * @return `true` if the priority was successfully changed for the current user, `false` otherwise.
         *
         * @throws RemoteException If a remote communication error occurs.
         */
        @Throws(RemoteException::class)
        override fun setLowestPriority(packageName: String): Boolean {
            var result = false
            var currentUserListed = false
            val profiles = mUserManager.getProfiles(currentUserId, true)

            profiles.forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        val userId = userInfo.userHandle.getUserIdentifier()
                        val tempResult = mOverlayManager.setLowestPriority(
                            packageName,
                            userId
                        )
                        if (userId == currentUserId) {
                            currentUserListed = true
                            result = tempResult
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error setting overlay priority for user ${userInfo.userHandle}",
                        e
                    )
                }
            }

            if (!currentUserListed) {
                result = mOverlayManager.setLowestPriority(packageName, currentUserId)
            }

            return result
        }

        /**
         * Generates an OverlayIdentifier for the given package name.
         *
         * This method generates an OverlayIdentifier for the specified package name, using the
         * default fabricated overlay source package.
         *
         * @param packageName The package name for which to generate the OverlayIdentifier.
         * @return An OverlayIdentifier for the specified package name, or null if an error occurs.
         * @throws RemoteException If a remote exception occurs during the operation.
         */
        @Throws(RemoteException::class)
        override fun generateOverlayIdentifier(packageName: String): OverlayIdentifier? {
            return generateOverlayIdentifier(packageName, FABRICATED_OVERLAY_SOURCE_PACKAGE)
        }

        /**
         * Invalidates the caches for an overlay package for all user profiles associated with the current user.
         *
         * This method iterates through all user profiles, including the main user and any managed profiles,
         * and calls [IOverlayManager.invalidateCachesForOverlay] for each profile to invalidate the caches
         * for the specified overlay package.
         *
         * @param packageName The package name of the overlay for which to invalidate caches.
         *
         * @throws RemoteException if there is a communication error with the system service.
         */
        @Throws(RemoteException::class)
        override fun invalidateCachesForOverlay(packageName: String) {
            var currentUserListed = false
            val profiles = mUserManager.getProfiles(currentUserId, true)

            profiles.forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        val userId = userInfo.userHandle.getUserIdentifier()
                        mOverlayManager.invalidateCachesForOverlay(
                            packageName,
                            userId
                        )
                        if (userId == currentUserId) {
                            currentUserListed = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error invalidating overlay caches for user ${userInfo.userHandle}",
                        e
                    )
                }
            }

            if (!currentUserListed) {
                mOverlayManager.invalidateCachesForOverlay(packageName, currentUserId)
            }
        }

        private fun switchOverlay(packageName: String, enable: Boolean) {
            var currentUserListed = false
            val profiles = mUserManager.getProfiles(currentUserId, true)

            profiles.forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        val userId = userInfo.userHandle.getUserIdentifier()
                        mOverlayManager.setEnabled(
                            packageName,
                            enable,
                            userId
                        )
                        if (userId == currentUserId) {
                            currentUserListed = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error setting overlay ${if (enable) "enabled" else "disabled"} for user ${userInfo.userHandle}",
                        e
                    )
                }
            }

            if (!currentUserListed) {
                mOverlayManager.setEnabled(packageName, enable, currentUserId)
            }
        }

        @Suppress("DEPRECATION")
        private fun switchOverlayWithIdentifier(identifier: OverlayIdentifier?, enable: Boolean) {
            try {
                val omtbInstance = omtbClass.newInstance()

                val setEnabledMethod = omtbClass.getMethod(
                    "setEnabled",
                    oiClass,
                    Boolean::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )

                var currentUserListed = false
                val profiles = mUserManager.getProfiles(currentUserId, true)

                profiles.forEach { userInfo ->
                    try {
                        if (userInfo.isProfile) {
                            val userId = userInfo.userHandle.getUserIdentifier()
                            setEnabledMethod.invoke(
                                omtbInstance,
                                identifier,
                                enable,
                                userId
                            )
                            if (userId == currentUserId) {
                                currentUserListed = true
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "Error setting overlay ${if (enable) "enabled" else "disabled"} for user ${userInfo.userHandle}",
                            e
                        )
                    }
                }

                if (!currentUserListed) {
                    setEnabledMethod.invoke(
                        omtbInstance,
                        identifier,
                        enable,
                        currentUserId
                    )
                }

                val omtInstance = omtbClass.getMethod(
                    "build"
                ).invoke(omtbInstance)

                commit(omtInstance)
            } catch (e: Exception) {
                Log.e(TAG, "switchOverlayWithIdentifier: ", e)
            }
        }

        /**
         * Uninstalls any overlay updates for the given package name.
         *
         * This function executes the `pm uninstall` command to remove any updates applied
         * to the overlay package specified by `packageName`. This effectively reverts
         * the overlay to its original state.
         *
         * @param packageName The name of the package for which to uninstall overlay updates.
         */
        override fun uninstallOverlayUpdates(packageName: String) {
            runCommand(listOf("pm uninstall $packageName"))
        }

        /**
         * Restarts the System UI (SystemUI) process immediately.
         *
         * This function executes a command to kill the SystemUI process, which will
         * cause Android to automatically restart it. This can be useful for applying
         * changes to SystemUI without requiring a full device reboot.
         *
         * @throws RemoteException if there is an error communicating with the remote service.
         */
        @Throws(RemoteException::class)
        override fun restartSystemUI() {
            runCommand(listOf("killall com.android.systemui"))
        }

        /**
         * Executes a list of commands with root privileges.
         *
         * This function utilizes the `Shell` utility to execute the provided commands as root.
         * The output of the command execution is captured and returned as an array of strings.
         *
         * @param command A list of strings representing the command and its arguments.
         * @return An array of strings containing the output of the command execution.
         */
        override fun runCommand(command: List<String>): Array<String> {
            return Shell.cmd(*command.toTypedArray<String>()).exec().out.toTypedArray<String>()
        }

        @SuppressLint("NewApi")
        @Throws(Exception::class)
        private fun commit(transaction: Any?) {
            mOverlayManager.commit(transaction as OverlayManagerTransaction?)
        }

        @SuppressLint("PrivateApi", "NewApi", "StaticFieldLeak", "RestrictedApi")
        companion object {
            private val TAG: String = RootConnectionImpl::class.java.simpleName

            private val currentUserId: Int
                get() = Process.myUserHandle().getUserIdentifier()

            private val SystemUI_UID: Int
                get() = try {
                    Utils.getContext().packageManager.getPackageUid(SYSTEMUI_PACKAGE, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.e(TAG, "SystemUI_UID", e)
                    -1
                }

            private val mUserManager: IUserManager by lazy {
                IUserManager.Stub.asInterface(
                    SystemServiceHelper.getSystemService("user")
                )
            }

            private val mOverlayManager: IOverlayManager by lazy {
                IOverlayManager.Stub.asInterface(
                    SystemServiceHelper.getSystemService("overlay")
                )
            }

            private val mActivityManager: IActivityManager by lazy {
                IActivityManager.Stub.asInterface(
                    SystemServiceHelper.getSystemService("activity")
                )
            }

            private val oiClass: Class<*> by lazy {
                try {
                    Class.forName("android.content.om.OverlayIdentifier")
                } catch (e: ClassNotFoundException) {
                    Log.e(TAG, "OverlayIdentifier class not found", e)
                    throw RuntimeException(e)
                }
            }

            private val foClass: Class<*> by lazy {
                try {
                    Class.forName("android.content.om.FabricatedOverlay")
                } catch (e: ClassNotFoundException) {
                    Log.e(TAG, "FabricatedOverlay class not found", e)
                    throw RuntimeException(e)
                }
            }

            private val fobClass: Class<*> by lazy {
                try {
                    Class.forName("android.content.om.FabricatedOverlay\$Builder")
                } catch (e: ClassNotFoundException) {
                    Log.e(TAG, "FabricatedOverlay.Builder class not found", e)
                    throw RuntimeException(e)
                }
            }

            private val omtbClass: Class<*> by lazy {
                try {
                    Class.forName("android.content.om.OverlayManagerTransaction\$Builder")
                } catch (e: ClassNotFoundException) {
                    Log.e(TAG, "OverlayManagerTransaction.Builder class not found", e)
                    throw RuntimeException(e)
                }
            }

            @Suppress("SameParameterValue")
            private fun generateOverlayIdentifier(
                packageName: String,
                sourcePackage: String
            ): OverlayIdentifier? {
                try {
                    return oiClass.getConstructor(
                        String::class.java,
                        String::class.java
                    ).newInstance(
                        sourcePackage,
                        packageName
                    ) as OverlayIdentifier
                } catch (e: Exception) {
                    Log.e(TAG, "generateOverlayIdentifier: ", e)
                    return null
                }
            }

            private fun UserHandle.getUserIdentifier(): Int {
                val method = UserHandle::class.java.getDeclaredMethod("getIdentifier")
                method.isAccessible = true
                return method.invoke(this) as Int
            }
        }
    }
}
