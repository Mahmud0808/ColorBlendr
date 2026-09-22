package com.drdisagree.colorblendr.ui.compose.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

fun AnimatedContentTransitionScope<NavBackStackEntry>.isPop(navController: NavController): Boolean {
    val current = navController.currentBackStackEntry
    val previous = navController.previousBackStackEntry
    return targetState == previous || (initialState != current && initialState != previous)
}
