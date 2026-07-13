package me.jfenn.colorpickerdialog.compose.pickers

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jfenn.colorpickerdialog.R
import me.jfenn.colorpickerdialog.compose.components.AllPhotosTile
import me.jfenn.colorpickerdialog.compose.components.ImageTile
import me.jfenn.colorpickerdialog.compose.theme.PickerColors

// 300dp area: storage-permission gate or 3-column device image grid behind
// "All Photos" tile (system photo picker); tapping image hands uri to caller
// for sampling.
@Composable
internal fun ImagePickerPage(
    onImageSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var permissionGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(mediaPermission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> permissionGranted = granted }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(onImageSelected) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (!permissionGranted) {
            val textColorSecondary = remember(context) {
                Color(PickerColors.textColorSecondary(context))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(R.string.colorPickerDialog_msg_grant_permissions),
                    color = textColorSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 64.dp)
                        .padding(bottom = 16.dp)
                )
                Button(onClick = { permissionLauncher.launch(mediaPermission) }) {
                    Text(text = stringResource(R.string.colorPickerDialog_grant_permissions))
                }
            }
        } else {
            val imageUris = remember { queryDeviceImages(context) }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    top = 8.dp,
                    bottom = 126.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    AllPhotosTile(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    )
                }
                items(imageUris) { uri ->
                    ImageTile(
                        uri = uri,
                        onClick = { onImageSelected(uri) }
                    )
                }
            }
        }
    }
}

private fun queryDeviceImages(context: Context): List<Uri> {
    val uris = mutableListOf<Uri>()
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        arrayOf(MediaStore.Images.Media._ID),
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )?.use { cursor ->
        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            uris.add(
                ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(idIndex)
                )
            )
        }
    }
    return uris
}

@Preview(showBackground = true)
@Composable
private fun ImagePickerPagePreview() {
    ImagePickerPage(
        onImageSelected = {}
    )
}
