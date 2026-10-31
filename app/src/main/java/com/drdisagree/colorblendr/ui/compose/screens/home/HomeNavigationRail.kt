package com.drdisagree.colorblendr.ui.compose.screens.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeNavigationRail(
    currentGroup: Int,
    expanded: Boolean,
    onTabClick: (TabItem) -> Unit,
    onToggle: (() -> Unit)? = null
) {
    val state = rememberWideNavigationRailState()
    LaunchedEffect(expanded) {
        if (expanded) state.expand() else state.collapse()
    }

    WideNavigationRail(
        state = state,
        colors = WideNavigationRailDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        header = onToggle?.let { toggle ->
            {
                IconButton(
                    onClick = toggle,
                    modifier = Modifier.padding(start = 24.dp)
                ) {
                    Crossfade(
                        targetState = expanded,
                        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                        label = "railToggle"
                    ) { open ->
                        Icon(
                            painter = rememberVectorPainter(
                                if (open) Icons.AutoMirrored.Rounded.MenuOpen else Icons.Rounded.Menu
                            ),
                            contentDescription = stringResource(
                                if (open) R.string.nav_rail_collapse else R.string.nav_rail_expand
                            )
                        )
                    }
                }
            }
        }
    ) {
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
            onTabClick = {},
            onToggle = {}
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
            onTabClick = {},
            onToggle = {}
        )
    }
}
