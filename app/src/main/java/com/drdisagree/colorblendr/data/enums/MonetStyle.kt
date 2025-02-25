package com.drdisagree.colorblendr.data.enums

enum class MONET {
    SPRITZ,
    MONOCHROMATIC,
    TONAL_SPOT,
    VIBRANT,
    RAINBOW,
    EXPRESSIVE,
    FIDELITY,
    CONTENT,
    FRUIT_SALAD;

    override fun toString(): String {
        return name
    }

    companion object {
        fun String?.toEnumMonet(): MONET {
            return entries.find { it.name.equals(this, ignoreCase = true) } ?: TONAL_SPOT
        }
    }
}