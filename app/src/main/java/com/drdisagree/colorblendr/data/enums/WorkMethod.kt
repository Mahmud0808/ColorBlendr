package com.drdisagree.colorblendr.data.enums

enum class WorkMethod {
    NULL,
    ROOT,
    SHIZUKU;

    companion object {
        fun fromString(str: String?): WorkMethod {
            return try {
                valueOf(str!!)
            } catch (e: Exception) {
                NULL
            }
        }
    }
}