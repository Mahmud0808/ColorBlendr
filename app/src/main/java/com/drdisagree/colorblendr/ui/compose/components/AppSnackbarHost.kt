package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Tracks whether any snackbar is currently shown, so overlapping UI (e.g.
// the preview apply/discard buttons) can move out of the way.
object SnackbarVisibility {
    var count by mutableIntStateOf(0)
    val visible: Boolean get() = count > 0
}

// Snackbar host matching the old SnackBarStyle theme styles, dismissable by
// swiping horizontally.
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currentData = hostState.currentSnackbarData
    DisposableEffect(currentData) {
        if (currentData != null) {
            SnackbarVisibility.count++
            onDispose { SnackbarVisibility.count-- }
        } else {
            onDispose { }
        }
    }

    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        key(data) {
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value != SwipeToDismissBoxValue.Settled) {
                        data.dismiss()
                    }
                    true
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                backgroundContent = {}
            ) {
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Dismisses the currently shown snackbar before showing the new one instead
// of queueing behind it.
suspend fun SnackbarHostState.showSnackbarReplacing(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration =
        if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(message, actionLabel, withDismissAction, duration)
}

@Preview
@Composable
private fun AppSnackbarHostPreview() {
    ColorBlendrTheme {
        val hostState = remember {
            SnackbarHostState().apply {
                // Previews cannot suspend; render the styled snackbar directly.
            }
        }
        AppSnackbarHost(hostState = hostState)
        Snackbar(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Text(text = "Backup saved")
        }
    }
}
