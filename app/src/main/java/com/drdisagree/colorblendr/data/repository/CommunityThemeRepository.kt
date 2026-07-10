package com.drdisagree.colorblendr.data.repository

import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_INDEX_URL
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_LAST_FETCH
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_SHOWCASE_NEXT
import com.drdisagree.colorblendr.data.config.Prefs.clearPref
import com.drdisagree.colorblendr.data.config.Prefs.getLong
import com.drdisagree.colorblendr.data.config.Prefs.getString
import com.drdisagree.colorblendr.data.config.Prefs.putLong
import com.drdisagree.colorblendr.data.config.Prefs.putString
import com.drdisagree.colorblendr.data.dao.CommunityThemeDao
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.data.models.CommunityThemeEntity
import com.drdisagree.colorblendr.utils.community.CommunityThemeCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// Cache-first store for community themes. Index fetch replaces the Room
// cache; browse/showcase read only from cache. Showcase is double-buffered:
// the "next" bucket persisted last session renders instantly on launch, then
// a background refresh restocks it for the next launch.
class CommunityThemeRepository(private val dao: CommunityThemeDao) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun refreshIndex(): Boolean = withContext(Dispatchers.IO) {
        val body = try {
            client.newCall(Request.Builder().url(COMMUNITY_INDEX_URL).build())
                .execute().use { response ->
                    if (!response.isSuccessful) return@withContext false
                    val responseBody = response.body ?: return@withContext false
                    if (responseBody.contentLength() > MAX_INDEX_BYTES) return@withContext false
                    responseBody.string()
                }
        } catch (_: Exception) {
            return@withContext false
        }
        if (body.length > MAX_INDEX_BYTES) return@withContext false

        val themes = CommunityThemeCodec.parseIndex(body)
        if (themes.isEmpty()) return@withContext false

        // Index counts are baked daily; overlay live counts so votes and
        // applies since the last bake (including our own) survive a refresh.
        val liveCounts = fetchLiveCounts()
        val merged = if (liveCounts == null) {
            themes
        } else {
            themes.map {
                it.copy(
                    upvotes = liveCounts.first[it.id] ?: 0,
                    downloads = liveCounts.second[it.id] ?: 0
                )
            }
        }

        val fetchedAt = System.currentTimeMillis()
        dao.replaceAll(merged.map { it.toEntity(fetchedAt) })
        putLong(COMMUNITY_LAST_FETCH, fetchedAt)
        true
    }

    // Pair(upvotes by id, downloads by id).
    private fun fetchLiveCounts(): Pair<Map<String, Int>, Map<String, Int>>? = try {
        client.newCall(
            Request.Builder().url("$COMMUNITY_WORKER_URL/counts").build()
        ).execute().use { response ->
            if (!response.isSuccessful) return null
            val json = JSONObject(response.body?.string() ?: return null)

            fun section(name: String): Map<String, Int> {
                val obj = json.optJSONObject(name) ?: return emptyMap()
                return buildMap {
                    obj.keys().forEach { key -> put(key, obj.optInt(key)) }
                }
            }

            section("upvotes") to section("downloads")
        }
    } catch (_: Exception) {
        null
    }

    fun isStale(maxAgeMillis: Long): Boolean =
        System.currentTimeMillis() - getLong(COMMUNITY_LAST_FETCH, 0L) > maxAgeMillis

    suspend fun getThemes(): List<CommunityTheme> =
        dao.getAll().mapNotNull { it.toTheme() }

    // Reflect a fresh vote count immediately; the daily index rebuild remains
    // the source of truth.
    suspend fun updateUpvotes(id: String, upvotes: Int) {
        dao.updateUpvotes(id, upvotes)
        dao.getByIds(listOf(id)).firstOrNull()?.let { entity ->
            entity.toTheme()?.let { theme ->
                dao.insertAll(
                    listOf(entity.copy(
                        payloadJson = CommunityThemeCodec
                            .themeToJson(theme.copy(upvotes = upvotes)).toString()
                    ))
                )
            }
        }
    }

    suspend fun getThemeById(id: String): CommunityTheme? =
        dao.getByIds(listOf(id)).firstOrNull()?.toTheme()

    // Instant path: themes persisted for this launch. Empty only on first run
    // (or wiped cache); caller then falls back to fetchShowcaseNow().
    suspend fun getBufferedShowcase(): List<CommunityTheme> {
        val ids = readBucket()
        if (ids.isEmpty()) return emptyList()

        val byId = dao.getByIds(ids).mapNotNull { it.toTheme() }.associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    // First-run path: network fetch, then sample immediately.
    suspend fun fetchShowcaseNow(count: Int): List<CommunityTheme> {
        if (!refreshIndex()) return emptyList()
        return getThemes().shuffled().take(count)
    }

    // Restock the bucket for the next launch: refresh index (best effort),
    // sample avoiding what is on screen right now. On total failure keeps the
    // old bucket, so the carousel is never blank next launch.
    suspend fun prepareNextShowcase(count: Int, excludeIds: List<String>) {
        refreshIndex()

        val all = getThemes()
        if (all.isEmpty()) return

        val pool = all.filterNot { it.id in excludeIds }.ifEmpty { all }
        writeBucket(pool.shuffled().take(count).map { it.id })
    }

    private fun readBucket(): List<String> {
        val raw = getString(COMMUNITY_SHOWCASE_NEXT) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            List(array.length()) { array.getString(it) }
        } catch (_: Exception) {
            clearPref(COMMUNITY_SHOWCASE_NEXT)
            emptyList()
        }
    }

    private fun writeBucket(ids: List<String>) {
        putString(COMMUNITY_SHOWCASE_NEXT, JSONArray(ids).toString())
    }

    private fun CommunityTheme.toEntity(fetchedAt: Long) = CommunityThemeEntity(
        id = id,
        payloadJson = CommunityThemeCodec.themeToJson(this).toString(),
        upvotes = upvotes,
        downloads = downloads,
        createdAt = createdAt,
        fetchedAt = fetchedAt
    )

    private fun CommunityThemeEntity.toTheme(): CommunityTheme? = try {
        CommunityThemeCodec.parseTheme(JSONObject(payloadJson))
    } catch (_: Exception) {
        null
    }

    companion object {
        private const val MAX_INDEX_BYTES = 2L * 1024 * 1024
    }
}
