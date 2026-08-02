package com.drdisagree.colorblendr.dev.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.colorblendr.dev.R
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import com.drdisagree.colorblendr.dev.ui.components.BlockReasonDialog
import com.drdisagree.colorblendr.dev.ui.components.ConfirmDialog
import com.drdisagree.colorblendr.dev.ui.components.DeviceChip
import com.drdisagree.colorblendr.dev.ui.theme.DevTheme
import com.drdisagree.colorblendr.dev.utils.ThemePayloadDecoder
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmissionDetailScreen(
    item: PendingSubmission,
    onBack: () -> Unit,
    onPreview: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onBlock: (String) -> Unit
) {
    val payload = remember(item.payloadJson) { ThemePayloadDecoder.decode(item.payloadJson) }
    var confirmApprove by remember { mutableStateOf(false) }
    var confirmReject by remember { mutableStateOf(false) }
    var showBlock by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val lifted by remember { derivedStateOf { scrollState.value > 0 } }
    val toolbarColor by animateColorAsState(
        targetValue = if (lifted) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "toolbarLift"
    )

    if (showBlock) {
        BlockReasonDialog(
            target = item,
            onDismiss = { showBlock = false },
            onConfirm = { reason ->
                showBlock = false
                onBlock(reason)
            }
        )
    }

    if (confirmApprove) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_approve_title),
            message = stringResource(R.string.confirm_approve_message),
            confirmLabel = stringResource(R.string.approve),
            destructive = false,
            onConfirm = {
                confirmApprove = false
                onApprove()
            },
            onDismiss = { confirmApprove = false }
        )
    }

    if (confirmReject) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_reject_title),
            message = stringResource(R.string.confirm_reject_message),
            confirmLabel = stringResource(R.string.reject),
            destructive = true,
            onConfirm = {
                confirmReject = false
                onReject()
            },
            onDismiss = { confirmReject = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.submission)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = toolbarColor,
                    scrolledContainerColor = toolbarColor
                ),
                navigationIcon = {
                    IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                        Icon(
                            painter = rememberVectorPainter(Icons.AutoMirrored.Rounded.ArrowBack),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    FilledTonalIconButton(
                        onClick = { showBlock = true },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            painter = rememberVectorPainter(Icons.Rounded.Block),
                            contentDescription = stringResource(R.string.block_uploader)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(bottom = innerPadding.calculateBottomPadding() + 20.dp)
        ) {
            val heroColor = item.seedColor?.let { Color(it) }
                ?: MaterialTheme.colorScheme.primary

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(heroColor.copy(alpha = 0.9f), heroColor.copy(alpha = 0.35f))
                        ),
                        RoundedCornerShape(28.dp)
                    )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-14).dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    listOfNotNull(
                        item.seedColor, item.secondaryColor, item.tertiaryColor
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .padding(3.dp)
                                .background(Color(color), CircleShape)
                        )
                    }
                }
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = "${item.author.ifEmpty { stringResource(R.string.anonymous) }} · " +
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(item.created)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(modifier = Modifier.padding(top = 10.dp)) {
                DeviceChip(device = item.device)
            }

            if (payload != null && payload.description.isNotEmpty()) {
                Text(
                    text = payload.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                if (payload != null) {
                    InfoRow(stringResource(R.string.detail_style), prettyStyle(payload.style))
                    InfoRow(stringResource(R.string.detail_spec), specLabel(payload.colorSpecVersion))
                    InfoRow(
                        stringResource(R.string.detail_accent_sat),
                        payload.accentSaturation.toString()
                    )
                    InfoRow(
                        stringResource(R.string.detail_bg_sat),
                        payload.backgroundSaturation.toString()
                    )
                    InfoRow(
                        stringResource(R.string.detail_bg_light),
                        payload.backgroundLightness.toString()
                    )
                    if (payload.modeSpecificThemes) {
                        InfoRow(
                            stringResource(R.string.detail_accent_sat_light),
                            payload.accentSaturationLight.toString()
                        )
                        InfoRow(
                            stringResource(R.string.detail_bg_sat_light),
                            payload.backgroundSaturationLight.toString()
                        )
                        InfoRow(
                            stringResource(R.string.detail_bg_light_light),
                            payload.backgroundLightnessLight.toString()
                        )
                    }
                    if (payload.overrideCount > 0) {
                        InfoRow(
                            stringResource(R.string.detail_overrides),
                            payload.overrideCount.toString()
                        )
                    }
                }
                item.seedColor?.let {
                    ColorInfoRow(stringResource(R.string.detail_seed), it)
                }
                item.secondaryColor?.let {
                    ColorInfoRow(stringResource(R.string.detail_secondary), it)
                }
                item.tertiaryColor?.let {
                    ColorInfoRow(stringResource(R.string.detail_tertiary), it)
                }
                if (payload != null) {
                    BoolInfoRow(stringResource(R.string.detail_pitch_black), payload.pitchBlack)
                    BoolInfoRow(stringResource(R.string.detail_tint_text), payload.tintText)
                    BoolInfoRow(
                        stringResource(R.string.detail_accurate_shades),
                        payload.accurateShades
                    )
                    BoolInfoRow(
                        stringResource(R.string.detail_mode_specific),
                        payload.modeSpecificThemes
                    )
                }
                InfoRow(stringResource(R.string.detail_id), item.id)
            }

            ElevatedButton(
                onClick = onPreview,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            ) {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.Visibility),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = stringResource(R.string.preview))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Button(
                    onClick = { confirmApprove = true },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Check),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.approve))
                }
                FilledTonalButton(
                    onClick = { confirmReject = true },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Rounded.Close),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = stringResource(R.string.reject))
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ColorInfoRow(label: String, color: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color(color), RoundedCornerShape(6.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(6.dp)
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = hex(color),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BoolInfoRow(label: String, enabled: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = rememberVectorPainter(
                if (enabled) Icons.Rounded.Check else Icons.Rounded.Close
            ),
            contentDescription = null,
            tint = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun prettyStyle(style: String): String =
    style.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

private fun specLabel(version: Int): String = when (version) {
    0 -> "2021"
    1 -> "2025"
    2 -> "2026"
    else -> "v$version"
}

private fun hex(color: Int): String = "#%06X".format(0xFFFFFF and color)

@Preview
@Composable
private fun SubmissionDetailScreenPreview() {
    DevTheme {
        SubmissionDetailScreen(
            item = PendingSubmission(
                id = "ocean-breeze-abc123",
                name = "Ocean Breeze",
                author = "DrDisagree",
                device = "a1b2c3d4e5f6",
                created = 1752800000000L,
                seedColor = 0xFF51BDFF.toInt(),
                secondaryColor = 0xFF7C4DFF.toInt(),
                tertiaryColor = 0xFF26A69A.toInt(),
                payloadJson = """
                    {"description":"Cool ocean tones","style":"TONAL_SPOT",
                    "colorSpecVersion":2,"accentSaturation":120,"backgroundSaturation":90,
                    "backgroundLightness":100,"pitchBlack":true,"tintText":true,
                    "accurateShades":true}
                """.trimIndent()
            ),
            onBack = {},
            onPreview = {},
            onApprove = {},
            onReject = {},
            onBlock = {}
        )
    }
}
