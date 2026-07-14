package com.drdisagree.colorblendr.ui.compose.screens.about

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Help
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.runtime.setValue
import com.drdisagree.colorblendr.data.common.Utilities.setDeveloperModeEnabled
import com.drdisagree.colorblendr.data.common.Utilities.developerModeEnabled
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.interaction.MutableInteractionSource
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.models.AboutAppModel
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbar
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.Avatar
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.utils.app.parseContributors
import com.drdisagree.colorblendr.utils.app.parseTranslators

@Composable
fun AboutScreen() {
    val listState = rememberLazyListState()
    val toolbarLifted by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }
    val contributors = remember { parseContributors() }
    val translators = remember { parseTranslators() }
    val contributorsHeader = stringResource(R.string.contributors)
    val translatorsHeader = stringResource(R.string.translators)
    // Headers already underline-animated this visit (once per screen entry).
    val animatedHeaders = remember { mutableStateListOf<String>() }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.about_this_app_title),
                showBackButton = true,
                lifted = toolbarLifted
            )
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(bottom = LocalPreviewBottomInset.current),
                modifier = Modifier.fillMaxSize()
            ) {
                item { AboutAppHeader(animatedHeaders) }
                creditsSection(contributorsHeader, contributors, animatedHeaders)
                creditsSection(translatorsHeader, translators, animatedHeaders)
                item {
                    Spacer(
                        modifier = Modifier.height(dimensionResource(R.dimen.container_margin_bottom))
                    )
                }
            }
        }
    }
}

// Centered section title with an accent underline that grows from the center
// to the text width once, when the header first enters the visible area.
@Composable
private fun SectionHeader(
    text: String,
    animatedHeaders: MutableList<String>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var textWidthPx by remember { mutableIntStateOf(0) }
    val alreadyAnimated = text in animatedHeaders
    val progress = remember { Animatable(if (alreadyAnimated) 1f else 0f) }

    LaunchedEffect(text) {
        if (!alreadyAnimated) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            animatedHeaders.add(text)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            onTextLayout = { textWidthPx = it.size.width }
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(with(density) {
                    val startPx = 32.dp.toPx()
                    val endPx = textWidthPx.toFloat().coerceAtLeast(startPx)
                    (startPx + (endPx - startPx) * progress.value).toDp()
                })
                .height(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

private fun LazyListScope.creditsSection(
    header: String,
    items: List<AboutAppModel>,
    animatedHeaders: MutableList<String>
) {
    val credits = items.filter { it.url.isNotEmpty() }

    if (header.isNotEmpty()) {
        item {
            SectionHeader(
                text = header,
                animatedHeaders = animatedHeaders,
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )
        }
    }

    itemsIndexed(credits) { index, model ->
        CreditsItem(
            model = model,
            isFirst = index == 0,
            isLast = index == credits.size - 1
        )
    }
}

@Composable
private fun AboutAppHeader(animatedHeaders: MutableList<String>) {
    val context = LocalContext.current
    val appIcon = remember(context) {
        try {
            context.packageManager.getApplicationIcon(context.packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }?.toBitmap()?.asImageBitmap()
    }

    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)) {
        appIcon?.let {
            Image(
                bitmap = it,
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp)
            )
        }
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // 7 taps on the version unlock developer mode (community theme tester).
        val context = LocalContext.current
        val haptics = LocalHapticFeedback.current
        var versionTaps by remember { mutableIntStateOf(0) }

        Text(
            text = stringResource(
                R.string.version_codes,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE
            ),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 2.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (developerModeEnabled()) {
                        if (versionTaps == 0) {
                            AppSnackbar.show(
                                context.getString(R.string.developer_mode_already_enabled)
                            )
                            versionTaps++
                        }
                        return@clickable
                    }

                    versionTaps++
                    if (versionTaps >= 7) {
                        setDeveloperModeEnabled(true)
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        AppSnackbar.show(
                            context.getString(R.string.developer_mode_enabled)
                        )
                    }
                }
        )

        LinkButtonsCard(
            modifier = Modifier.padding(vertical = 24.dp)
        )

        SectionHeader(
            text = stringResource(R.string.meet_the_developer),
            animatedHeaders = animatedHeaders,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )

        DeveloperCard(
            modifier = Modifier.padding(bottom = 1.dp)
        )
    }
}

@Composable
private fun LinkButtonsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.alpha(0.8f)) {
            LinkButton(
                textResId = R.string.news,
                icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.Article),
                onClick = { context.openUrl("https://t.me/DrDsProjects") },
                modifier = Modifier.weight(1f)
            )
            LinkButtonDivider()
            LinkButton(
                textResId = R.string.support,
                icon = rememberVectorPainter(Icons.AutoMirrored.Rounded.Help),
                onClick = { context.openUrl("https://t.me/DrDsProjectsChat") },
                modifier = Modifier.weight(1f)
            )
            LinkButtonDivider()
            LinkButton(
                textResId = R.string.github,
                icon = painterResource(R.drawable.ic_github),
                onClick = { context.openUrl("https://github.com/Mahmud0808/ColorBlendr") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LinkButton(
    textResId: Int,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(dimensionResource(R.dimen.default_corner_radius)))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(dimensionResource(R.dimen.about_button_icon_size))
        )
        Text(
            text = stringResource(textResId),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun LinkButtonDivider() {
    Spacer(
        modifier = Modifier
            .padding(vertical = dimensionResource(R.dimen.container_margin_horizontal))
            .width(1.dp)
            .height(24.dp)
            .background(MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun DeveloperCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(dimensionResource(R.dimen.container_corner_radius)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { context.openUrl("https://github.com/Mahmud0808") }
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.img_drdisagree),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.dev_name),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.dev_bio),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(R.dimen.container_margin_horizontal))
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surface)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { context.openUrl("https://buymeacoffee.com/drdisagree") }
                    .padding(horizontal = 22.dp, vertical = 16.dp)
            ) {
                Box(modifier = Modifier.size(48.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.buymeacoffee_bg),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        painter = painterResource(R.drawable.buymeacoffee_fg),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.buymeacoffee_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.buymeacoffee_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CreditsItem(
    model: AboutAppModel,
    isFirst: Boolean,
    isLast: Boolean
) {
    val context = LocalContext.current
    val radius = dimensionResource(R.dimen.container_corner_radius)
    val radiusSmall = dimensionResource(R.dimen.container_corner_radius_small)
    val shape = when {
        isFirst && isLast -> RoundedCornerShape(radius)
        isFirst -> RoundedCornerShape(radius, radius, radiusSmall, radiusSmall)
        isLast -> RoundedCornerShape(radiusSmall, radiusSmall, radius, radius)
        else -> RoundedCornerShape(radiusSmall)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainer, shape = shape)
            .clip(shape)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { context.openUrl(model.url) }
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            Avatar(
                url = model.icon,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = model.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        if (!isLast) {
            Spacer(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(R.dimen.container_margin_horizontal))
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

private fun Context.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview
@Composable
private fun AboutScreenPreview() {
    ColorBlendrTheme {
        AboutScreen()
    }
}
