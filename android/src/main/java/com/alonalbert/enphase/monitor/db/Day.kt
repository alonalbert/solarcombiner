package com.alonalbert.enphase.monitor.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "Day",
  indices = [Index(value = ["date"], unique = true)]
)
data class Day(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  @ColumnInfo(name = "date")
  val date: String
)
