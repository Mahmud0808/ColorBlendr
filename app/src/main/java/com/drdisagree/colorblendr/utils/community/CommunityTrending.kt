package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.models.CommunityTheme
import kotlin.math.pow

// Gravity score: engagement decays with age, so fresh themes gaining votes
// outrank old winners camping the top. Shared by browse sort + home showcase.
object CommunityTrending {

    private const val DOWNLOAD_WEIGHT = 0.5
    private const val AGE_OFFSET_DAYS = 2.0
    private const val GRAVITY = 1.5

    // Score once per theme, then sort - a selector inside sortedByDescending
    // runs pow() on every comparison (O(n log n) instead of O(n)).
    fun sort(
        themes: List<CommunityTheme>,
        nowSeconds: Long = System.currentTimeMillis() / 1000
    ): List<CommunityTheme> = themes
        .map { it to score(it, nowSeconds) }
        .sortedByDescending { it.second }
        .map { it.first }

    // Top N without sorting the whole list: single pass keeping the best
    // `count` scores. O(n * count) with count ~10, beats O(n log n) on a big
    // cache and allocates one small array.
    fun top(
        themes: List<CommunityTheme>,
        count: Int,
        nowSeconds: Long = System.currentTimeMillis() / 1000
    ): List<CommunityTheme> {
        if (count <= 0) return emptyList()
        if (themes.size <= count) return sort(themes, nowSeconds)

        val best = ArrayList<Pair<CommunityTheme, Double>>(count + 1)
        themes.forEach { theme ->
            val score = score(theme, nowSeconds)
            if (best.size == count && score <= best.last().second) return@forEach
            var index = best.binarySearch { -it.second.compareTo(score) }
            if (index < 0) index = -index - 1
            best.add(index, theme to score)
            if (best.size > count) best.removeAt(best.size - 1)
        }
        return best.map { it.first }
    }

    private fun score(theme: CommunityTheme, nowSeconds: Long): Double {
        val ageDays = (nowSeconds - theme.createdAt).coerceAtLeast(0) / 86400.0
        return (theme.upvotes + theme.downloads * DOWNLOAD_WEIGHT) /
                (ageDays + AGE_OFFSET_DAYS).pow(GRAVITY)
    }
}