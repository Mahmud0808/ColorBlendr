package com.drdisagree.colorblendr.ui.compose.screens.about

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.drdisagree.colorblendr.BuildConfig
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.models.AboutAppModel
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.interop.Avatar
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.utils.app.parseContributors
import com.drdisagree.colorblendr.utils.app.parseTranslators

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val contributors = remember { parseContributors() }
    val translators = remember { parseTranslators() }
    val contributorsHeader = stringResource(R.string.contributors)
    val translatorsHeader = stringResource(R.string.translators)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.about_this_app_title),
                showBackButton = true,
                lifted = listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > 0
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                item { AboutAppHeader() }
                creditsSection(contributorsHeader, contributors)
                creditsSection(translatorsHeader, translators)
                item {
                    Spacer(
                        modifier = Modifier.height(dimensionResource(R.dimen.container_margin_bottom))
                    )
                }
            }
        }
    }
}

private fun LazyListScope.creditsSection(
    header: String,
    items: List<AboutAppModel>
) {
    val credits = items.filter { it.url.isNotEmpty() }

    if (header.isNotEmpty()) {
        item {
            Text(
                text = header,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
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
private fun AboutAppHeader() {
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
        )

        LinkButtonsCard(
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Text(
            text = stringResource(R.string.meet_the_developer),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
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
                iconResId = R.drawable.ic_news_scaled,
                onClick = { context.openUrl("https://t.me/DrDsProjects") },
                modifier = Modifier.weight(1f)
            )
            LinkButtonDivider()
            LinkButton(
                textResId = R.string.support,
                iconResId = R.drawable.ic_help_scaled,
                onClick = { context.openUrl("https://t.me/DrDsProjectsChat") },
                modifier = Modifier.weight(1f)
            )
            LinkButtonDivider()
            LinkButton(
                textResId = R.string.github,
                iconResId = R.drawable.ic_github_scaled,
                onClick = { context.openUrl("https://github.com/Mahmud0808/ColorBlendr") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LinkButton(
    textResId: Int,
    iconResId: Int,
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
            painter = painterResource(iconResId),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(16.dp)
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
