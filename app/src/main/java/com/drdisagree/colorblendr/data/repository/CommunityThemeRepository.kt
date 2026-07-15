package com.drdisagree.colorblendr.data.repository

import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_INDEX_URL
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_LAST_FETCH
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_WORKER_URL
import com.drdisagree.colorblendr.data.config.Prefs.getLong
import com.drdisagree.colorblendr.data.config.Prefs.putLong
import com.drdisagree.colorblendr.data.dao.CommunityThemeDao
import com.drdisagree.colorblendr.data.models.CommunityTheme
import com.drdisagree.colorblendr.data.models.CommunityThemeEntity
import com.drdisagree.colorblendr.utils.community.CommunityThemeCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// Cache-first store for community themes. Index fetch replaces the Room
// cache; browse/showcase read only from cache.
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
                    val responseBody = response.body
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
            val json = JSONObject(response.body.string())

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
        private const val MAX_INDEX_BYTES = 20L * 1024 * 1024
    }
}
