package com.drdisagree.colorblendr.ui.compose.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import com.drdisagree.colorblendr.data.common.Constant.TURNSTILE_SITE_KEY
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Cloudflare Turnstile inside a small WebView dialog; usually solves
// invisibly and calls back with a token, occasionally shows a checkbox.
// Loaded against the worker origin (must be listed in the widget's domains).
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TurnstileChallenge(
    onToken: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var solving by remember { mutableStateOf(true) }
    var interactive by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = stringResource(
                        if (interactive) {
                            R.string.complete_verification
                        } else {
                            R.string.verifying_human
                        }
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Spinner while solving invisibly; hidden once the widget
                    // shows an interactive challenge (checkbox).
                    if (solving && !interactive) {
                        ContainedLoadingIndicator()
                    }

                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                setBackgroundColor(0)
                                webViewClient = WebViewClient()
                                addJavascriptInterface(object {
                                    @JavascriptInterface
                                    fun onToken(token: String) {
                                        post {
                                            solving = false
                                            interactive = false
                                            onToken(token)
                                        }
                                    }

                                    @JavascriptInterface
                                    fun onInteractive() {
                                        post { interactive = true }
                                    }
                                }, "TurnstileBridge")

                                loadDataWithBaseURL(
                                    COMMUNITY_WORKER_URL,
                                    TURNSTILE_HTML,
                                    "text/html",
                                    "utf-8",
                                    null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private val TURNSTILE_HTML = """
    <!doctype html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <script src="https://challenges.cloudflare.com/turnstile/v0/api.js" async defer></script>
        <style>body { margin: 0; display: flex; justify-content: center; background: transparent; }</style>
    </head>
    <body>
        <div class="cf-turnstile"
             data-sitekey="$TURNSTILE_SITE_KEY"
             data-callback="onToken"
             data-before-interactive-callback="onInteractive"></div>
        <script>
            function onToken(token) { TurnstileBridge.onToken(token); }
            function onInteractive() { TurnstileBridge.onInteractive(); }
        </script>
    </body>
    </html>
""".trimIndent()

@Preview
@Composable
private fun TurnstileChallengePreview() {
    ColorBlendrTheme {
        TurnstileChallenge(
            onToken = {},
            onDismiss = {}
        )
    }
}
