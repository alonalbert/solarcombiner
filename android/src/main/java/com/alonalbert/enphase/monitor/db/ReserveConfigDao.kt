package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReserveConfigDao {
  @Insert(onConflict = REPLACE)
  suspend fun set(reserveConfig: ReserveConfig)

  @Query("SELECT * FROM reserveconfig WHERE ROWID = 1")
  fun getReserveConfig(): ReserveConfig?

  @Query("SELECT * FROM reserveconfig WHERE ROWID = 1")
  fun getReserveConfigFlow(): Flow<ReserveConfig?>
}