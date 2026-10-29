package com.drdisagree.colorblendr.theme.token

import androidx.core.graphics.ColorUtils
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme

object TokenContrast {

    private const val MIN_CONTRAST = 2.0

    private val SURFACES = listOf(
        Token.SURFACE,
        Token.SURFACE_DIM,
        Token.SURFACE_BRIGHT,
        Token.SURFACE_CONTAINER_LOWEST,
        Token.SURFACE_CONTAINER_LOW,
        Token.SURFACE_CONTAINER,
        Token.SURFACE_CONTAINER_HIGH,
        Token.SURFACE_CONTAINER_HIGHEST
    )

    private val NEUTRAL_PAIRS: List<ContrastPair> = buildList {
        add(ContrastPair(Token.ON_BACKGROUND, Token.BACKGROUND))
        SURFACES.forEach { add(ContrastPair(Token.ON_SURFACE, it)) }
        (SURFACES + Token.SURFACE_VARIANT).forEach { add(ContrastPair(Token.ON_SURFACE_VARIANT, it)) }
        add(ContrastPair(Token.INVERSE_ON_SURFACE, Token.INVERSE_SURFACE))
    }

    private val ACCENT_PAIRS: List<ContrastPair> = listOf(
        ContrastPair(Token.ON_PRIMARY, Token.PRIMARY),
        ContrastPair(Token.ON_PRIMARY_CONTAINER, Token.PRIMARY_CONTAINER),
        ContrastPair(Token.ON_SECONDARY, Token.SECONDARY),
        ContrastPair(Token.ON_SECONDARY_CONTAINER, Token.SECONDARY_CONTAINER),
        ContrastPair(Token.ON_TERTIARY, Token.TERTIARY),
        ContrastPair(Token.ON_TERTIARY_CONTAINER, Token.TERTIARY_CONTAINER),
        ContrastPair(Token.ON_ERROR, Token.ERROR),
        ContrastPair(Token.ON_ERROR_CONTAINER, Token.ERROR_CONTAINER)
    )

    val CORE_PAIRS: List<ContrastPair> = NEUTRAL_PAIRS + ACCENT_PAIRS

    private val PAIRS: List<ContrastPair> = buildList {
        addAll(CORE_PAIRS)

        fun fixed(onFixed: Token, onFixedVariant: Token, vararg backgrounds: Token) {
            backgrounds.forEach { background ->
                add(ContrastPair(onFixed, background))
                add(ContrastPair(onFixedVariant, background))
            }
        }

        fixed(Token.ON_PRIMARY_FIXED, Token.ON_PRIMARY_FIXED_VARIANT, Token.PRIMARY_FIXED, Token.PRIMARY_FIXED_DIM)
        fixed(Token.ON_SECONDARY_FIXED, Token.ON_SECONDARY_FIXED_VARIANT, Token.SECONDARY_FIXED, Token.SECONDARY_FIXED_DIM)
        fixed(Token.ON_TERTIARY_FIXED, Token.ON_TERTIARY_FIXED_VARIANT, Token.TERTIARY_FIXED, Token.TERTIARY_FIXED_DIM)

        add(ContrastPair(Token.ON_SHADE_ACTIVE, Token.SHADE_ACTIVE))
        add(ContrastPair(Token.ON_SHADE_INACTIVE, Token.SHADE_INACTIVE))
        add(ContrastPair(Token.ON_THEME_APP, Token.THEME_APP))
    }

    fun lowContrastTokens(scheme: ResolvedScheme, candidates: Set<Token>): Set<Token> {
        if (candidates.isEmpty()) return emptySet()
        return PAIRS
            .filter { it.foreground in candidates || it.background in candidates }
            .filter { isLow(scheme, it) }
            .flatMap { listOf(it.foreground, it.background) }
            .filter { it in candidates }
            .toSet()
    }

    fun hasLowContrast(scheme: ResolvedScheme, pairs: List<ContrastPair>): Boolean =
        pairs.any { isLow(scheme, it) }

    private fun isLow(scheme: ResolvedScheme, pair: ContrastPair): Boolean {
        val foreground = scheme.colors[pair.foreground] ?: return false
        val background = scheme.colors[pair.background] ?: return false
        return contrast(foreground, background) < MIN_CONTRAST
    }

    private fun contrast(foreground: Int, background: Int): Double =
        ColorUtils.calculateContrast(
            ColorUtils.setAlphaComponent(foreground, 255),
            ColorUtils.setAlphaComponent(background, 255)
        )
}
