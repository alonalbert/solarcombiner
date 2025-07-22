package com.alonalbert.enphase.monitor.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reserve_config")
data class ReserveConfig(
  @PrimaryKey
  val id: Int = 1,
  val idleLoad: Double = 1.0,
  val minReserve: Int = 20,
  val chargeTime: Int = 9,
)