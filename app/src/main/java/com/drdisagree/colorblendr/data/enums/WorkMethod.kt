package com.drdisagree.colorblendr.data.enums

enum class WorkMethod {
    NULL,
    ROOT,
    SHIZUKU,
    WIRELESS_ADB,
    NULLGATE;

    companion object {
        fun fromString(str: String?): WorkMethod {
            return try {
                valueOf(str!!)
            } catch (_: Exception) {
                NULL
            }
        }
    }
}
