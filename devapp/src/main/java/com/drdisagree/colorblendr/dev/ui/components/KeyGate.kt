package com.drdisagree.colorblendr.dev.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme

@Composable
fun KeyGate(
    adminKey: String,
    onKeyChange: (String) -> Unit,
    loading: Boolean,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Palette),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(44.dp)
            )
        }
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            text = stringResource(R.string.moderation_console),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        var revealed by rememberSaveable { mutableStateOf(false) }
        OutlinedTextField(
            shape = RoundedCornerShape(20.dp),
            value = adminKey,
            onValueChange = { onKeyChange(it.filterNot(Char::isISOControl)) },
            label = { Text(text = stringResource(R.string.admin_key)) },
            singleLine = true,
            enabled = !loading,
            visualTransformation = if (revealed) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(
                onGo = { if (adminKey.isNotBlank()) onUnlock() }
            ),
            trailingIcon = {
                IconButton(onClick = { revealed = !revealed }) {
                    Icon(
                        painter = rememberVectorPainter(
                            if (revealed) Icons.Rounded.VisibilityOff
                            else Icons.Rounded.Visibility
                        ),
                        contentDescription = stringResource(
                            if (revealed) R.string.hide_key else R.string.reveal_key
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        )
        Button(
            onClick = onUnlock,
            enabled = !loading && adminKey.isNotBlank(),
            shapes = ButtonDefaults.shapes(),
            contentPadding = ButtonDefaults.contentPaddingFor(56.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .height(56.dp)
        ) {
            if (loading) {
                LoadingIndicator(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(text = stringResource(R.string.unlock))
            }
        }
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Preview
@Composable
private fun KeyGatePreview() {
    DevTheme {
        KeyGate(
            adminKey = "",
            onKeyChange = {},
            loading = false,
            onUnlock = {}
        )
    }
}