package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import android.R as AndroidR

// Confirmation dialog: title + message, outlined cancel + filled confirm,
// M3 expressive button shapes. Compose sibling of ErrorDialog.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = stringResource(AndroidR.string.cancel)
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(text = dismissText)
            }
        }
    )
}

@Preview
@Composable
private fun ConfirmDialogPreview() {
    ColorBlendrTheme {
        ConfirmDialog(
            title = "Delete style?",
            message = "This action cannot be undone.",
            confirmText = "Delete",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
