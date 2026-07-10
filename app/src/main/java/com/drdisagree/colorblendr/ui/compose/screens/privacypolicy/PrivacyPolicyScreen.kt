package com.drdisagree.colorblendr.ui.compose.screens.privacypolicy

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import java.io.IOException

private const val TAG = "PrivacyPolicyScreen"

@Composable
fun PrivacyPolicyScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }
    val policyText = remember(context) {
        markdownToBold(loadPrivacyPolicyFromAssets(context))
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = LocalPreviewBottomInset.current)
            ) {
                SelectionContainer {
                    Text(
                        text = policyText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.container_margin_horizontal))
                    )
                }
            }
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
