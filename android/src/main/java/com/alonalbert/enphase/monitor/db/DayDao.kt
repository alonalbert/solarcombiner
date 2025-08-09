package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import com.alonalbert.enphase.monitor.enphase.model.Energy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.YEAR

private val FORMATTER = DateTimeFormatterBuilder()
  .parseCaseInsensitive()
  .appendValue(YEAR)
  .appendLiteral('-')
  .appendValue(MONTH_OF_YEAR, 2)
  .appendLiteral('-')
  .appendValue(DAY_OF_MONTH, 2)
  .toFormatter()

@Dao
interface DayDao {

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertDay(day: Day): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDayValues(values: List<DayValues>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDayExportValues(values: List<DayExportValues>)

  @Transaction
  suspend fun updateValues(
    date: LocalDate,
    production: List<Double>,
    consumption: List<Double>,
    charge: List<Double>,
    discharge: List<Double>,
    import: List<Double>,
    export: List<Double>,
    battery: List<Int?>,
  ) {
    val dayId = getOrInsertDay(date.format(FORMATTER))
    assert(production.size == 96)
    assert(consumption.size == 96)
    assert(charge.size == 96)
    assert(discharge.size == 96)
    assert(import.size == 96)
    assert(export.size == 96)
    assert(battery.size == 96)
    val values = (0..95).map {
      DayValues(
        dayId = dayId,
        index = it,
        production = production[it],
        consumption = consumption[it],
        charge = charge[it],
        discharge = discharge[it],
        import = import[it],
        export = export[it],
        battery = battery[it],
      )
    }
    insertDayValues(values)
  }

  @Transaction
  suspend fun updateExportValues(
    date: LocalDate,
    production: List<Double>,
  ) {
    val dayId = getOrInsertDay(date.format(FORMATTER))
    assert(production.size == 96)
    val values = (0..95).map {
      DayExportValues(
        dayId = dayId,
        index = it,
        production = production[it],
      )
    }
    insertDayExportValues(values)
  }

  private suspend fun getOrInsertDay(date: String): Long {
    val id = insertDay(Day(date = date))
    if (id > 0) {
      return id
    }
    return getDayId(date) ?: throw IllegalStateException("Failed to get or insert a day: $date")
  }

  @Transaction
  @Query("SELECT * FROM Day WHERE date = :date")
  suspend fun getDayWithValues(date: String): DayWithValues?

  @Transaction
  @Query("SELECT * FROM Day WHERE date = :date")
  fun getDayWithValuesFlow(date: String): Flow<DayWithValues?>

  @Transaction
  @Query("SELECT * FROM Day WHERE date = :date")
  fun getDayWithExportValuesFlow(date: String): Flow<DayWithExportValues?>


  fun getDailyEnergyFlow(date: LocalDate): Flow<DailyEnergy> {
    val valuesFlow = getDayWithValuesFlow(date.format(FORMATTER))
    val exportValuesFlow = getDayWithExportValuesFlow(date.format(FORMATTER))
    return valuesFlow.combine(exportValuesFlow) { values, exportValues ->
      val energies = values.valuesOrEmpty().zip(exportValues.valuesOrEmpty()) { values, exportValues ->
        Energy(
          exportValues.production.kwh(),
          values.production.kwh(),
          values.consumption.kwh(),
          values.charge.kwh(),
          values.discharge.kwh(),
          values.export.kwh(),
          values.import.kwh(),
          values.battery,
        )
      }
      DailyEnergy(date, energies)
    }
  }

  @Query("SELECT id FROM Day WHERE date = :date")
  suspend fun getDayId(date: String): Long?
}

private fun DayWithValues?.valuesOrEmpty(): List<DayValues> {
  return when {
    this == null || values.isEmpty() -> List(96) { DayValues(0, 0, it, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0) }
    else -> values
  }
}

private fun DayWithExportValues?.valuesOrEmpty(): List<DayExportValues> {
  return when {
    this == null || values.isEmpty() -> List(96) { DayExportValues(0, 0, it, 0.0) }
    else -> values
  }
}

private fun Double.kwh() = this / 1000 * 4
