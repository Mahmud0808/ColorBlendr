package com.drdisagree.colorblendr.dev.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterExitState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme

fun NavGraphBuilder.screen(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(route = route, arguments = arguments) { entry ->
        ExitInputGuard { content(entry) }
    }
}

@Composable
fun AnimatedContentScope.ExitInputGuard(content: @Composable () -> Unit) {
    val exiting = transition.targetState == EnterExitState.PostExit
    Box(
        modifier = if (exiting) {
            Modifier.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                    }
                }
            }
        } else {
            Modifier
        }
    ) {
        content()
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Preview
@Composable
private fun ExitInputGuardPreview() {
    DevTheme {
        AnimatedContent(targetState = Unit) {
            ExitInputGuard { Text(text = "Screen") }
        }
    }
}
