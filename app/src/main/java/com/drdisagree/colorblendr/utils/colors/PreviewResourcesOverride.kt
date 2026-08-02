package com.drdisagree.colorblendr.utils.colors

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.drdisagree.colorblendr.data.domain.PreviewController.PreviewColors
import com.drdisagree.colorblendr.utils.app.SystemUtil
import com.drdisagree.colorblendr.utils.colors.ColorUtil.systemPaletteNames
import com.google.android.material.color.PreviewColorResourcesLoader

// Remaps the framework system color resources on the activity while a preview
// is active, so view-based UI (MDC dialogs, the color picker) follows the
// previewed colors just like the Compose theme does.
object PreviewResourcesOverride {

    private val TAG = PreviewResourcesOverride::class.java.simpleName

    private var currentLoader: ResourcesLoader? = null
    private val resourceIdCache = HashMap<String, Int>()

    // Bumped after every loader change; composables resolving theme attrs
    // read this so they re-resolve once the override is actually in place
    // (or removed) instead of racing the loader swap.
    var revision by mutableIntStateOf(0)
        private set

    fun apply(activity: Activity, previewColors: PreviewColors?) {
        val resources = activity.resources

        currentLoader?.let {
            try {
                resources.removeLoaders(it)
            } catch (_: Exception) {
            }
            currentLoader = null
        }

        if (previewColors == null) {
            // Theme objects cache resolved attributes; without a rebase some
            // of them keep serving the removed preview values.
            activity.theme.rebase()
            revision++
            return
        }

        val colorValues = buildColorValues(resources, previewColors)
        if (colorValues.isEmpty()) return

        try {
            PreviewColorResourcesLoader.create(activity, colorValues)?.let {
                resources.addLoaders(it)
                currentLoader = it
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error overriding preview resources", e)
        }
        activity.theme.rebase()
        revision++
    }

    @SuppressLint("DiscouragedApi")
    private fun buildColorValues(
        resources: Resources,
        previewColors: PreviewColors
    ): Map<Int, Int> {
        val colorValues = HashMap<Int, Int>()
        val palette = if (SystemUtil.isDarkMode) {
            previewColors.paletteDark
        } else {
            previewColors.paletteLight
        }

        fun colorId(name: String): Int = resourceIdCache.getOrPut(name) {
            resources.getIdentifier(name, "color", "android")
        }

        systemPaletteNames.forEachIndexed { i, row ->
            row.forEachIndexed { j, name ->
                val id = colorId(name)
                if (id != 0) colorValues[id] = palette[i][j]
            }
        }

        (previewColors.lightMap.entries + previewColors.darkMap.entries)
            .forEach { (name, color) ->
                val id = colorId(name)
                if (id != 0) colorValues[id] = color
            }

        return colorValues
    }
}
