package com.drdisagree.colorblendr.utils

import com.drdisagree.colorblendr.common.Const.COLOR_THEME_APP_ICON
import com.drdisagree.colorblendr.common.Const.LOCK_ADAPTIVE_COLOR
import com.drdisagree.colorblendr.common.Const.THEME_CUSTOMIZATION_OVERLAY_PACKAGES
import com.drdisagree.colorblendr.common.Const.WALLPAPER_THEME_COLORS
import com.drdisagree.colorblendr.common.Const.WALLPAPER_THEME_COLORS_FOR_GOOGLE
import com.drdisagree.colorblendr.common.Const.WALLPAPER_THEME_COLOR_IS_GRAY
import com.drdisagree.colorblendr.common.Const.WALLPAPER_THEME_STATE
import com.topjohnwu.superuser.Shell

object SamsungPalette {

    fun applySystemColors(jsonString: String, paletteArray: String) {
        Shell.cmd(
            "settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '$jsonString'",
            "settings put system $WALLPAPER_THEME_STATE '1'",
            "settings put system $LOCK_ADAPTIVE_COLOR '3'",
            "settings put system $WALLPAPER_THEME_COLORS '$paletteArray'",
            "settings put system $WALLPAPER_THEME_COLORS_FOR_GOOGLE '$paletteArray'",
            "settings put system $WALLPAPER_THEME_COLOR_IS_GRAY '0'"
        ).exec()
    }

    val isThemedIconEnabled: Boolean
        get() = Shell.cmd(
            "settings get system $COLOR_THEME_APP_ICON"
        ).exec().out[0] == "1"

    fun enableThemedIcon(enabled: Boolean) {
        Shell.cmd(
            "settings put system $COLOR_THEME_APP_ICON '${if (enabled) "1" else "0"}'"
        ).exec()
    }

    fun removeSystemColors() {
        Shell.cmd(
            "settings put secure $THEME_CUSTOMIZATION_OVERLAY_PACKAGES '${SystemPalette.originalSettings}'",
            "settings put system $WALLPAPER_THEME_STATE '0'",
            "settings put system $LOCK_ADAPTIVE_COLOR '3'",
            "settings put system $WALLPAPER_THEME_COLORS ''",
            "settings put system $WALLPAPER_THEME_COLORS_FOR_GOOGLE ''"
        ).exec()
    }
}