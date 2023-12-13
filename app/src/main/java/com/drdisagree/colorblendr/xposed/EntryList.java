package com.drdisagree.colorblendr.xposed;

import static com.drdisagree.colorblendr.common.Const.SYSTEMUI_PACKAGE;

import com.drdisagree.colorblendr.xposed.modules.MonetColors;

import java.util.ArrayList;

public class EntryList {

    public static ArrayList<Class<? extends ModPack>> getEntries(String packageName) {
        ArrayList<Class<? extends ModPack>> modPacks = new ArrayList<>();

        if (packageName.equals(SYSTEMUI_PACKAGE)) {
            if (!HookEntry.isChildProcess) {
                modPacks.add(MonetColors.class);
            }
        }

        return modPacks;
    }
}
