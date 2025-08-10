package com.alonalbert.enphase.monitor.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [
    Settings::class,
    ReserveConfig::class,
    Day::class,
    DayValues::class,
    DayExportValues::class,
    BatteryStatus::class,
  ],
  version = 1,
  exportSchema = true,
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun settingsDao(): SettingsDao
  abstract fun reserveConfigDao(): ReserveConfigDao
  abstract fun dayDao(): DayDao
  abstract fun batteryStatusDao(): BatteryStatusDao

  companion object {
    fun getDatabase(context: Context, filename: String): AppDatabase {
      return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, filename)
        .fallbackToDestructiveMigration(true)
        .build()
    }
  }
}
