package com.drdisagree.colorblendr.ui.compose.screens.palette

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.enums.MONET
import com.drdisagree.colorblendr.theme.ThemeEngine
import com.drdisagree.colorblendr.theme.model.ThemeAdjustments
import com.drdisagree.colorblendr.theme.model.ThemeSource
import com.drdisagree.colorblendr.theme.scheme.ResolvedScheme
import com.drdisagree.colorblendr.theme.token.Token
import com.drdisagree.colorblendr.theme.token.TokenCategory
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.InfoHint
import com.drdisagree.colorblendr.ui.compose.components.ListSectionHeader
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.components.previewContentPadding
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.utils.AdaptivePreviews
import com.drdisagree.colorblendr.ui.compose.utils.transparencyGrid
import com.drdisagree.colorblendr.ui.viewmodels.ColorTokensViewModel

@Composable
fun TokenGuideScreen(
    viewModel: ColorTokensViewModel = viewModel()
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    TokenGuideContent(scheme = theme?.scheme(isDark))
}

@Composable
private fun TokenGuideContent(scheme: ResolvedScheme?) {
    val listState = rememberLazyListState()
    val toolbarLifted by remember { derivedStateOf { listState.canScrollBackward } }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.color_tokens_guide_title),
                showBackButton = true,
                lifted = toolbarLifted
            )
            LazyColumn(
                state = listState,
                contentPadding = previewContentPadding(top = 12.dp, bottom = 12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .contentWidthLimit()
            ) {
                item(key = "hint") {
                    InfoHint(text = stringResource(R.string.color_tokens_usage_hint))
                }
                items(TokenCategory.entries, key = { it.name }) { category ->
                    Column {
                        ListSectionHeader(text = stringResource(category.labelRes))
                        GuideCard(tokens = category.tokens, scheme = scheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideCard(tokens: List<Token>, scheme: ResolvedScheme?) {
    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.container_margin_horizontal))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            tokens.forEach { token ->
                GuideRow(token = token, color = scheme?.get(token))
            }
        }
    }
}

@Composable
private fun GuideRow(token: Token, @ColorInt color: Int?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .padding(1.dp)
                .clip(CircleShape)
                .transparencyGrid(color)
                .background(color?.let(::Color) ?: Color.Transparent)
        )
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = token.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(token.usageRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun TokenGuideContentPreview() {
    val scheme = ThemeEngine.scheme(
        source = ThemeSource(style = MONET.TONAL_SPOT, seedColor = 0xFF4285F4.toInt()),
        adjustments = ThemeAdjustments(),
        isDark = true
    )
    ColorBlendrTheme {
        TokenGuideContent(scheme = scheme)
    }
}
