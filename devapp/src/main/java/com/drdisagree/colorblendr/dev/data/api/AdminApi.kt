package com.drdisagree.colorblendr.dev.data.api

import androidx.core.graphics.toColorInt
import com.drdisagree.colorblendr.dev.data.models.BlockedEntry
import com.drdisagree.colorblendr.dev.data.models.ComparableTheme
import com.drdisagree.colorblendr.dev.data.models.PendingSubmission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AdminApi {

    private const val WORKER_URL = "https://colorblendr-themes.drdisagree.workers.dev"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun fetchPending(adminKey: String): ApiResult<List<PendingSubmission>> =
        execute(get("pending", adminKey)) { body ->
            val array = JSONObject(body).optJSONArray("pending") ?: return@execute null
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

    suspend fun fetchBlocked(adminKey: String): ApiResult<List<BlockedEntry>> =
        execute(get("blocked", adminKey)) { body ->
            val array = JSONObject(body).optJSONArray("blocked") ?: return@execute null
            (0 until array.length()).mapNotNull { i ->
                val entry = array.optJSONObject(i) ?: return@mapNotNull null
                BlockedEntry(
                    device = entry.optString("device"),
                    reason = entry.optString("reason"),
                    created = entry.optLong("created")
                )
            }
        }

    suspend fun fetchPublishedThemes(): ApiResult<List<ComparableTheme>> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://cdn.jsdelivr.net/gh/Mahmud0808/ColorBlendr-Themes@main/index.json")
                    .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext ApiResult.Failure(
                        response.code,
                        "Failed to fetch published index"
                    )
                    val body = response.body.string()
                    val array = JSONArray(body)
                    val list = (0 until array.length()).mapNotNull { i ->
                        val entry = array.optJSONObject(i) ?: return@mapNotNull null
                        val id = entry.optString("id").takeIf { it.isNotEmpty() }
                            ?: return@mapNotNull null
                        ComparableTheme(
                            id = id,
                            name = entry.optString("name"),
                            author = entry.optString("author"),
                            description = entry.optString("description"),
                            seedColor = entry.color("seedColor"),
                            secondaryColor = entry.color("secondaryColor"),
                            tertiaryColor = entry.color("tertiaryColor"),
                            style = entry.optString("style"),
                            colorSpecVersion = entry.optInt("colorSpecVersion", 0),
                            accentSaturation = entry.optInt("accentSaturation", 100),
                            backgroundSaturation = entry.optInt("backgroundSaturation", 100),
                            backgroundLightness = entry.optInt("backgroundLightness", 100),
                            modeSpecificThemes = entry.optBoolean("modeSpecificThemes", false),
                            accentSaturationLight = entry.optInt("accentSaturationLight", 100),
                            backgroundSaturationLight = entry.optInt("backgroundSaturationLight", 100),
                            backgroundLightnessLight = entry.optInt("backgroundLightnessLight", 100),
                            accurateShades = entry.optBoolean("accurateShades", true),
                            pitchBlack = entry.optBoolean("pitchBlack", false),
                            tintText = entry.optBoolean("tintText", true),
                            colorOverrides = emptyMap(),
                            payloadJson = entry.toString(),
                            isPublished = true
                        )
                    }
                    ApiResult.Success(list)
                }
            } catch (e: Exception) {
                ApiResult.Failure(null, e.message)
            }
        }

    suspend fun approve(
        adminKey: String,
        id: String,
        name: String? = null,
        description: String? = null
    ): ApiResult<String> {
        val payload = JSONObject().put("id", id)
        name?.let { payload.put("name", it) }
        description?.let { payload.put("description", it) }
        return execute(post("approve", adminKey, payload)) { body ->
            JSONObject(body).optString("prUrl").takeIf { it.isNotEmpty() }
        }
    }

    suspend fun reject(adminKey: String, id: String): ApiResult<Unit> =
        execute(post("reject", adminKey, JSONObject().put("id", id))) { body ->
            if (JSONObject(body).optBoolean("rejected")) Unit else null
        }

    suspend fun block(adminKey: String, device: String, reason: String): ApiResult<Unit> =
        execute(
            post("block", adminKey, JSONObject().put("device", device).put("reason", reason))
        ) { body ->
            if (JSONObject(body).optBoolean("blocked")) Unit else null
        }

    suspend fun unblock(adminKey: String, device: String): ApiResult<Unit> =
        execute(post("unblock", adminKey, JSONObject().put("device", device))) { body ->
            if (JSONObject(body).optBoolean("unblocked")) Unit else null
        }

    private suspend fun <T> execute(
        request: Request,
        parse: (String) -> T?
    ): ApiResult<T> =
        withContext(Dispatchers.IO) {
            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body.string()
                    if (!response.isSuccessful) {
                        val error = runCatching {
                            JSONObject(body).optString("error")
                        }.getOrNull()?.takeIf { it.isNotEmpty() }
                        return@withContext ApiResult.Failure(response.code, error)
                    }
                    parse(body)?.let { ApiResult.Success(it) }
                        ?: ApiResult.Failure(response.code, "unexpected response")
                }
            } catch (e: Exception) {
                ApiResult.Failure(null, e.message)
            }
        }

    private fun get(path: String, adminKey: String): Request =
        Request.Builder()
            .url("$WORKER_URL/admin/$path")
            .header("x-admin-key", sanitize(adminKey))
            .build()

    private fun post(action: String, adminKey: String, payload: JSONObject): Request =
        Request.Builder()
            .url("$WORKER_URL/admin/$action")
            .header("x-admin-key", sanitize(adminKey))
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

    private fun sanitize(value: String): String =
        value.filter { it.code in 0x20..0x7e }.trim()

    private fun JSONObject.color(key: String): Int? =
        optString(key).takeIf { it.isNotEmpty() }?.let {
            runCatching { it.toColorInt() }.getOrNull()
        }
}