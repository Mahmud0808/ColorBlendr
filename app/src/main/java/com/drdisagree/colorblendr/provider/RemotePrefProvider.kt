package com.drdisagree.colorblendr.provider

import com.crossbowffs.remotepreferences.RemotePreferenceFile
import com.crossbowffs.remotepreferences.RemotePreferenceProvider
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.data.common.Const

class RemotePrefProvider : RemotePreferenceProvider(
    BuildConfig.APPLICATION_ID,
    arrayOf(RemotePreferenceFile(Const.SHARED_PREFS, true))
)