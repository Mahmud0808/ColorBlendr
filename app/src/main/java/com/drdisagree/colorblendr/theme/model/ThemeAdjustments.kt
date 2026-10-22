package com.drdisagree.colorblendr.theme.model

data class ThemeAdjustments(
    val accentSaturation: Int = NEUTRAL_LEVEL,
    val backgroundSaturation: Int = NEUTRAL_LEVEL,
    val backgroundLightness: Int = NEUTRAL_LEVEL,
    val accurateShades: Boolean = true,
    val pitchBlack: Boolean = false,
    val pitchBlackContainers: Boolean = false,
    val tintedText: Boolean = true,
    val shadeOverrides: Map<String, Int> = emptyMap()
) {
    fun shadeOverridesFor(role: PaletteRole): Map<Int, Int> = buildMap {
        ShadeScale.indices.forEach { index ->
            shadeOverrides[role.shadeName(index)]?.let { put(index, it) }
        }
    }

    companion object {
        const val NEUTRAL_LEVEL = 100
    }
}
