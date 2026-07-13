package me.jfenn.colorpickerdialog.compose.dialogs

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import me.jfenn.colorpickerdialog.compose.components.ImageColorPicker
import me.jfenn.colorpickerdialog.compose.theme.PickerColors

// Surface-colored 8dp-corner card: touch sampler above 64dp button bar; OK
// confirms sampled color; bitmap loads with Coil.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImageColorPickerDialog(
    imageUri: Uri,
    initialColor: Int,
    onDismissRequest: () -> Unit,
    onColorPicked: (Int) -> Unit
) {
    val context = LocalContext.current
    var color by rememberSaveable { mutableIntStateOf(initialColor) }
    var bitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri) {
        val result = SingletonImageLoader.get(context).execute(
            ImageRequest.Builder(context)
                .data(imageUri)
                .allowHardware(false)
                .build()
        )
        bitmap = result.image?.toBitmap()
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(PickerColors.colorSurface(context))
        ) {
            Column {
                bitmap?.let {
                    Box(
                        contentAlignment = Alignment.Center,
                        // fill=false keeps buttons on screen: image takes at
                        // most remaining height, aspect-fits.
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        ImageColorPicker(
                            bitmap = it,
                            onColorPicked = { sampled -> color = sampled }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                    Button(
                        onClick = { onColorPicked(color) },
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun ImageColorPickerDialogPreview() {
    ImageColorPickerDialog(
        imageUri = Uri.EMPTY,
        initialColor = 0xFF6750A4.toInt(),
        onDismissRequest = {},
        onColorPicked = {}
    )
}
