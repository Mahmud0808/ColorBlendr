package com.drdisagree.colorblendr.ui.compose.screens.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

@Composable
internal fun HomeNavigationBar(
    currentGroup: Int,
    onTabClick: (TabItem) -> Unit
) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        tabs.forEach { tab ->
            val selected = currentGroup == tab.group
            NavigationBarItem(
                selected = selected,
                alwaysShowLabel = false,
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
private fun HomeNavigationBarPreview() {
    ColorBlendrTheme {
        HomeNavigationBar(
            currentGroup = 1,
            onTabClick = {}
        )
    }
}