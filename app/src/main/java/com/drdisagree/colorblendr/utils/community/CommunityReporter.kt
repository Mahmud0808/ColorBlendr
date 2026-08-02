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

// Reports a community creation; server dedupes per device and opens a
// moderation issue on the first report. Returns false on network failure.
object CommunityReporter {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun report(themeId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject()
                .put("themeId", themeId)
                .put("device", CommunityVotes.deviceHash())
                .toString()
                .toRequestBody("application/json".toMediaType())

            client.newCall(
                Request.Builder().url("$COMMUNITY_WORKER_URL/report").post(body).build()
            ).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }
}
