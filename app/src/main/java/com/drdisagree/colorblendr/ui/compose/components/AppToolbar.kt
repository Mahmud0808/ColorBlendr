package com.drdisagree.colorblendr.ui.compose.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Mirrors view_toolbar.xml: AppBarLayout(liftOnScroll) + MaterialToolbar with
// 12dp horizontal padding, zero content inset and TitleLarge title.
@Composable
fun AppToolbar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    lifted: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val containerColor by animateColorAsState(
        targetValue = if (lifted) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "toolbarLift"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .height(64.dp)
            .padding(horizontal = 12.dp)
    ) {
        if (showBackButton) {
            IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_toolbar_chevron),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Preview
@Composable
private fun AppToolbarPreview() {
    ColorBlendrTheme {
        AppToolbar(title = "Settings", showBackButton = true)
    }
}
