package com.drdisagree.colorblendr.utils.community

import androidx.annotation.StringRes
import com.drdisagree.colorblendr.R

object CommunityTextRules {

    const val NAME_MIN_LENGTH = 4
    const val NAME_MAX_LENGTH = 24
    const val DESCRIPTION_MIN_LENGTH = 12
    const val DESCRIPTION_MAX_LENGTH = 300
    const val AUTHOR_MAX_LENGTH = 24

    private const val NAME_MIN_DISTINCT_LETTERS = 3
    private const val DESCRIPTION_MIN_WORDS = 3

    private val NAME_CHARS = Regex("^[\\p{L}\\p{N}][\\p{L}\\p{N} '&()+.,!:#/_-]*$")
    private val WHITESPACE = Regex("\\s+")
    private val WORD = Regex("\\p{L}{2,}")

    fun normalizeName(raw: String): String = raw.trim().replace(WHITESPACE, " ")

    @StringRes
    fun nameError(raw: String): Int? {
        val name = normalizeName(raw)
        return when {
            name.length < NAME_MIN_LENGTH -> R.string.community_text_too_short
            !NAME_CHARS.matches(name) -> R.string.theme_name_invalid_chars
            name.filter(Char::isLetter).lowercase().toSet().size < NAME_MIN_DISTINCT_LETTERS ->
                R.string.theme_name_too_few_letters

            else -> null
        }
    }

    @StringRes
    fun descriptionError(raw: String): Int? {
        val description = raw.trim()
        return when {
            description.length < DESCRIPTION_MIN_LENGTH -> R.string.community_text_too_short
            description.split(WHITESPACE).count(WORD::containsMatchIn) < DESCRIPTION_MIN_WORDS ->
                R.string.theme_description_too_few_words

            else -> null
        }
    }
}
