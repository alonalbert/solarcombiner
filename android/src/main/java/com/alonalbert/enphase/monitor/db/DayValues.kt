package com.alonalbert.enphase.monitor.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "DayValues",
  foreignKeys = [
    ForeignKey(
      entity = Day::class,
      parentColumns = ["id"],
      childColumns = ["day_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["day_id", "index"], unique = true)]
)
data class DayValues(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  @ColumnInfo(name = "day_id")
  val dayId: Long,

  val index: Int,

  val produced: Double,
  val consumed: Double,
  val charged: Double,
  val discharged: Double,
  val imported: Double,
  val exported: Double,
  val battery: Int?,
)