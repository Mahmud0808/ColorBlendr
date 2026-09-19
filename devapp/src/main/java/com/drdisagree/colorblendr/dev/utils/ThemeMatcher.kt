package com.drdisagree.colorblendr.dev.utils

import androidx.annotation.ColorInt
import com.drdisagree.colorblendr.dev.data.models.ComparableTheme
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.data.models.ThemePayload
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min

data class MatchResult(
    val matchedTheme: ComparableTheme,
    val score: Float,
    val percentage: Int
)

object ThemeMatcher {

    const val MATCH_THRESHOLD = 0.85f

    private const val SETTINGS_FLOOR = 0.55f

    private const val SEED_WEIGHT = 0.7f
    private const val ACCENT_WEIGHT = 0.15f

    private const val HUE_WEIGHT = 0.6f
    private const val CHROMA_WEIGHT = 0.3f
    private const val LIGHTNESS_WEIGHT = 0.1f

    private const val NEUTRAL_CHROMA = 12f
    private const val HUE_TOLERANCE = 45f
    private const val CHROMA_TOLERANCE = 40f
    private const val LIGHTNESS_TOLERANCE = 50f
    private const val SLIDER_TOLERANCE = 50f

    private const val STYLE_WEIGHT = 3f
    private const val SLIDER_WEIGHT = 2f
    private const val PITCH_BLACK_WEIGHT = 2f
    private const val OVERRIDE_WEIGHT = 3f
    private const val MINOR_WEIGHT = 1f

    fun toComparable(
        item: PendingSubmission,
        payload: ThemePayload?
    ): ComparableTheme {
        val p = payload ?: ThemePayloadDecoder.decode(item.payloadJson)
        return ComparableTheme(
            id = item.id,
            name = item.name,
            author = item.author,
            description = p?.description.orEmpty(),
            seedColor = item.seedColor,
            secondaryColor = item.secondaryColor,
            tertiaryColor = item.tertiaryColor,
            style = p?.style.orEmpty(),
            colorSpecVersion = p?.colorSpecVersion ?: 0,
            accentSaturation = p?.accentSaturation ?: 100,
            backgroundSaturation = p?.backgroundSaturation ?: 100,
            backgroundLightness = p?.backgroundLightness ?: 100,
            modeSpecificThemes = p?.modeSpecificThemes ?: false,
            accentSaturationLight = p?.accentSaturationLight ?: (p?.accentSaturation ?: 100),
            backgroundSaturationLight = p?.backgroundSaturationLight ?: (p?.backgroundSaturation ?: 100),
            backgroundLightnessLight = p?.backgroundLightnessLight ?: (p?.backgroundLightness ?: 100),
            accurateShades = p?.accurateShades ?: true,
            pitchBlack = p?.pitchBlack ?: false,
            tintText = p?.tintText ?: true,
            colorOverrides = p?.colorOverrides ?: emptyMap(),
            payloadJson = item.payloadJson,
            isPublished = false
        )
    }

    fun findBestMatch(
        target: PendingSubmission,
        candidates: List<ComparableTheme>,
        threshold: Float = MATCH_THRESHOLD
    ): MatchResult? {
        val targetPayload = ThemePayloadDecoder.decode(target.payloadJson)
        val targetComp = toComparable(target, targetPayload)

        var bestMatch: ComparableTheme? = null
        var maxScore = 0.0f

        for (candidate in candidates) {
            if (candidate.id == target.id) continue
            val score = calculateSimilarity(targetComp, candidate)
            if (score > maxScore) {
                maxScore = score
                bestMatch = candidate
            }
        }

        if (bestMatch != null && maxScore >= threshold) {
            val pct = if (maxScore >= 1f) 100 else floor(maxScore * 100f).toInt().coerceIn(0, 99)
            return MatchResult(bestMatch, maxScore, pct)
        }

        return null
    }

    fun calculateSimilarity(t1: ComparableTheme, t2: ComparableTheme): Float {
        val paletteSimilarity = paletteSimilarity(t1, t2)
        if (paletteSimilarity <= 0f) return 0f
        val settingsSimilarity = settingsSimilarity(t1, t2)
        return (paletteSimilarity * (SETTINGS_FLOOR + (1f - SETTINGS_FLOOR) * settingsSimilarity))
            .coerceIn(0f, 1f)
    }

    private fun paletteSimilarity(t1: ComparableTheme, t2: ComparableTheme): Float {
        var weighted = 0f
        var totalWeight = 0f

        fun slot(c1: Int?, c2: Int?, weight: Float) {
            if (c1 == null && c2 == null) return
            totalWeight += weight
            weighted += weight * when {
                c1 == null || c2 == null -> 0f
                else -> colorSimilarity(c1, c2)
            }
        }

        slot(t1.seedColor, t2.seedColor, SEED_WEIGHT)
        slot(t1.secondaryColor, t2.secondaryColor, ACCENT_WEIGHT)
        slot(t1.tertiaryColor, t2.tertiaryColor, ACCENT_WEIGHT)

        if (totalWeight == 0f) return 1f
        return weighted / totalWeight
    }

    private fun colorSimilarity(@ColorInt c1: Int, @ColorInt c2: Int): Float {
        if (c1 == c2) return 1f

        val lch1 = PerceptualColor.lch(c1)
        val lch2 = PerceptualColor.lch(c2)
        val neutral1 = lch1[1] < NEUTRAL_CHROMA
        val neutral2 = lch2[1] < NEUTRAL_CHROMA

        val hueSimilarity = when {
            neutral1 && neutral2 -> 1f
            neutral1 != neutral2 -> 0f
            else -> {
                val distance = abs(lch1[2] - lch2[2])
                ratio(min(distance, 360f - distance), HUE_TOLERANCE)
            }
        }
        val chromaSimilarity = ratio(abs(lch1[1] - lch2[1]), CHROMA_TOLERANCE)
        val lightnessSimilarity = ratio(abs(lch1[0] - lch2[0]), LIGHTNESS_TOLERANCE)

        return HUE_WEIGHT * hueSimilarity +
                CHROMA_WEIGHT * chromaSimilarity +
                LIGHTNESS_WEIGHT * lightnessSimilarity
    }

    private fun settingsSimilarity(t1: ComparableTheme, t2: ComparableTheme): Float {
        var weighted = 0f
        var totalWeight = 0f

        fun field(similarity: Float, weight: Float) {
            totalWeight += weight
            weighted += weight * similarity
        }

        fun flag(isMatch: Boolean, weight: Float) = field(if (isMatch) 1f else 0f, weight)

        flag(t1.style.equals(t2.style, ignoreCase = true), STYLE_WEIGHT)
        flag(t1.colorSpecVersion == t2.colorSpecVersion, MINOR_WEIGHT)
        field(sliderSimilarity(t1.accentSaturation, t2.accentSaturation), SLIDER_WEIGHT)
        field(sliderSimilarity(t1.backgroundSaturation, t2.backgroundSaturation), SLIDER_WEIGHT)
        field(sliderSimilarity(t1.backgroundLightness, t2.backgroundLightness), SLIDER_WEIGHT)
        flag(t1.modeSpecificThemes == t2.modeSpecificThemes, MINOR_WEIGHT)

        if (t1.modeSpecificThemes || t2.modeSpecificThemes) {
            field(
                sliderSimilarity(t1.accentSaturationLight, t2.accentSaturationLight),
                SLIDER_WEIGHT
            )
            field(
                sliderSimilarity(t1.backgroundSaturationLight, t2.backgroundSaturationLight),
                SLIDER_WEIGHT
            )
            field(
                sliderSimilarity(t1.backgroundLightnessLight, t2.backgroundLightnessLight),
                SLIDER_WEIGHT
            )
        }

        flag(t1.accurateShades == t2.accurateShades, MINOR_WEIGHT)
        flag(t1.pitchBlack == t2.pitchBlack, PITCH_BLACK_WEIGHT)
        flag(t1.tintText == t2.tintText, MINOR_WEIGHT)

        val overrideKeys = t1.colorOverrides.keys + t2.colorOverrides.keys
        if (overrideKeys.isNotEmpty()) {
            var overrideScore = 0f
            for (key in overrideKeys) {
                val o1 = t1.colorOverrides[key]
                val o2 = t2.colorOverrides[key]
                overrideScore += if (o1 == null || o2 == null) 0f else colorSimilarity(o1, o2)
            }
            field(overrideScore / overrideKeys.size, OVERRIDE_WEIGHT)
        }

        if (totalWeight == 0f) return 1f
        return weighted / totalWeight
    }

    private fun sliderSimilarity(v1: Int, v2: Int): Float =
        ratio(abs(v1 - v2).toFloat(), SLIDER_TOLERANCE)

    private fun ratio(distance: Float, tolerance: Float): Float =
        (1f - (distance / tolerance)).coerceIn(0f, 1f)
}
