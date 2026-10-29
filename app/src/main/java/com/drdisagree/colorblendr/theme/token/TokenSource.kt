package com.drdisagree.colorblendr.theme.token

import com.drdisagree.materialcolorutilities.dynamiccolor.MaterialDynamicColors

class TokenSource {
    val material = MaterialDynamicColors()
    val android = AndroidDynamicColors(material)
    val systemUi = SystemUiDynamicColors(material)
}
