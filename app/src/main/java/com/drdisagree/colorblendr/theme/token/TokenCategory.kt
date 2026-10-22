package com.drdisagree.colorblendr.theme.token

enum class TokenCategory {
    SURFACE,
    PRIMARY,
    SECONDARY,
    TERTIARY,
    ERROR,
    FIXED,
    FRAMEWORK,
    SYSTEM_UI,
    KEY_COLOR;

    val tokens: List<Token> by lazy { Token.entries.filter { of(it) == this } }

    companion object {
        private val FRAMEWORK_PREFIXES = listOf("CONTROL_", "TEXT_", "GEN_")

        fun of(token: Token): TokenCategory {
            val name = token.name
            return when {
                name.startsWith("PALETTE_KEY_COLOR") -> KEY_COLOR
                token.group == TokenGroup.FIXED -> FIXED
                token.group == TokenGroup.CUSTOM -> SYSTEM_UI
                FRAMEWORK_PREFIXES.any(name::startsWith) -> FRAMEWORK
                "PRIMARY" in name -> PRIMARY
                "SECONDARY" in name -> SECONDARY
                "TERTIARY" in name -> TERTIARY
                "ERROR" in name -> ERROR
                else -> SURFACE
            }
        }
    }
}
