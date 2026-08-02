package com.drdisagree.colorblendr.utils.community

import android.annotation.SuppressLint
import android.provider.Settings
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_VOTED_IDS
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import com.drdisagree.colorblendr.data.config.Prefs
import com.drdisagree.colorblendr.data.models.CommunityVoteResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

// Anonymous one-vote-per-device votes. Device key = salted SHA-256 of SSAID
// (survives reinstall, scoped to this app's signing key, never sent raw).
// Voted ids cached in prefs, reconciled with the worker on details open.
object CommunityVotes {

    private const val SALT = "colorblendr-community-v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    @SuppressLint("HardwareIds")
    fun deviceHash(): String {
        val ssaid = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        return MessageDigest.getInstance("SHA-256")
            .digest((ssaid + SALT).toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun votedIds(): Set<String> {
        val raw = Prefs.getString(COMMUNITY_VOTED_IDS) ?: return emptySet()
        return try {
            val array = JSONArray(raw)
            buildSet { for (i in 0 until array.length()) add(array.getString(i)) }
        } catch (_: Exception) {
            emptySet()
        }
    }

    private fun saveVotedIds(ids: Set<String>) {
        Prefs.putString(COMMUNITY_VOTED_IDS, JSONArray(ids.toList()).toString())
    }

    // Server state wins (covers reinstall/wipe); null on network failure.
    suspend fun sync(): Set<String>? = withContext(Dispatchers.IO) {
        try {
            client.newCall(
                Request.Builder()
                    .url("$COMMUNITY_WORKER_URL/votes?device=${deviceHash()}")
                    .build()
            ).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = JSONObject(response.body.string())
                val array = json.optJSONArray("themeIds") ?: return@withContext null
                val ids = buildSet {
                    for (i in 0 until array.length()) add(array.getString(i))
                }
                saveVotedIds(ids)
                ids
            }
        } catch (_: Exception) {
            null
        }
    }

    // Fire-and-forget "download" ping when a community creation is actually
    // applied; server dedupes per device.
    suspend fun reportApply(themeId: String) {
        withContext(Dispatchers.IO) {
            try {
                val body = JSONObject()
                    .put("themeId", themeId)
                    .put("device", deviceHash())
                    .toString()
                    .toRequestBody("application/json".toMediaType())

                client.newCall(
                    Request.Builder().url("$COMMUNITY_WORKER_URL/download").post(body).build()
                ).execute().close()
            } catch (_: Exception) {
            }
        }
    }

    suspend fun toggle(themeId: String): CommunityVoteResult? = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject()
                .put("themeId", themeId)
                .put("device", deviceHash())
                .toString()
                .toRequestBody("application/json".toMediaType())

            client.newCall(
                Request.Builder().url("$COMMUNITY_WORKER_URL/vote").post(body).build()
            ).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val json = JSONObject(response.body.string())
                val result = CommunityVoteResult(
                    voted = json.optBoolean("voted"),
                    upvotes = json.optInt("upvotes")
                )

                val ids = votedIds().toMutableSet()
                if (result.voted) ids.add(themeId) else ids.remove(themeId)
                saveVotedIds(ids)
                result
            }
        } catch (_: Exception) {
            null
        }
    }
}
