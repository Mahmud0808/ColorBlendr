package com.drdisagree.colorblendr.ui.compose.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

fun NavController.navigateSingleTop(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    val resumed = currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED)
    if (resumed != false) {
        navigate(route, builder)
    }
}