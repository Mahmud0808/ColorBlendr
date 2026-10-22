package com.drdisagree.colorblendr.theme.scheme

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.override.ColorOverride
import com.drdisagree.colorblendr.theme.palette.AdjustedPalette
import com.drdisagree.colorblendr.theme.rule.ThemeRule
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenGroup
import com.drdisagree.colorblendr.theme.token.TokenResolution
import kotlin.math.abs
import kotlin.math.roundToInt

class ResolvedScheme(
    val isDark: Boolean,
    private val palettes: Map<PaletteRole, AdjustedPalette>,
    private val rules: List<ThemeRule>,
    private val overrides: Map<Token, ColorOverride> = emptyMap(),
    private val tones: () -> Map<Token, TokenResolution>
) {
    private val baseColors: Map<Token, Int> by lazy {
        tones().mapValues { (token, resolution) -> resolve(token, resolution) }
    }

    val colors: Map<Token, Int> by lazy {
        baseColors.mapValues { (token, color) -> overrides[token]?.let(::colorOf) ?: color }
    }

    val shades: List<List<Int>> by lazy {
        PaletteRole.entries.map { role ->
            ShadeScale.indices.map { index ->
                rules.fold(get(role).shades[index]) { color, rule ->
                    rule.shade(role, index, color, isDark)
                }
            }
        }
    }

    @ColorInt
    operator fun get(token: Token): Int = colors.getValue(token)

    operator fun get(role: PaletteRole): AdjustedPalette = palettes.getValue(role)

    @ColorInt
    operator fun get(role: PaletteRole, index: Int): Int = shades[role.ordinal][index]

    fun tokens(group: TokenGroup): List<Token> = colors.keys.filter { it.group == group }

    fun origin(token: Token): TokenResolution? = tones()[token]

    fun override(token: Token): ColorOverride? = overrides[token]

    @ColorInt
    fun base(token: Token): Int = baseColors.getValue(token)

    @ColorInt
    fun colorOf(override: ColorOverride): Int = when (override) {
        is ColorOverride.Shade -> get(override.role, override.index).let { shade ->
            override.alpha?.let { ColorUtils.setAlphaComponent(shade, it) } ?: shade
        }

        is ColorOverride.Custom -> override.color
    }

    fun originShade(token: Token): ColorOverride.Shade? {
        val origin = origin(token) ?: return null
        val role = origin.role ?: return null
        val index = ShadeScale.indices.minBy { abs(ShadeScale.tones[it] - origin.tone) }
        return ColorOverride.Shade(role, index)
    }

    fun closestShade(@ColorInt color: Int): ColorOverride.Shade {
        val target = DoubleArray(3).also { ColorUtils.colorToLAB(color, it) }
        val candidate = DoubleArray(3)
        var best = ColorOverride.Shade(PaletteRole.NEUTRAL, 0)
        var bestDistance = Double.MAX_VALUE
        PaletteRole.entries.forEach { role ->
            ShadeScale.indices.forEach { index ->
                ColorUtils.colorToLAB(get(role, index), candidate)
                val distance = ColorUtils.distanceEuclidean(target, candidate)
                if (distance < bestDistance) {
                    bestDistance = distance
                    best = ColorOverride.Shade(role, index)
                }
            }
        }
        return best
    }

    @ColorInt
    fun reference(role: PaletteRole, tone: Double): Int {
        val index = ShadeScale.indexOfTone(tone)
        if (index >= 0) return get(role).shades[index]
        val ruledTone = rules.fold(tone) { current, rule -> rule.referenceTone(role, current, isDark) }
        return get(role).colorAt(ruledTone)
    }

    private fun resolve(token: Token, initial: TokenResolution): Int {
        val resolution = rules.fold(initial) { current, rule -> rule.token(token, current, isDark) }
        val argb = resolution.color
            ?: get(resolution.role!!).colorAt(resolution.tone, resolution.chromaMultiplier)

        return resolution.opacity?.let { opacity ->
            ColorUtils.setAlphaComponent(argb, (opacity * 255).roundToInt().coerceIn(0, 255))
        } ?: argb
    }
}
