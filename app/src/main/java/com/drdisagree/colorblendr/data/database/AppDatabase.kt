package com.drdisagree.colorblendr.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant.DATABASE_NAME
import com.drdisagree.colorblendr.data.dao.CustomStyleDao
import com.drdisagree.colorblendr.data.models.CustomStyleModel

val appDatabase: AppDatabase
    get() = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        DATABASE_NAME
    ).build()

@Database(entities = [CustomStyleModel::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customStyleDao(): CustomStyleDao
}