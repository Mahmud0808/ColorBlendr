package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// Submits a theme payload to the worker, which opens a moderated PR on the
// themes repo. Returns the PR url, or null on any failure.
object CommunityUploader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun upload(payload: JSONObject, turnstileToken: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject()
                    .put("payload", payload)
                    .put("turnstileToken", turnstileToken)
                    .toString()
                    .toRequestBody("application/json".toMediaType())

                client.newCall(
                    Request.Builder().url("$COMMUNITY_WORKER_URL/upload").post(body).build()
                ).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    JSONObject(response.body.string())
                        .optString("prUrl").takeIf { it.isNotEmpty() }
                }
            } catch (_: Exception) {
                null
            }
        }
}
