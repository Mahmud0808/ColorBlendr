package com.drdisagree.colorblendr.dev.ui.navigation

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{submissionId}"
    const val ARG_SUBMISSION_ID = "submissionId"

    fun detail(submissionId: String): String = "detail/$submissionId"
}
