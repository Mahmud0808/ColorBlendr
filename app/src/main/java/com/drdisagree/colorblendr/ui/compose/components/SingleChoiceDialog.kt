package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.verticalFadingEdges
import android.R as AndroidR

// Radio list dialog; tapping an option commits and dismisses (same behavior
// as the old single-choice alert dialogs).
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SingleChoiceDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    descriptions: List<String> = emptyList()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .verticalFadingEdges(scrollState, length = 24.dp)
                    .verticalScroll(scrollState)
            ) {
                options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .selectable(
                                selected = index == selectedIndex,
                                role = Role.RadioButton,
                                onClick = {
                                    onSelect(index)
                                    onDismiss()
                                }
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = null
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            descriptions.getOrNull(index)?.let { description ->
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(text = stringResource(AndroidR.string.cancel))
            }
        }
    )
}

@Preview
@Composable
private fun SingleChoiceDialogPreview() {
    ColorBlendrTheme {
        SingleChoiceDialog(
            title = "Sort by",
            options = listOf("Most popular", "Newest first", "Oldest first"),
            selectedIndex = 0,
            onSelect = {},
            onDismiss = {},
            descriptions = listOf("Most upvoted first")
        )
    }
}
