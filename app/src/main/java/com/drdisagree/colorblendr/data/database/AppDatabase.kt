package com.drdisagree.colorblendr.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.drdisagree.colorblendr.ColorBlendr.Companion.appContext
import com.drdisagree.colorblendr.data.common.Constant.COMMUNITY_THEME_TABLE
import com.drdisagree.colorblendr.data.common.Constant.CUSTOM_STYLE_TABLE
import com.drdisagree.colorblendr.data.common.Constant.DATABASE_NAME
import com.drdisagree.colorblendr.data.dao.CommunityThemeDao
import com.drdisagree.colorblendr.data.dao.CustomStyleDao
import com.drdisagree.colorblendr.data.models.CommunityThemeEntity
import com.drdisagree.colorblendr.data.models.CustomStyleModel

@Database(entities = [CustomStyleModel::class, CommunityThemeEntity::class], version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customStyleDao(): CustomStyleDao
    abstract fun communityThemeDao(): CommunityThemeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3
                ).build().also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$COMMUNITY_THEME_TABLE` (" +
                            "`id` TEXT NOT NULL, " +
                            "`payloadJson` TEXT NOT NULL, " +
                            "`upvotes` INTEGER NOT NULL, " +
                            "`downloads` INTEGER NOT NULL, " +
                            "`createdAt` INTEGER NOT NULL, " +
                            "`fetchedAt` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `$CUSTOM_STYLE_TABLE` " +
                            "ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL("UPDATE `$CUSTOM_STYLE_TABLE` SET sortOrder = rowid")
            }
        }

        fun reloadInstance() {
            synchronized(this) {
                INSTANCE = Room.databaseBuilder(
                    appContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3
                ).build()
            }
        }
    }
}