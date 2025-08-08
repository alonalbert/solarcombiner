package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryStatusDao {
  @Insert(onConflict = REPLACE)
  suspend fun set(batteryStatus: BatteryStatus)

  @Query("SELECT * FROM BatteryStatus WHERE ROWID = 1")
  fun getBatteryStatusFlow(): Flow<BatteryStatus?>
}
