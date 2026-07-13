package com.drdisagree.colorblendr.ui.compose.screens.privacypolicy

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.ui.compose.components.PositionedCard
import com.drdisagree.colorblendr.ui.compose.components.WidgetPosition
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import java.io.IOException

private const val TAG = "PrivacyPolicyScreen"

// Parsed from the asset: preamble (intro card) + underlined sections (cards).
private data class PolicySection(
    val title: String,
    val body: String
)

private data class PolicyDocument(
    val intro: String,
    val sections: List<PolicySection>
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PrivacyPolicyScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }
    val document = remember(context) {
        parsePolicy(loadPrivacyPolicyFromAssets(context))
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.privacy_policy),
                showBackButton = true,
                lifted = toolbarLifted
            )
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = LocalPreviewBottomInset.current)
                        .padding(top = 8.dp)
                ) {
                    PolicyHeader(intro = document.intro)

                    document.sections.forEachIndexed { index, section ->
                        PolicySectionCard(
                            section = section,
                            position = when {
                                document.sections.size == 1 -> WidgetPosition.Single
                                index == 0 -> WidgetPosition.Top
                                index == document.sections.lastIndex -> WidgetPosition.Bottom
                                else -> WidgetPosition.Middle
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PolicyHeader(intro: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialShapes.Cookie9Sided.toShape()
                )
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.VerifiedUser),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp)
            )
        }
        if (intro.isNotEmpty()) {
            Text(
                text = annotatedBody(
                    text = intro,
                    linkColor = MaterialTheme.colorScheme.primary
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}

@Composable
private fun PolicySectionCard(
    section: PolicySection,
    position: WidgetPosition
) {
    PositionedCard(position = position) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = annotatedBody(
                    text = section.body,
                    linkColor = MaterialTheme.colorScheme.primary
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun loadPrivacyPolicyFromAssets(context: Context): String {
    return try {
        context.assets.open("privacy_policy.txt").bufferedReader().use { it.readText().trim() }
    } catch (e: IOException) {
        Log.e(TAG, "Error loading privacy policy: ${e.message}")
        "Unable to load privacy policy."
    }
}

// Splits on "-----"-underlined titles; "====="-underlined doc title dropped
// (toolbar already shows it). Text before first section = intro.
private fun parsePolicy(text: String): PolicyDocument {
    val lines = text.lines()
    val sections = mutableListOf<PolicySection>()
    val intro = StringBuilder()
    var currentTitle: String? = null
    val currentBody = StringBuilder()

    fun flush() {
        currentTitle?.let { title ->
            sections.add(PolicySection(title, currentBody.toString().trim()))
        }
        currentBody.clear()
    }

    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val underline = lines.getOrNull(i + 1)?.trim()

        when {
            line.isNotBlank() && underline != null &&
                    underline.length >= 3 && underline.all { it == '=' } -> i++ // skip doc title

            line.isNotBlank() && underline != null &&
                    underline.length >= 3 && underline.all { it == '-' } -> {
                flush()
                currentTitle = line.trim()
                i++
            }

            currentTitle == null -> intro.appendLine(line)
            else -> currentBody.appendLine(line)
        }
        i++
    }
    flush()

    return PolicyDocument(intro.toString().trim(), sections)
}

// **bold** spans + clickable https links.
private fun annotatedBody(text: String, linkColor: Color): AnnotatedString {
    val bold = markdownToBold(text)
    val urlRegex = "https://\\S+".toRegex()

    return buildAnnotatedString {
        var lastIndex = 0
        urlRegex.findAll(bold.text).forEach { match ->
            append(bold.subSequence(lastIndex, match.range.first))
            withLink(
                LinkAnnotation.Url(
                    url = match.value,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                )
            ) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }
        append(bold.subSequence(lastIndex, bold.length))
    }
}

private fun markdownToBold(text: String): AnnotatedString = buildAnnotatedString {
    val regex = "\\*\\*(.*?)\\*\\*".toRegex()
    var lastIndex = 0

    regex.findAll(text).forEach { match ->
        append(text.substring(lastIndex, match.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        lastIndex = match.range.last + 1
    }
    append(text.substring(lastIndex))
}

@Preview
@Composable
private fun PrivacyPolicyScreenPreview() {
    ColorBlendrTheme {
        PrivacyPolicyScreen()
    }
}
