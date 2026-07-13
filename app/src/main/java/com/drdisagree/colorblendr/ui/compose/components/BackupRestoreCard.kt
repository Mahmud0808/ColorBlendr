package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme
import com.drdisagree.colorblendr.ui.compose.theme.themeAttrColor
import android.R as AndroidR
import com.google.android.material.R as MaterialR

// Tap crossfades backup/restore button row in/out (config_mediumAnimTime).
@Composable
fun BackupRestoreCard(
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
    position: WidgetPosition = WidgetPosition.Single
) {
    var buttonsVisible by remember { mutableStateOf(false) }
    val mediumAnimTime = LocalContext.current.resources
        .getInteger(AndroidR.integer.config_mediumAnimTime)

    PositionedCard(
        position = position,
        onClick = { buttonsVisible = !buttonsVisible },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.AttachFile),
                    contentDescription = null,
                    tint = themeAttrColor(MaterialR.attr.colorPrimaryVariant),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.backup_restore_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.backup_restore_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = buttonsVisible,
                enter = expandVertically(
                    MaterialTheme.motionScheme.defaultSpatialSpec()
                ) + fadeIn(tween(mediumAnimTime)),
                exit = shrinkVertically(
                    MaterialTheme.motionScheme.defaultSpatialSpec()
                ) + fadeOut(tween(mediumAnimTime))
            ) {
                Row(modifier = Modifier.padding(top = 12.dp)) {
                    Button(
                        onClick = onBackup,
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = ButtonDefaults.ContentPadding,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Save),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(R.string.backup))
                    }
                    Button(
                        onClick = onRestore,
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = ButtonDefaults.ContentPadding,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Restore),
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(text = stringResource(R.string.restore))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun BackupRestoreCardPreview() {
    ColorBlendrTheme {
        BackupRestoreCard(onBackup = {}, onRestore = {})
    }
}
