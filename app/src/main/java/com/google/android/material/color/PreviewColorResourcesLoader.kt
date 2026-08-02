package com.google.android.material.color

import android.content.Context
import android.content.res.loader.ResourcesLoader

// Accessor for the package-private ColorResourcesLoaderCreator, used to build
// a ResourcesLoader that remaps system colors for the in-app color preview.
object PreviewColorResourcesLoader {

    fun create(context: Context, colorValues: Map<Int, Int>): ResourcesLoader? =
        ColorResourcesLoaderCreator.create(context, colorValues)
}
