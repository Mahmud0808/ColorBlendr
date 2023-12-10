package com.drdisagree.colorblendr.xposed;

import static com.drdisagree.colorblendr.common.Const.SYSTEMUI_PACKAGE;

import android.os.Build;

import com.drdisagree.colorblendr.xposed.modules.MonetColorsA12;
import com.drdisagree.colorblendr.xposed.modules.MonetColorsA13;
import com.drdisagree.colorblendr.xposed.modules.MonetColorsA14;

import java.util.ArrayList;

public class EntryList {

    public static ArrayList<Class<? extends ModPack>> getEntries(String packageName) {
        ArrayList<Class<? extends ModPack>> modPacks = new ArrayList<>();

        if (packageName.equals(SYSTEMUI_PACKAGE)) {
            if (!HookEntry.isChildProcess) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    modPacks.add(MonetColorsA12.class);
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
                    modPacks.add(MonetColorsA13.class);
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    modPacks.add(MonetColorsA14.class);
                }
            }
        }

        return modPacks;
    }
}
