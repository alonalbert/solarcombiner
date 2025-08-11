package com.alonalbert.enphase.monitor.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class KeyValue(
  @PrimaryKey
  val name: String,
  val value: String,
)
