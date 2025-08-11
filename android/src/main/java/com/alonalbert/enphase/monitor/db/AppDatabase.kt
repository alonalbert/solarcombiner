package com.alonalbert.enphase.monitor.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [
    BatteryStatus::class,
    Config::class,
    Day::class,
    DayExportValues::class,
    DayValues::class,
    ReserveConfig::class,
    Settings::class,
  ],
  version = 2,
  exportSchema = true,
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun batteryStatusDao(): BatteryStatusDao
  abstract fun dayDao(): DayDao
  abstract fun configDao(): ConfigDao
  abstract fun reserveConfigDao(): ReserveConfigDao
  abstract fun settingsDao(): SettingsDao

  companion object {
    fun getDatabase(context: Context, filename: String): AppDatabase {
      return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, filename)
        .fallbackToDestructiveMigration(true)
        .build()
    }
  }
}
