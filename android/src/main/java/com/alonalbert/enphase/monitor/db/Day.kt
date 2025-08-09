package com.alonalbert.enphase.monitor.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  indices = [Index(value = ["date"], unique = true)]
)
data class Day(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  val date: String,

  val production: Double = 0.0,
  val consumption: Double = 0.0,
  val charge: Double = 0.0,
  val discharge: Double = 0.0,
  val import: Double = 0.0,
  val export: Double = 0.0,
  val exportProduction: Double = 0.0,
)
