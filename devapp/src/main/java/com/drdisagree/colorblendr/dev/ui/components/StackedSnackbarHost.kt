package com.drdisagree.colorblendr.dev.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.dev.data.models.StackedMessage
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun StackedSnackbarHost(
    messages: List<StackedMessage>,
    onDismiss: (Long) -> Unit
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
    ) {
        Column {
            messages.forEach { msg ->
                key(msg.id) {
                    val visibleState = remember {
                        MutableTransitionState(false).apply { targetState = true }
                    }
                    LaunchedEffect(msg.id) {
                        delay(3500)
                        visibleState.targetState = false
                    }
                    LaunchedEffect(msg.id) {
                        snapshotFlow { visibleState.isIdle && !visibleState.targetState }
                            .first { it }
                        onDismiss(msg.id)
                    }
                    AnimatedVisibility(
                        visibleState = visibleState,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val dismissState = rememberSwipeToDismissBoxState()
                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                visibleState.targetState = false
                            }
                        }
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {},
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Snackbar(
                                shape = RoundedCornerShape(16.dp),
                                containerColor = MaterialTheme.colorScheme.inverseSurface,
                                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = msg.text)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun StackedSnackbarHostPreview() {
    DevTheme {
        StackedSnackbarHost(
            messages = listOf(
                StackedMessage(1L, "Approved, PR opened"),
                StackedMessage(2L, "Uploader blocked")
            ),
            onDismiss = {}
        )
    }
}
