package com.drdisagree.colorblendr.utils.colors

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.google.android.material.color.PreviewColorResourcesLoader
import java.lang.ref.WeakReference

// Remaps the framework system color resources on the activity while a preview
// is active, so view-based UI (MDC dialogs, the color picker) follows the
// previewed colors just like the Compose theme does.
object PreviewResourcesOverride {

    private val TAG = PreviewResourcesOverride::class.java.simpleName

    private var loader: ResourcesLoader? = null
    private var loaderResources: WeakReference<Resources>? = null
    private val resourceIdCache = HashMap<String, Int>()

    // Bumped after every loader change; composables resolving theme attrs
    // read this so they re-resolve once the override is actually in place
    // (or removed) instead of racing the loader swap.
    var revision by mutableIntStateOf(0)
        private set

    fun apply(activity: Activity, frameworkColors: Map<String, Int>?) {
        val resources = activity.resources
        val loader = loaderFor(resources)

        val colorValues = frameworkColors?.let { buildColorValues(resources, it) }.orEmpty()
        val built = if (colorValues.isEmpty()) {
            null
        } else {
            try {
                PreviewColorResourcesLoader.create(activity, colorValues)
            } catch (e: Exception) {
                Log.e(TAG, "Error overriding preview resources", e)
                null
            }
        }
        val providers = built?.providers.orEmpty()

        val oldProviders = loader.providers
        if (providers.isEmpty() && oldProviders.isEmpty()) return

        loader.setProviders(providers)
        built?.clearProviders()
        oldProviders.forEach { runCatching { it.close() } }

        // Theme objects cache resolved attributes; without a rebase some
        // of them keep serving the removed preview values.
        activity.theme.rebase()
        revision++
    }

    private fun loaderFor(resources: Resources): ResourcesLoader {
        loader?.takeIf { loaderResources?.get() === resources }?.let { return it }
        return ResourcesLoader().also {
            resources.addLoaders(it)
            loader = it
            loaderResources = WeakReference(resources)
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun buildColorValues(resources: Resources, frameworkColors: Map<String, Int>): Map<Int, Int> {
        val colorValues = HashMap<Int, Int>()

        frameworkColors.forEach { (name, color) ->
            val id = resourceIdCache.getOrPut(name) {
                resources.getIdentifier(name, "color", "android")
            }
            if (id != 0) colorValues[id] = color
        }

        return colorValues
    }
}
