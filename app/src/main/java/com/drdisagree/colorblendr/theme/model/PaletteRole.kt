package com.drdisagree.colorblendr.theme.model

enum class PaletteRole(val systemName: String, val referenceName: String) {
    PRIMARY("system_accent1", "primary"),
    SECONDARY("system_accent2", "secondary"),
    TERTIARY("system_accent3", "tertiary"),
    NEUTRAL("system_neutral1", "neutral"),
    NEUTRAL_VARIANT("system_neutral2", "neutral_variant"),
    ERROR("system_error", "error");

    val isAccent: Boolean
        get() = this == PRIMARY || this == SECONDARY || this == TERTIARY

    val isNeutral: Boolean
        get() = this == NEUTRAL || this == NEUTRAL_VARIANT

    fun shadeName(index: Int): String = "${systemName}_${ShadeScale.shades[index]}"

    companion object {
        val shadeNames: List<List<String>> by lazy {
            entries.map { role -> ShadeScale.indices.map(role::shadeName) }
        }
    }
}
