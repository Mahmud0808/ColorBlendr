package com.drdisagree.colorblendr.dev.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import com.drdisagree.colorblendr.dev.data.models.PreviewResult

object ThemeForwarder {

    private const val COLORBLENDR_PACKAGE = "com.drdisagree.colorblendr"

    fun openPreview(context: Context, payloadJson: String): PreviewResult {
        if (!isInstalled(context)) return PreviewResult.NOT_INSTALLED

        val encoded = Base64.encodeToString(
            payloadJson.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        val uri: Uri = "colorblendr://preview?data=$encoded".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage(COLORBLENDR_PACKAGE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        return try {
            context.startActivity(intent)
            PreviewResult.SUCCESS
        } catch (_: SecurityException) {
            PreviewResult.SIGNATURE_MISMATCH
        } catch (_: ActivityNotFoundException) {
            PreviewResult.NO_ACTIVITY
        } catch (_: Exception) {
            PreviewResult.ERROR
        }
    }

    private fun isInstalled(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(COLORBLENDR_PACKAGE, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}
