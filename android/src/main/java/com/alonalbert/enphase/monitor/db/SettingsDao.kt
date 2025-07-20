package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
  @Insert(onConflict = REPLACE)
  suspend fun set(settings: Settings)

  @Query("SELECT * FROM settings WHERE id = 1")
  suspend fun getSettings(): Settings

  @Query("SELECT * FROM settings WHERE id = 1")
  fun getSettingsFlow(): Flow<Settings?>
}