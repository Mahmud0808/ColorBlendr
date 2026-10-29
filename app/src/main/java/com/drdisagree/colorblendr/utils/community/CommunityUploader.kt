package com.drdisagree.colorblendr.utils.community

import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

// Submits a theme payload to the worker's review queue; the owner approves
// before anything reaches GitHub. Returns false on any failure.
object CommunityUploader {

    private val client = CommunityHttp.withCallTimeout(45)

    private val _uploading = MutableStateFlow(false)
    val uploading = _uploading.asStateFlow()

    private val _submissions = MutableStateFlow(0)
    val submissions = _submissions.asStateFlow()

    suspend fun upload(payload: JSONObject, turnstileToken: String): Boolean =
        withContext(Dispatchers.IO) {
            _uploading.value = true
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
                }.also { queued -> if (queued) _submissions.update { it + 1 } }
            } catch (_: Exception) {
                false
            } finally {
                _uploading.value = false
            }
        }
}
