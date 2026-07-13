package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// primaryContainer note card: bold title, optional close action.
@Composable
fun WarningCard(
    warningText: String,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.container_margin_horizontal))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.note_title),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                if (onClose != null) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Close),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onClose()
                            }
                    )
                }
            }
            Text(
                text = warningText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}

@Preview
@Composable
private fun WarningCardPreview() {
    ColorBlendrTheme {
        WarningCard(
            warningText = "Changes here only apply to the selected app.",
            onClose = {}
        )
    }
}
