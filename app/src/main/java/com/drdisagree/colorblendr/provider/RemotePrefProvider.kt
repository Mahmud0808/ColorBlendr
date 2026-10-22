package com.drdisagree.colorblendr.provider

import com.crossbowffs.remotepreferences.RemotePreferenceFile
import com.crossbowffs.remotepreferences.RemotePreferenceProvider
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.data.common.Constant

class RemotePrefProvider : RemotePreferenceProvider(
    BuildConfig.APPLICATION_ID,
    arrayOf(RemotePreferenceFile(Constant.SHARED_PREFS, true))
) {
    override fun checkAccess(prefFileName: String, prefKey: String, write: Boolean): Boolean {
        return !write
    }
}
