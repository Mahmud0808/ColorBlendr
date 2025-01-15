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
import com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_NAME_SYSTEM
import com.drdisagree.colorblendr.common.Const.FABRICATED_OVERLAY_SOURCE_PACKAGE
import com.drdisagree.colorblendr.common.Const.SYSTEMUI_PACKAGE
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
                            enableOverlayWithIdentifier(
                                listOf(
                                    FABRICATED_OVERLAY_NAME_SYSTEM
                                )
                            )
                        } catch (ignored: RemoteException) {
                            // Overlay was never registered
                        }
                    }, 3000)
                }
            }
        }

        @Throws(RemoteException::class)
        override fun isRooted(): Boolean {
            return Shell.isAppGrantedRoot()!!
        }

        /**
         * Listener to notify when SystemUI restarts.
         */
        @Throws(RemoteException::class)
        override fun setSystemUIRestartListener() {
            mActivityManager.registerProcessObserver(processListener)
        }

        /**
         * Return true if an overlay package is installed.
         */
        @Throws(RemoteException::class)
        override fun isOverlayInstalled(packageName: String): Boolean {
            return mOverlayManager.getOverlayInfo(packageName, currentUserId) != null
        }

        /**
         * Return true if an overlay package is enabled.
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
                e.printStackTrace()
                return false
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
                return false
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                return false
            }
        }

        /**
         * Request that an overlay package be enabled when possible to do so.
         */
        override fun enableOverlay(packages: List<String>) {
            packages.forEach { p ->
                switchOverlay(p, true)
            }
        }

        /**
         * Request that an overlay package be enabled when possible to do so.
         */
        @Throws(RemoteException::class)
        override fun enableOverlayWithIdentifier(packages: List<String>) {
            packages.forEach { p ->
                val identifier = generateOverlayIdentifier(p)
                switchOverlayWithIdentifier(identifier, true)
            }
        }

        /**
         * Request that an overlay package is enabled and any other overlay packages with the same
         * target package are disabled.
         */
        @Throws(RemoteException::class)
        override fun enableOverlayExclusive(packageName: String): Boolean {
            mUserManager.getProfiles(currentUserId, true).forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        mOverlayManager.setEnabledExclusive(
                            packageName,
                            true,
                            userInfo.userHandle.getIdentifier()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return mOverlayManager.setEnabledExclusive(packageName, true, currentUserId)
        }

        /**
         * Request that an overlay package is enabled and any other overlay packages with the same
         * target package and category are disabled.
         */
        @Throws(RemoteException::class)
        override fun enableOverlayExclusiveInCategory(packageName: String): Boolean {
            mUserManager.getProfiles(currentUserId, true).forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        mOverlayManager.setEnabledExclusiveInCategory(
                            packageName,
                            userInfo.userHandle.getIdentifier()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return mOverlayManager.setEnabledExclusiveInCategory(packageName, currentUserId)
        }

        /**
         * Request that an overlay package be disabled when possible to do so.
         */
        @Throws(RemoteException::class)
        override fun disableOverlay(packages: List<String>) {
            packages.forEach { p ->
                switchOverlay(p, false)
            }
        }

        /**
         * Request that an overlay package be disabled when possible to do so.
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
                    if (isA14orHigher && overlayEntry.getConfiguration() != null && setResourceValueMethodWithConfig != null) {
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
         * Disables and removes the overlay from the overlay manager for all users.
         *
         * @param packageName the package name of the overlay to disable and remove
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
         * Change the priority of the given overlay to the highest priority relative to
         * the other overlays with the same target and user.
         */
        @Throws(RemoteException::class)
        override fun setHighestPriority(packageName: String): Boolean {
            mUserManager.getProfiles(currentUserId, true).forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        mOverlayManager.setHighestPriority(
                            packageName,
                            userInfo.userHandle.getIdentifier()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return mOverlayManager.setHighestPriority(packageName, currentUserId)
        }

        /**
         * Change the priority of the given overlay to the lowest priority relative to
         * the other overlays with the same target and user.
         */
        @Throws(RemoteException::class)
        override fun setLowestPriority(packageName: String): Boolean {
            mUserManager.getProfiles(currentUserId, true).forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        mOverlayManager.setLowestPriority(
                            packageName,
                            userInfo.userHandle.getIdentifier()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return mOverlayManager.setLowestPriority(packageName, currentUserId)
        }

        @Throws(RemoteException::class)
        override fun generateOverlayIdentifier(packageName: String): OverlayIdentifier? {
            return generateOverlayIdentifier(packageName, FABRICATED_OVERLAY_SOURCE_PACKAGE)
        }

        @Throws(RemoteException::class)
        override fun invalidateCachesForOverlay(packageName: String) {
            mUserManager.getProfiles(currentUserId, true).forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        mOverlayManager.invalidateCachesForOverlay(
                            packageName,
                            userInfo.userHandle.getIdentifier()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            mOverlayManager.invalidateCachesForOverlay(packageName, currentUserId)
        }

        private fun switchOverlay(packageName: String, enable: Boolean) {
            mUserManager.getProfiles(currentUserId, true).forEach { userInfo ->
                try {
                    if (userInfo.isProfile) {
                        mOverlayManager.setEnabled(
                            packageName,
                            enable,
                            userInfo.userHandle.getIdentifier()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            mOverlayManager.setEnabled(packageName, enable, currentUserId)
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

                mUserManager.getProfiles(currentUserId, true).forEach { userInfo ->
                    try {
                        if (userInfo.isProfile) {
                            setEnabledMethod.invoke(
                                omtbInstance,
                                identifier,
                                enable,
                                userInfo.userHandle.getIdentifier()
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                setEnabledMethod.invoke(
                    omtbInstance,
                    identifier,
                    enable,
                    currentUserId
                )

                val omtInstance = omtbClass.getMethod(
                    "build"
                ).invoke(omtbInstance)

                commit(omtInstance)
            } catch (e: Exception) {
                Log.e(TAG, "switchOverlayWithIdentifier: ", e)
            }
        }

        /**
         * Uninstall any overlay updates for the given package name.
         */
        override fun uninstallOverlayUpdates(packageName: String) {
            runCommand(listOf("pm uninstall $packageName"))
        }

        /**
         * Restart systemui immediately.
         */
        @Throws(RemoteException::class)
        override fun restartSystemUI() {
            runCommand(listOf("killall com.android.systemui"))
        }

        /**
         * Run list of commands as root.
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
                get() = Process.myUserHandle().getIdentifier()

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

            private fun UserHandle.getIdentifier(): Int {
                val method = UserHandle::class.java.getDeclaredMethod("getIdentifier")
                method.isAccessible = true
                return method.invoke(this) as Int
            }
        }
    }
}
