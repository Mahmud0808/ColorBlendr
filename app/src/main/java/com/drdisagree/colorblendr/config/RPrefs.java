package com.drdisagree.colorblendr.config;

import static android.content.Context.MODE_PRIVATE;
import static com.drdisagree.colorblendr.common.Const.EXCLUDED_PREFS_FROM_BACKUP;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.drdisagree.colorblendr.ColorBlendr;
import com.drdisagree.colorblendr.common.Const;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class RPrefs {

    private static final String TAG = RPrefs.class.getSimpleName();

    public static SharedPreferences prefs = ColorBlendr.getAppContext().createDeviceProtectedStorageContext().getSharedPreferences(Const.SharedPrefs, MODE_PRIVATE);
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

    public static void putFloat(String key, float val) {
        editor.putFloat(key, val).apply();
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

    public static float getFloat(String key) {
        return prefs.getFloat(key, 0);
    }

    public static float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
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

    public static void backupPrefs(final @NonNull OutputStream outputStream) {
        try (outputStream; ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(prefs.getAll());
        } catch (IOException e) {
            Log.e(TAG, "Error serializing preferences", e);
        }

        try (outputStream; ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            Map<String, ?> allPrefs = prefs.getAll();

            for (String excludedPref : EXCLUDED_PREFS_FROM_BACKUP) {
                allPrefs.remove(excludedPref);
            }

            objectOutputStream.writeObject(allPrefs);
        } catch (IOException e) {
            Log.e(TAG, "Error serializing preferences", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void restorePrefs(final @NonNull InputStream inputStream) {
        Map<String, Object> map;
        try (inputStream; ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            map = (Map<String, Object>) objectInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            Log.e(TAG, "Error deserializing preferences", e);
            return;
        }

        // Retrieve excluded prefs from current prefs
        Map<String, Object> excludedPrefs = new HashMap<>();
        for (String excludedPref : EXCLUDED_PREFS_FROM_BACKUP) {
            Object prefValue = prefs.getAll().get(excludedPref);
            if (prefValue != null) {
                excludedPrefs.put(excludedPref, prefValue);
            }
        }

        editor.clear();

        // Restore excluded prefs
        for (Map.Entry<String, Object> entry : excludedPrefs.entrySet()) {
            putObject(entry.getKey(), entry.getValue());
        }

        // Restore non-excluded prefs
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (EXCLUDED_PREFS_FROM_BACKUP.contains(entry.getKey())) {
                continue;
            }

            putObject(entry.getKey(), entry.getValue());
        }

        editor.apply();
    }

    private static void putObject(String key, Object value) {
        if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else {
            throw new IllegalArgumentException("Type " + value.getClass().getName() + " is unknown");
        }
    }
}
