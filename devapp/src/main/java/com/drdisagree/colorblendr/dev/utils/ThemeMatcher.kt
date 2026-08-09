package com.drdisagree.colorblendr.dev.utils

import com.drdisagree.colorblendr.dev.data.models.ComparableTheme
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.data.models.ThemePayload
import kotlin.math.roundToInt

data class MatchResult(
    val matchedTheme: ComparableTheme,
    val score: Float,
    val percentage: Int
)

object ThemeMatcher {

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
        threshold: Float = 0.80f
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
            val pct = (maxScore * 100f).roundToInt().coerceIn(0, 100)
            return MatchResult(bestMatch, maxScore, pct)
        }

        return null
    }

    fun calculateSimilarity(t1: ComparableTheme, t2: ComparableTheme): Float {
        var matchCount = 0
        var totalFields = 0

        fun check(isMatch: Boolean) {
            totalFields++
            if (isMatch) matchCount++
        }

        check(t1.seedColor == t2.seedColor)
        check(t1.secondaryColor == t2.secondaryColor)
        check(t1.tertiaryColor == t2.tertiaryColor)
        check(t1.style.equals(t2.style, ignoreCase = true))
        check(t1.colorSpecVersion == t2.colorSpecVersion)
        check(t1.accentSaturation == t2.accentSaturation)
        check(t1.backgroundSaturation == t2.backgroundSaturation)
        check(t1.backgroundLightness == t2.backgroundLightness)
        check(t1.modeSpecificThemes == t2.modeSpecificThemes)
        check(t1.accentSaturationLight == t2.accentSaturationLight)
        check(t1.backgroundSaturationLight == t2.backgroundSaturationLight)
        check(t1.backgroundLightnessLight == t2.backgroundLightnessLight)
        check(t1.accurateShades == t2.accurateShades)
        check(t1.pitchBlack == t2.pitchBlack)
        check(t1.tintText == t2.tintText)

        val allOverrideKeys = (t1.colorOverrides.keys + t2.colorOverrides.keys).toSet()
        if (allOverrideKeys.isEmpty()) {
            check(true)
        } else {
            for (key in allOverrideKeys) {
                check(t1.colorOverrides[key] == t2.colorOverrides[key])
            }
        }

        if (totalFields == 0) return 1.0f
        return (matchCount.toFloat() / totalFields.toFloat()).coerceIn(0.0f, 1.0f)
    }
}
