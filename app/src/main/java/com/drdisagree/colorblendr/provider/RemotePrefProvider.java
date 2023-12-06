package com.drdisagree.colorblendr.provider;

import com.crossbowffs.remotepreferences.RemotePreferenceFile;
import com.crossbowffs.remotepreferences.RemotePreferenceProvider;
import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.common.Const;

public class RemotePrefProvider extends RemotePreferenceProvider {

    public RemotePrefProvider() {
        super(BuildConfig.APPLICATION_ID, new RemotePreferenceFile[]{new RemotePreferenceFile(Const.SharedPrefs, true)});
    }
}