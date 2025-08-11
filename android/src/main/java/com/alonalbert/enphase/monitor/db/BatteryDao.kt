package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@Dao
interface BatteryDao: KeyValueDao {
  @Query(
    """
    SELECT 
      (SELECT value FROM KeyValue WHERE name='battery') as battery, 
      (SELECT value FROM KeyValue WHERE name='reserve') as reserve
      WHERE TRUE 
        AND battery IS NOT NULL
        AND reserve IS NOT NULL
    """
  )
  fun getBatteryStatusFlow(): Flow<BatteryStatus?>

  @Transaction
  suspend fun updateBatteryStatus(batteryStatus: BatteryStatus) {
    upsert("battery", batteryStatus.battery.toString())
    upsert("reserve", batteryStatus.reserve.toString())
  }

  @Query(
    """
    SELECT 
      (SELECT value FROM KeyValue WHERE name='idleLoad') as idleLoad, 
      (SELECT value FROM KeyValue WHERE name='minReserve') as minReserve, 
      (SELECT value FROM KeyValue WHERE name='chargeStart') as chargeStart
      WHERE TRUE 
        AND idleLoad IS NOT NULL
        AND minReserve IS NOT NULL
        AND chargeStart IS NOT NULL
    """
  )
  fun getReserveConfigFlow(): Flow<ReserveConfig?>

  suspend fun getReserveConfig(): ReserveConfig? = getReserveConfigFlow().firstOrNull()

  @Transaction
  suspend fun updateReserveConfig(reserveConfig: ReserveConfig) {
    upsert("idleLoad", reserveConfig.idleLoad.toString())
    upsert("minReserve", reserveConfig.minReserve.toString())
    upsert("chargeStart", reserveConfig.chargeStart.toString())
  }

  @Query("SELECT CAST(value AS REAL) FROM KeyValue WHERE name='batteryCapacity'")
  fun getBatteryCapacityFlow() : Flow<Double?>

  suspend fun getBatteryCapacity() = getBatteryCapacityFlow().firstOrNull()

  suspend fun updateBatteryCapacity(value: Double) {
    upsert("batteryCapacity", value.toString())
  }
}
