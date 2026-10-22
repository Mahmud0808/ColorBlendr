package com.drdisagree.colorblendr.theme.scheme

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.theme.model.PaletteRole
import com.drdisagree.colorblendr.theme.model.ShadeScale
import com.drdisagree.colorblendr.theme.palette.AdjustedPalette
import com.drdisagree.colorblendr.theme.rule.ThemeRule
import com.drdisagree.colorblendr.theme.scheme.SchemeFactory.roleOf
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenGroup
import com.drdisagree.colorblendr.theme.token.TokenResolution
import com.drdisagree.colorblendr.theme.token.TokenSource
import com.drdisagree.materialcolorutilities.dynamiccolor.DynamicColor
import com.drdisagree.materialcolorutilities.dynamiccolor.DynamicScheme
import kotlin.math.roundToInt

class ResolvedScheme(
    val isDark: Boolean,
    private val dynamicScheme: DynamicScheme,
    private val palettes: Map<PaletteRole, AdjustedPalette>,
    private val rules: List<ThemeRule>
) {
    val colors: Map<Token, Int> by lazy {
        val source = TokenSource()
        buildMap {
            Token.entries.forEach { token ->
                token.color(source)?.let { put(token, resolve(token, it)) }
            }
        }
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

    @ColorInt
    fun reference(role: PaletteRole, tone: Double): Int {
        val index = ShadeScale.indexOfTone(tone)
        if (index >= 0) return get(role).shades[index]
        val ruledTone = rules.fold(tone) { current, rule -> rule.referenceTone(role, current, isDark) }
        return get(role).colorAt(ruledTone)
    }

    private fun resolve(token: Token, color: DynamicColor): Int {
        val role = dynamicScheme.roleOf(color.palette(dynamicScheme))

        val initial = if (role == null) {
            TokenResolution(role = null, tone = color.getTone(dynamicScheme), color = color.getArgb(dynamicScheme))
        } else {
            TokenResolution(
                role = role,
                tone = color.getTone(dynamicScheme),
                chromaMultiplier = color.chromaMultiplier?.invoke(dynamicScheme) ?: 1.0,
                opacity = color.opacity?.invoke(dynamicScheme)
            )
        }

        val resolution = rules.fold(initial) { current, rule -> rule.token(token, current, isDark) }
        val argb = resolution.color
            ?: get(resolution.role!!).colorAt(resolution.tone, resolution.chromaMultiplier)

        return resolution.opacity?.let { opacity ->
            ColorUtils.setAlphaComponent(argb, (opacity * 255).roundToInt().coerceIn(0, 255))
        } ?: argb
    }
}
