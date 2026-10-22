package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// Submits a theme payload to the worker's review queue; the owner approves
// before anything reaches GitHub. Returns false on any failure.
object CommunityUploader {

    private val client = CommunityHttp.withCallTimeout(45)

    suspend fun upload(payload: JSONObject, turnstileToken: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject()
                    .put("payload", payload)
                    .put("turnstileToken", turnstileToken)
                    .put("device", CommunityVotes.deviceHash())
                    .toString()
                    .toRequestBody("application/json".toMediaType())

                client.newCall(
                    Request.Builder().url("$COMMUNITY_WORKER_URL/upload").post(body).build()
                ).execute().use { response ->
                    if (!response.isSuccessful) return@withContext false
                    JSONObject(response.body.string()).optBoolean("queued")
                }
            } catch (_: Exception) {
                false
            }
        }
}
