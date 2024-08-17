package com.drdisagree.colorblendr.ui.models

import android.graphics.drawable.Drawable

data class AppInfoModel(
    var appName: String,
    var packageName: String,
    var appIcon: Drawable,
    var isSelected: Boolean = false
)
