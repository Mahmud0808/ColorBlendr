package com.drdisagree.colorblendr.dev.data.api

import androidx.core.graphics.toColorInt
import com.drdisagree.colorblendr.dev.data.models.BlockedEntry
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AdminApi {

    private const val WORKER_URL = "https://colorblendr-themes.drdisagree.workers.dev"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchPending(adminKey: String): List<PendingSubmission>? =
        withContext(Dispatchers.IO) {
            try {
                client.newCall(get("pending", adminKey)).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val array = JSONObject(response.body.string()).optJSONArray("pending")
                        ?: return@withContext null
                    (0 until array.length()).mapNotNull { i ->
                        val entry = array.optJSONObject(i) ?: return@mapNotNull null
                        val id = entry.optString("id").takeIf { it.isNotEmpty() }
                            ?: return@mapNotNull null
                        val payload = entry.optJSONObject("payload") ?: JSONObject()
                        PendingSubmission(
                            id = id,
                            name = entry.optString("name"),
                            author = entry.optString("author"),
                            device = entry.optString("device"),
                            created = entry.optLong("created"),
                            seedColor = payload.color("seedColor"),
                            secondaryColor = payload.color("secondaryColor"),
                            tertiaryColor = payload.color("tertiaryColor"),
                            payloadJson = payload.toString()
                        )
                    }
                }
            } catch (_: Exception) {
                null
            }
        }

    suspend fun fetchBlocked(adminKey: String): List<BlockedEntry>? =
        withContext(Dispatchers.IO) {
            try {
                client.newCall(get("blocked", adminKey)).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val array = JSONObject(response.body.string()).optJSONArray("blocked")
                        ?: return@withContext null
                    (0 until array.length()).mapNotNull { i ->
                        val entry = array.optJSONObject(i) ?: return@mapNotNull null
                        BlockedEntry(
                            device = entry.optString("device"),
                            reason = entry.optString("reason"),
                            created = entry.optLong("created")
                        )
                    }
                }
            } catch (_: Exception) {
                null
            }
        }

    suspend fun approve(adminKey: String, id: String): String? =
        post(adminKey, "approve", JSONObject().put("id", id))
            ?.optString("prUrl")?.takeIf { it.isNotEmpty() }

    suspend fun reject(adminKey: String, id: String): Boolean =
        post(adminKey, "reject", JSONObject().put("id", id))
            ?.optBoolean("rejected") == true

    suspend fun block(adminKey: String, device: String, reason: String): Boolean =
        post(adminKey, "block", JSONObject().put("device", device).put("reason", reason))
            ?.optBoolean("blocked") == true

    suspend fun unblock(adminKey: String, device: String): Boolean =
        post(adminKey, "unblock", JSONObject().put("device", device))
            ?.optBoolean("unblocked") == true

    private fun get(path: String, adminKey: String): Request =
        Request.Builder()
            .url("$WORKER_URL/admin/$path")
            .header("x-admin-key", adminKey)
            .build()

    private suspend fun post(
        adminKey: String, action: String, payload: JSONObject
    ): JSONObject? =
        withContext(Dispatchers.IO) {
            try {
                val body = payload.toString()
                    .toRequestBody("application/json".toMediaType())
                client.newCall(
                    Request.Builder()
                        .url("$WORKER_URL/admin/$action")
                        .header("x-admin-key", adminKey)
                        .post(body)
                        .build()
                ).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    JSONObject(response.body.string())
                }
            } catch (_: Exception) {
                null
            }
        }

    private fun JSONObject.color(key: String): Int? =
        optString(key).takeIf { it.isNotEmpty() }?.let {
            runCatching { it.toColorInt() }.getOrNull()
        }
}