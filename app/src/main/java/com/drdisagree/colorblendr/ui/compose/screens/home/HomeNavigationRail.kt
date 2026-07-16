package com.drdisagree.colorblendr.ui.compose.screens.home

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

@Composable
internal fun HomeNavigationRail(
    currentGroup: Int,
    expanded: Boolean,
    onTabClick: (TabItem) -> Unit
) {
    val state = rememberWideNavigationRailState()
    LaunchedEffect(expanded) {
        if (expanded) state.expand() else state.collapse()
    }

    WideNavigationRail(state = state) {
        tabs.forEach { tab ->
            val selected = currentGroup == tab.group
            WideNavigationRailItem(
                railExpanded = state.targetValue == WideNavigationRailValue.Expanded,
                selected = selected,
                icon = {
                    Icon(
                        imageVector = if (selected) tab.filledIcon else tab.outlineIcon,
                        contentDescription = null
                    )
                },
                label = { Text(text = stringResource(tab.labelResId)) },
                onClick = { onTabClick(tab) }
            )
        }
    }
}

@Preview
@Composable
private fun HomeNavigationRailPreview() {
    ColorBlendrTheme {
        HomeNavigationRail(
            currentGroup = 1,
            expanded = false,
            onTabClick = {}
        )
    }
}

@Preview
@Composable
private fun HomeNavigationRailExpandedPreview() {
    ColorBlendrTheme {
        HomeNavigationRail(
            currentGroup = 1,
            expanded = true,
            onTabClick = {}
        )
    }
}