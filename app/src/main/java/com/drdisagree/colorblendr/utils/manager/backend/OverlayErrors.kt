package com.drdisagree.colorblendr.utils.manager.backend

import androidx.annotation.StringRes
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.R
import com.drdisagree.colorblendr.data.domain.ThemingErrorReporter

object OverlayErrors {

    fun report(@StringRes message: Int) = ThemingErrorReporter.report(appContext.getString(message))

    fun reportOperation(detail: String) =
        ThemingErrorReporter.report(appContext.getString(R.string.error_overlay_operation, detail))

    fun reportOperation(error: Exception) = reportOperation(error.message ?: error.javaClass.simpleName)
}
