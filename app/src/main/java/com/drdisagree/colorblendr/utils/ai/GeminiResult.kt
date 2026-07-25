package com.drdisagree.colorblendr.utils.ai

import com.drdisagree.colorblendr.data.models.CommunityTheme

sealed class GeminiResult {
    data class Success(val theme: CommunityTheme) : GeminiResult()
    data class Failure(val error: GeminiError) : GeminiResult()
}