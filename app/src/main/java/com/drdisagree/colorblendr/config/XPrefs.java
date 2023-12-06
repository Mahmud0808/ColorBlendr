package com.drdisagree.colorblendr.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.common.Const;
import com.drdisagree.colorblendr.xposed.HookEntry;
import com.drdisagree.colorblendr.xposed.ModPack;

public class XPrefs {

    private static final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> loadEverything(key);
    public static SharedPreferences Xprefs;
    private static String packageName;

    public static void init(Context context) {
        packageName = context.getPackageName();
        Xprefs = new RemotePreferences(context, BuildConfig.APPLICATION_ID, Const.SharedPrefs, true);
        Xprefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void loadEverything(String... key) {
        if (key.length > 0 && (key[0] == null || Const.PREF_UPDATE_EXCLUSIONS.stream().anyMatch(exclusion -> key[0].startsWith(exclusion))))
            return;

        for (ModPack thisMod : HookEntry.runningMods) {
            thisMod.updatePrefs(key);
        }
    }
}