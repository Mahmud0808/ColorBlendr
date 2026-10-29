package com.drdisagree.colorblendr.utils.community

import java.util.Locale

// Vote / download counts shortened past 1000: 1K, 1.1K, 12.3K, 1.4M.
// Truncates, never rounds up -> count never reads higher than it is.
object CommunityCount {

    fun compact(value: Int): String = when {
        value < 1_000 -> value.toString()
        value < 1_000_000 -> scaled(value, 1_000, "K")
        else -> scaled(value, 1_000_000, "M")
    }

    private fun scaled(value: Int, divisor: Int, suffix: String): String {
        val tenths = value / (divisor / 10)

        return if (tenths % 10 == 0) {
            "${tenths / 10}$suffix"
        } else {
            String.format(Locale.getDefault(), "%.1f%s", tenths / 10.0, suffix)
        }
    }
}