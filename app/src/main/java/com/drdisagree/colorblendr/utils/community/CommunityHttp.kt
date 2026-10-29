package com.drdisagree.colorblendr.utils.community

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object CommunityHttp {

    private val baseClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    fun withCallTimeout(seconds: Long): OkHttpClient = baseClient.newBuilder()
        .callTimeout(seconds, TimeUnit.SECONDS)
        .build()
}
