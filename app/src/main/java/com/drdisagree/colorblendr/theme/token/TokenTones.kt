package com.drdisagree.colorblendr.theme.token

import android.util.LruCache
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.scheme.SchemeFactory.roleOf
import com.drdisagree.materialcolorutilities.dynamiccolor.DynamicScheme

object TokenTones {

    private val cache = LruCache<Pair<ThemeSource, Boolean>, Map<Token, TokenResolution>>(16)

    fun of(source: ThemeSource, scheme: DynamicScheme): Map<Token, TokenResolution> {
        val key = source to scheme.isDark
        return cache.get(key) ?: compute(scheme).also { cache.put(key, it) }
    }

    private fun compute(scheme: DynamicScheme): Map<Token, TokenResolution> {
        val tokenSource = TokenSource()
        return buildMap {
            Token.entries.forEach { token ->
                val color = token.color(tokenSource) ?: return@forEach
                val role = scheme.roleOf(color.palette(scheme))
                val tone = color.getTone(scheme)
                put(
                    token,
                    if (role == null) {
                        TokenResolution(role = null, tone = tone, color = color.getArgb(scheme))
                    } else {
                        TokenResolution(
                            role = role,
                            tone = tone,
                            chromaMultiplier = color.chromaMultiplier?.invoke(scheme) ?: 1.0,
                            opacity = color.opacity?.invoke(scheme)
                        )
                    }
                )
            }
        }
    }
}
