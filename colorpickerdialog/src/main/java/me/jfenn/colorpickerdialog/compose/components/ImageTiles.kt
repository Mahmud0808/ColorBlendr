package me.jfenn.colorpickerdialog.compose.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import me.jfenn.colorpickerdialog.R
import me.jfenn.colorpickerdialog.compose.theme.PickerColors

// Compose port of colorpicker_item_image.xml: 4dp-corner square thumbnail
// with 4dp margin, center-cropped, crossfaded in (Glide transition parity,
// loaded with Coil instead).
@Composable
internal fun ImageTile(
    uri: Uri,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AsyncImage(
        model = remember(uri) {
            ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build()
        },
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    )
}

// Compose port of colorpicker_item_image_select.xml: dimmed rounded square
// with the "add a photo" icon and the All Photos label.
@Composable
internal fun AllPhotosTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColorPrimary = remember(context) { Color(PickerColors.textColorPrimary(context)) }
    val textColorSecondary = remember(context) { Color(PickerColors.textColorSecondary(context)) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .alpha(0.2f)
                .background(textColorPrimary)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_a_photo),
                contentDescription = null,
                tint = textColorSecondary,
                modifier = Modifier
                    .size(36.dp)
                    .alpha(0.5f)
            )
            Text(
                text = stringResource(R.string.colorPickerDialog_title_all_photos),
                color = textColorSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageTilesPreview() {
    Row(modifier = Modifier.fillMaxWidth()) {
        AllPhotosTile(
            onClick = {},
            modifier = Modifier.size(120.dp)
        )
        ImageTile(
            uri = Uri.EMPTY,
            onClick = {},
            modifier = Modifier.size(120.dp)
        )
    }
}
