package com.drdisagree.colorblendr.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.drdisagree.colorblendr.data.common.Const.CUSTOM_STYLE_TABLE
import com.drdisagree.colorblendr.utils.MONET
import java.util.UUID

@Entity(tableName = CUSTOM_STYLE_TABLE)
data class CustomStyleModel(
    @PrimaryKey val styleId: String = UUID.randomUUID().toString(),
    var styleName: String,
    var description: String,
    val prefsGson: String,
    val monet: MONET,
    val palette: ArrayList<ArrayList<Int>>
)
