package com.drdisagree.colorblendr.ui.compose.screens.about

import android.content.ClipData
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.components.AppSnackbar
import com.drdisagree.colorblendr.ui.compose.components.AppToolbar
import com.drdisagree.colorblendr.ui.compose.components.ExpressiveEmptyState
import com.drdisagree.colorblendr.ui.compose.components.LocalPreviewBottomInset
import com.drdisagree.colorblendr.ui.compose.components.contentWidthLimit
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.utils.app.CrashLogger
import androidx.compose.material.icons.rounded.BugReport
import kotlinx.coroutines.launch

// Dev-mode only: crashes captured locally by CrashLogger, newest first.
@Composable
fun CrashLogScreen() {
    val context = LocalContext.current
    val inspection = LocalInspectionMode.current
    val scrollState = rememberScrollState()
    val toolbarLifted by remember { derivedStateOf { scrollState.value > 0 } }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var log by remember {
        mutableStateOf(if (inspection) "" else CrashLogger.read(context))
    }
    val copiedText = stringResource(R.string.crash_log_copied)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            AppToolbar(
                title = stringResource(R.string.crash_log_title),
                showBackButton = true,
                lifted = toolbarLifted,
                actions = {
                    if (log.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText("crash log", log)
                                        )
                                    )
                                    AppSnackbar.show(copiedText)
                                }
                            }
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Rounded.ContentCopy),
                                contentDescription = stringResource(R.string.crash_log_copy),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                CrashLogger.clear(context)
                                log = ""
                            }
                        ) {
                            Icon(
                                painter = rememberVectorPainter(Icons.Rounded.DeleteOutline),
                                contentDescription = stringResource(R.string.crash_log_clear),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )

            if (log.isEmpty()) {
                ExpressiveEmptyState(
                    icon = Icons.Rounded.BugReport,
                    title = stringResource(R.string.crash_log_empty),
                    description = stringResource(R.string.crash_log_empty_desc),
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                SelectionContainer {
                    Text(
                        text = log,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxSize()
                            .contentWidthLimit()
                            .verticalScroll(scrollState)
                            .horizontalScroll(rememberScrollState())
                            .padding(16.dp)
                            .padding(bottom = LocalPreviewBottomInset.current)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CrashLogScreenPreview() {
    ColorBlendrTheme {
        CrashLogScreen()
    }
}
