package com.drdisagree.colorblendr.data.database

import androidx.room.TypeConverter
import com.drdisagree.colorblendr.data.common.Constant.GSON
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromPalette(palette: ArrayList<ArrayList<Int>>?): String? {
        val type = object : TypeToken<ArrayList<ArrayList<Int>>>() {}.type
        return GSON.toJson(palette, type)
    }

    @TypeConverter
    fun toPalette(paletteString: String?): ArrayList<ArrayList<Int>>? {
        val type = object : TypeToken<ArrayList<ArrayList<Int>>>() {}.type
        return GSON.fromJson(paletteString, type)
    }
}