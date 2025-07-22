package com.alonalbert.enphase.monitor.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [
    Settings::class,
    ReserveConfig::class,
  ],
  version = 2,
  exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun settingsDao(): SettingsDao
  abstract fun reserveConfigDao(): ReserveConfigDao

  companion object {
    fun getDatabase(context: Context, filename: String): AppDatabase {
      return Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        filename
      )
        .addMigrations(MIGRATION_1_2)
        .build()
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          CREATE TABLE `reserve_config` (
            `id` INTEGER NOT NULL,
            `idleLoad` REAL NOT NULL,
            `minReserve` INTEGER NOT NULL, 
            `chargeTime` INTEGER NOT NULL,
            PRIMARY KEY(`id`)
          );
        """.trimIndent()
        )
      }
    }
  }
}
