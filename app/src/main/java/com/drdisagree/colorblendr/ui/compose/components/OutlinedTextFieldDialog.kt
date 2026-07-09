package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Mirrors StylesFragment's dialog built from view_text_field_outlined.xml:
// two 16dp-rounded outlined fields (RoundedTextInputLayout style).
@Composable
fun OutlinedTextFieldDialog(
    title: String,
    firstFieldLabel: String,
    secondFieldLabel: String,
    confirmText: String,
    dismissText: String,
    onConfirm: (first: String, second: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialFirstValue: String = "",
    initialSecondValue: String = ""
) {
    var firstValue by rememberSaveable { mutableStateOf(initialFirstValue) }
    var secondValue by rememberSaveable { mutableStateOf(initialSecondValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                OutlinedTextField(
                    value = firstValue,
                    onValueChange = { firstValue = it },
                    label = { Text(text = firstFieldLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = secondValue,
                    onValueChange = { secondValue = it },
                    label = { Text(text = secondFieldLabel) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(firstValue, secondValue) }) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText)
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
private fun OutlinedTextFieldDialogPreview() {
    ColorBlendrTheme {
        OutlinedTextFieldDialog(
            title = "New style",
            firstFieldLabel = "Name",
            secondFieldLabel = "Description",
            confirmText = "Save",
            dismissText = "Cancel",
            onConfirm = { _, _ -> },
            onDismiss = {}
        )
    }
}
