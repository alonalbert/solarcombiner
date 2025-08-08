package com.alonalbert.enphase.monitor.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ReserveConfig(
  @PrimaryKey
  val id: Int = 1,
  val idleLoad: Double = 0.8,
  val minReserve: Int = 20,
  val chargeStart: Int = 9,
)