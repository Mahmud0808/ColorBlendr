package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

@Composable
fun ListSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(
            start = dimensionResource(R.dimen.container_margin_horizontal) + 16.dp,
            top = 12.dp,
            bottom = 8.dp
        )
    )
}

@Preview
@Composable
private fun ListSectionHeaderPreview() {
    ColorBlendrTheme {
        ListSectionHeader(text = "Surfaces")
    }
}
