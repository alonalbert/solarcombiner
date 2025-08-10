package com.alonalbert.enphase.monitor.db

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

object LocalDateConverter {
  @TypeConverter
  @JvmStatic
  fun fromLocalDate(date: LocalDate?): String? {
    return date?.format(ISO_LOCAL_DATE)
  }

  @TypeConverter
  @JvmStatic
  fun toLocalDate(value: String?): LocalDate? {
    return value?.let { LocalDate.parse(it, ISO_LOCAL_DATE) }
  }
}
