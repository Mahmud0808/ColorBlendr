package com.drdisagree.colorblendr.config;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.drdisagree.colorblendr.BuildConfig;
import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;

@SuppressWarnings("unused")
public class XPrefs {

    public static SharedPreferences prefs = ColorBlendr.getAppContext().getSharedPreferences(Const.SharedPrefs, MODE_PRIVATE);
    public static SharedPreferences Xprefs = new RemotePreferences(ColorBlendr.getAppContext(), BuildConfig.APPLICATION_ID, Const.SharedPrefs, true);
    static SharedPreferences.Editor editor = prefs.edit();

    public static void putBoolean(String key, boolean val) {
        editor.putBoolean(key, val).apply();
    }

    public static void putInt(String key, int val) {
        editor.putInt(key, val).apply();
    }

    public static void putLong(String key, long val) {
        editor.putLong(key, val).apply();
    }

    public static void putString(String key, String val) {
        editor.putString(key, val).apply();
    }

    public static boolean getBoolean(String key) {
        return prefs.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, Boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public static int getInt(String key) {
        return prefs.getInt(key, 0);
    }

    public static int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    public static long getLong(String key) {
        return prefs.getLong(key, 0);
    }

    public static long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    public static String getString(String key) {
        return prefs.getString(key, null);
    }

    public static String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    public static void clearPref(String key) {
        editor.remove(key).apply();
    }

    public static void clearPrefs(String... keys) {
        for (String key : keys) {
            editor.remove(key).apply();
        }
    }

    public static void clearAllPrefs() {
        editor.clear().apply();
    }
}
