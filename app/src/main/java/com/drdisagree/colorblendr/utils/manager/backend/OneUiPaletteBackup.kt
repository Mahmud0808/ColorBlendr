package com.drdisagree.colorblendr.utils.manager.backend

import org.json.JSONObject

data class OneUiPaletteBackup(
    val state: String?,
    val colors: String?,
    val googleColors: String?,
    val isGray: String?,
    val themeCustomization: String?
) {

    fun toJson(): String = JSONObject()
        .put(KEY_STATE, state)
        .put(KEY_COLORS, colors)
        .put(KEY_GOOGLE_COLORS, googleColors)
        .put(KEY_IS_GRAY, isGray)
        .put(KEY_THEME_CUSTOMIZATION, themeCustomization)
        .toString()

    companion object {
        private const val KEY_STATE = "state"
        private const val KEY_COLORS = "colors"
        private const val KEY_GOOGLE_COLORS = "googleColors"
        private const val KEY_IS_GRAY = "isGray"
        private const val KEY_THEME_CUSTOMIZATION = "themeCustomization"

        fun fromJson(json: String): OneUiPaletteBackup? = runCatching {
            val obj = JSONObject(json)
            fun JSONObject.stringOrNull(key: String): String? =
                if (isNull(key)) null else getString(key)
            OneUiPaletteBackup(
                state = obj.stringOrNull(KEY_STATE),
                colors = obj.stringOrNull(KEY_COLORS),
                googleColors = obj.stringOrNull(KEY_GOOGLE_COLORS),
                isGray = obj.stringOrNull(KEY_IS_GRAY),
                themeCustomization = obj.stringOrNull(KEY_THEME_CUSTOMIZATION)
            )
        }.getOrNull()
    }
}
