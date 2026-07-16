package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.common.Constant.CUSTOM_MONET_STYLE
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCENT_SATURATION_DARK
import com.drdisagree.colorblendr.data.common.Constant.MONET_ACCENT_SATURATION_LIGHT
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_LIGHTNESS_DARK
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_LIGHTNESS_LIGHT
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_SATURATION_DARK
import com.drdisagree.colorblendr.data.common.Constant.MONET_BACKGROUND_SATURATION_LIGHT
import com.drdisagree.colorblendr.data.common.Utilities.setAccurateShadesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setColorSpecVersion
import com.drdisagree.colorblendr.data.common.Utilities.setCurrentMonetStyle
import com.drdisagree.colorblendr.data.common.Utilities.setCustomColorEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setModeSpecificThemesEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setPitchBlackThemeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.setSecondaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setSeedColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setTertiaryColorValue
import com.drdisagree.colorblendr.data.common.Utilities.setTintedTextEnabled
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.domain.PreviewController
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import android.graphics.Color as AndroidColor

// Maps a community theme onto the staged prefs and starts a live preview —
// same path user selections take, so apply/discard FABs and commit behave
// exactly like local edits. Root-only callers gate this.
object CommunityThemeApplier {

    // Creation staged for preview; consumed when the preview is committed so
    // the apply gets counted, cleared on discard.
    @Volatile
    var pendingCreationId: String? = null

    fun stageForPreview(theme: CommunityTheme) {
        pendingCreationId = theme.id.takeUnless { it == TestThemeHolder.TEST_THEME_ID }
        PreviewController.beginPreview()

        // Community themes replace any selected custom style.
        Prefs.clearPref(CUSTOM_MONET_STYLE)

        setCurrentMonetStyle(theme.style)
        setCustomColorEnabled(true)
        setSeedColorValue(theme.seedColor)
        // WHITE = "not set" sentinel for secondary/tertiary.
        setSecondaryColorValue(theme.secondaryColor ?: AndroidColor.WHITE)
        setTertiaryColorValue(theme.tertiaryColor ?: AndroidColor.WHITE)
        // Raw keys: setters write the CURRENT mode's key, payload is explicit.
        Prefs.putInt(MONET_ACCENT_SATURATION_DARK, theme.accentSaturation)
        Prefs.putInt(MONET_BACKGROUND_SATURATION_DARK, theme.backgroundSaturation)
        Prefs.putInt(MONET_BACKGROUND_LIGHTNESS_DARK, theme.backgroundLightness)
        setModeSpecificThemesEnabled(theme.modeSpecificThemes)
        if (theme.modeSpecificThemes) {
            Prefs.putInt(
                MONET_ACCENT_SATURATION_LIGHT, theme.accentSaturationLight
            )
            Prefs.putInt(
                MONET_BACKGROUND_SATURATION_LIGHT,
                theme.backgroundSaturationLight
            )
            Prefs.putInt(
                MONET_BACKGROUND_LIGHTNESS_LIGHT,
                theme.backgroundLightnessLight
            )
        } else {
            Prefs.clearPref(MONET_ACCENT_SATURATION_LIGHT)
            Prefs.clearPref(MONET_BACKGROUND_SATURATION_LIGHT)
            Prefs.clearPref(MONET_BACKGROUND_LIGHTNESS_LIGHT)
        }
        setAccurateShadesEnabled(theme.accurateShades)
        setColorSpecVersion(theme.colorSpecVersion)
        setPitchBlackThemeEnabled(theme.pitchBlack)
        setTintedTextEnabled(theme.tintText)

        systemPaletteNames.forEach { row ->
            row.forEach { shadeName ->
                val override = theme.colorOverrides[shadeName]
                if (override != null) {
                    Prefs.putInt(shadeName, override)
                } else {
                    Prefs.clearPref(shadeName)
                }
            }
        }
    }
}
