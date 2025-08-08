package com.alonalbert.enphase.monitor.db

import androidx.room.Embedded
import androidx.room.Relation

data class DayWithExportValues(
  @Embedded
  val day: Day,

  @Relation(
    parentColumn = "id",
    entityColumn = "day_id"
  )
  val values: List<DayExportValues>,
)