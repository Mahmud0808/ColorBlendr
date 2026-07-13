package com.drdisagree.colorblendr.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_THEME_TABLE
import com.drdisagree.colorblendr.data.models.CommunityThemeEntity

@Dao
interface CommunityThemeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(themes: List<CommunityThemeEntity>)

    @Query("DELETE FROM $COMMUNITY_THEME_TABLE")
    suspend fun clear()

    // Index is the source of truth; removed themes (kill-list) drop out here.
    @Transaction
    suspend fun replaceAll(themes: List<CommunityThemeEntity>) {
        clear()
        insertAll(themes)
    }

    @Query("SELECT * FROM $COMMUNITY_THEME_TABLE")
    suspend fun getAll(): List<CommunityThemeEntity>

    @Query("SELECT * FROM $COMMUNITY_THEME_TABLE WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<CommunityThemeEntity>

    @Query("UPDATE $COMMUNITY_THEME_TABLE SET upvotes = :upvotes WHERE id = :id")
    suspend fun updateUpvotes(id: String, upvotes: Int)
}
