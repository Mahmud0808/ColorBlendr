package com.drdisagree.colorblendr.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drdisagree.colorblendr.data.common.Const.CUSTOM_STYLE_TABLE
import com.drdisagree.colorblendr.data.models.CustomStyleModel

@Dao
interface CustomStyleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomStyle(customStyle: CustomStyleModel)

    @Update
    suspend fun updateCustomStyle(customStyle: CustomStyleModel)

    @Delete
    suspend fun deleteCustomStyle(customStyle: CustomStyleModel)

    @Query("SELECT * FROM $CUSTOM_STYLE_TABLE")
    suspend fun getAllCustomStyles(): List<CustomStyleModel>

    @Query("SELECT * FROM $CUSTOM_STYLE_TABLE WHERE styleId = :styleId LIMIT 1")
    suspend fun getCustomStyleById(styleId: String): CustomStyleModel?
}