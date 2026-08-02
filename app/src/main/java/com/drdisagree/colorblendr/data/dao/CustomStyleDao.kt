package com.drdisagree.colorblendr.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.drdisagree.colorblendr.data.common.Constant.CUSTOM_STYLE_TABLE
import com.drdisagree.colorblendr.data.models.CustomStyleModel

@Dao
interface CustomStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomStyle(customStyle: CustomStyleModel)

    @Update
    suspend fun updateCustomStyle(customStyle: CustomStyleModel)

    @Delete
    suspend fun deleteCustomStyle(customStyle: CustomStyleModel)

    @Query("SELECT * FROM $CUSTOM_STYLE_TABLE ORDER BY sortOrder ASC")
    suspend fun getAllCustomStyles(): List<CustomStyleModel>

    @Query("SELECT * FROM $CUSTOM_STYLE_TABLE WHERE styleId = :styleId LIMIT 1")
    suspend fun getCustomStyleById(styleId: String): CustomStyleModel?

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM $CUSTOM_STYLE_TABLE")
    suspend fun getNextSortOrder(): Int

    @Query("UPDATE $CUSTOM_STYLE_TABLE SET sortOrder = :sortOrder WHERE styleId = :styleId")
    suspend fun updateSortOrder(styleId: String, sortOrder: Int)

    @Transaction
    suspend fun updateSortOrders(styleIds: List<String>) {
        styleIds.forEachIndexed { index, styleId ->
            updateSortOrder(styleId, index)
        }
    }
}