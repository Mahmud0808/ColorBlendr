package com.drdisagree.colorblendr.data.enums

enum class ContrastLevel(val value: Double) {
    STANDARD(0.0),
    MEDIUM(0.5),
    HIGH(1.0);

    companion object {
        fun fromString(name: String?): ContrastLevel = entries.find { it.name == name } ?: STANDARD
    }
}
