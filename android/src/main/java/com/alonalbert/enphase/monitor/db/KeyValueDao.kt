package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface KeyValueDao {
  @Query("SELECT value FROM KeyValue WHERE name = :name")
  suspend fun get(name: String): String?

  @Upsert()
  suspend fun upsert(keyValue: KeyValue)

  suspend fun upsert(name: String, value: String) {
    upsert(KeyValue(name, value))
  }
}