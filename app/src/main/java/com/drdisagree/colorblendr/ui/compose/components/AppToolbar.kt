package com.drdisagree.colorblendr.ui.compose.components

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Lift-on-scroll toolbar: 12dp horizontal padding, zero content inset,
// TitleLarge title.
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
            .padding(horizontal = 16.dp)
    ) {
        if (showBackButton) {
            ToolbarIconPill(
                iconResId = R.drawable.ic_ab_back_material,
                shape = CircleShape,
                width = 40.dp,
                onClick = { backDispatcher?.onBackPressed() }
            )
            Spacer(modifier = Modifier.width(8.dp))
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

// Surface pill (surfaceBright at night) behind 24dp icon.
@Composable
fun ToolbarIconPill(
    iconResId: Int,
    shape: Shape,
    width: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pillColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surfaceBright
    } else {
        MaterialTheme.colorScheme.surface
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(width)
            .height(40.dp)
            .clip(shape)
            .background(pillColor)
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ToolbarOverflowButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    ToolbarIconPill(
        iconResId = R.drawable.ic_menu_moreoverflow_material,
        shape = RoundedCornerShape(12.dp),
        width = 36.dp,
        onClick = onClick,
        modifier = modifier
    )
}

@Preview
@Composable
private fun AppToolbarPreview() {
    ColorBlendrTheme {
        AppToolbar(title = "Settings", showBackButton = true)
    }
}
