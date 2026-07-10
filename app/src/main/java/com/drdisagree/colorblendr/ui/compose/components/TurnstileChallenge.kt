package com.drdisagree.colorblendr.ui.compose.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
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

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(16.dp)
            ) {
                if (solving) {
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
                                        onToken(token)
                                    }
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
             data-callback="onToken"></div>
        <script>
            function onToken(token) { TurnstileBridge.onToken(token); }
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
