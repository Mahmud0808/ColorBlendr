package com.drdisagree.colorblendr.ui.models

import com.drdisagree.colorblendr.utils.MONET

data class StyleModel(
    val titleResId: Int = 0,
    val descriptionResId: Int = 0,
    val isEnabled: Boolean = false,
    val monetStyle: MONET,
    val customStyle: CustomStyleModel? = null
)