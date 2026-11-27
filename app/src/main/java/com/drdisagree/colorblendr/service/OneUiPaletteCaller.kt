package com.drdisagree.colorblendr.service

import android.content.om.IOverlayManager
import android.system.Os
import rikka.shizuku.SystemServiceHelper
import kotlin.system.exitProcess

object OneUiPaletteCaller {

    const val RESULT_OK = "ONE_UI_PALETTE_OK"
    const val ACTION_APPLY = "apply"
    const val ACTION_CLEAR = "clear"
    const val METHOD_NAME = "applyWallpaperColor"

    private const val SYSTEM_UID = 1000

    @JvmStatic
    fun main(args: Array<String>) {
        val error = runCatching { call(args) }.exceptionOrNull()
        println(if (error == null) RESULT_OK else (error.cause ?: error).toString())
        exitProcess(0)
    }

    private fun call(args: Array<String>) {
        Os.setgid(SYSTEM_UID)
        Os.setuid(SYSTEM_UID)

        val overlayManager = IOverlayManager.Stub.asInterface(
            SystemServiceHelper.getSystemService("overlay")
        )
        val method = overlayManager.javaClass.getMethod(
            METHOD_NAME,
            List::class.java,
            List::class.java,
            Boolean::class.javaPrimitiveType
        )

        when (args.firstOrNull()) {
            ACTION_APPLY -> method.invoke(
                overlayManager,
                parseColors(args[1]),
                parseColors(args[2]),
                args[3].toBooleanStrict()
            )

            ACTION_CLEAR -> method.invoke(overlayManager, null, null, false)
            else -> error("Unknown action: ${args.firstOrNull()}")
        }
    }

    private fun parseColors(value: String): ArrayList<Int> =
        value.split(",").mapTo(ArrayList()) { it.trim().toInt() }
}
