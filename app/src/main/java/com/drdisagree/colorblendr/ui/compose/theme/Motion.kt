package com.drdisagree.colorblendr.ui.compose.theme

import android.view.animation.DecelerateInterpolator
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

val DecelerateEasing = Easing { DecelerateInterpolator().getInterpolation(it) }

@Composable
fun shortAnimTime(): Int =
    LocalContext.current.resources.getInteger(android.R.integer.config_shortAnimTime)
