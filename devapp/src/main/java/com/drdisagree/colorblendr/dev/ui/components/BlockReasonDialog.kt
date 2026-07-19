package com.drdisagree.colorblendr.dev.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme

@Composable
fun BlockReasonDialog(
    target: PendingSubmission,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val anonymous = stringResource(R.string.anonymous)
    var reason by rememberSaveable(target.device) {
        mutableStateOf("${target.name} by ${target.author.ifEmpty { anonymous }}")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Block),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text(text = stringResource(R.string.block_uploader)) },
        text = {
            OutlinedTextField(
                shape = RoundedCornerShape(16.dp),
                value = reason,
                onValueChange = { reason = it },
                label = { Text(text = stringResource(R.string.block_reason)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason.trim()) },
                enabled = reason.isNotBlank(),
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(text = stringResource(R.string.block))
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

@Preview
@Composable
private fun BlockReasonDialogPreview() {
    DevTheme {
        BlockReasonDialog(
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
            onDismiss = {},
            onConfirm = {}
        )
    }
}