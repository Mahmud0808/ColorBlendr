package com.drdisagree.colorblendr.ui.models

import com.drdisagree.colorblendr.utils.MONET
import java.util.UUID

data class CustomStyleModel(
    val styleId: String = UUID.randomUUID().toString(),
    var styleName: String,
    var description: String,
    val prefsGson: String,
    val monet: MONET,
    val palette: ArrayList<ArrayList<Int>>
)
