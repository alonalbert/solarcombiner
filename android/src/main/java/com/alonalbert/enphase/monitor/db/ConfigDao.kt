package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query

@Dao
interface ConfigDao {
  @Insert(onConflict = REPLACE)
  suspend fun insert(config: Config)

  @Query("SELECT value FROM Config WHERE name = :name")
  suspend fun get(name: String): String?
}