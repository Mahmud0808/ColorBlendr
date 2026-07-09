package com.drdisagree.colorblendr.ui.compose.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.ui.compose.theme.ColorBlendrTheme

// Circle-cropped remote avatar with crossfade, matching the old Glide
// setRoundImageUrl behavior from AboutAppAdapter.
@Composable
fun Avatar(
    url: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url.replace("http://", "https://"))
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.ic_user_account),
        error = painterResource(R.drawable.ic_user_account),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape)
    )
}

@Preview
@Composable
private fun AvatarPreview() {
    ColorBlendrTheme {
        Avatar(
            url = "",
            modifier = Modifier.size(48.dp)
        )
    }
}
