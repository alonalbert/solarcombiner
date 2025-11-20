package com.alonalbert.enphase.monitor.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [
    KeyValue::class,
    Day::class,
    DayExportValues::class,
    DayValues::class,
  ],
  version = 1,
  exportSchema = true,
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun batteryDao(): BatteryDao
  abstract fun dayDao(): DayDao
  abstract fun configDao(): KeyValueDao
  abstract fun settingsDao(): EnphaseConfigDao
  abstract fun loginInfoDao(): LoginInfoDao

  companion object {
    fun getDatabase(context: Context, filename: String): AppDatabase {
      return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, filename)
        .fallbackToDestructiveMigration(true)
        .build()
    }
  }
}
