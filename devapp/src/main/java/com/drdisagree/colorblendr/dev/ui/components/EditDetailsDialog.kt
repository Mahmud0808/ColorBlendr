package com.drdisagree.colorblendr.dev.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme

@Composable
fun EditDetailsDialog(
    target: PendingSubmission,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by rememberSaveable(target.id) { mutableStateOf(target.name) }
    var text by rememberSaveable(target.id) { mutableStateOf(description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Edit),
                contentDescription = null
            )
        },
        title = { Text(text = stringResource(R.string.edit_details)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    shape = RoundedCornerShape(16.dp),
                    value = name,
                    onValueChange = { name = it.take(PendingSubmission.MAX_NAME) },
                    label = { Text(text = stringResource(R.string.theme_name)) },
                    supportingText = {
                        Counter(name.length, PendingSubmission.MAX_NAME)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    shape = RoundedCornerShape(16.dp),
                    value = text,
                    onValueChange = { text = it.take(PendingSubmission.MAX_DESCRIPTION) },
                    label = { Text(text = stringResource(R.string.theme_description)) },
                    supportingText = {
                        Counter(text.length, PendingSubmission.MAX_DESCRIPTION)
                    },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), text.trim()) },
                enabled = name.isNotBlank() && text.isNotBlank(),
                shapes = ButtonDefaults.shapes()
            ) {
                Text(text = stringResource(R.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(text = stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun Counter(length: Int, max: Int) {
    Text(
        text = stringResource(R.string.char_counter, length, max),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.End)
    )
}

@Preview
@Composable
private fun EditDetailsDialogPreview() {
    DevTheme {
        EditDetailsDialog(
            target = PendingSubmission(
                id = "ocean-breeze-abc123",
                name = "Ocean Breeze",
                author = "DrDisagree",
                device = "a1b2c3d4e5f6",
                created = 1752800000000L,
                seedColor = null,
                secondaryColor = null,
                tertiaryColor = null,
                payloadJson = "{}"
            ),
            description = "Cool ocean tones",
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}