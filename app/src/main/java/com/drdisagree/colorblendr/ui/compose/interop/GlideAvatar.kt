package com.drdisagree.colorblendr.ui.compose.interop

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.drdisagree.colorblendr.R

// Glide 5 has no Compose artifact; wrap an ImageView to keep the exact
// crossfade + circleCrop behavior of AboutAppAdapter.setRoundImageUrl.
@Composable
fun GlideAvatar(
    url: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context -> ImageView(context) },
        update = { imageView ->
            Glide.with(imageView.context)
                .load(url.replace("http://", "https://"))
                .apply(RequestOptions.centerCropTransform())
                .apply(RequestOptions().override(48, 48))
                .placeholder(R.drawable.ic_user_account)
                .error(R.drawable.ic_user_account)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        },
        modifier = modifier
    )
}
