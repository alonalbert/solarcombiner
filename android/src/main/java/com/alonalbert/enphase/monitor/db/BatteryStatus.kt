package com.alonalbert.enphase.monitor.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BatteryStatus(
  @PrimaryKey
  val id: Int = 1,
  val battery: Int,
  val reserve: Int,
)
