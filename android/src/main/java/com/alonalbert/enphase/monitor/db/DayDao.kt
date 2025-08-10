package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import com.alonalbert.enphase.monitor.enphase.model.Energy
import com.alonalbert.enphase.monitor.enphase.util.format
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

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
    val dayId = getOrInsertDay(date.format())
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
    updateTotals(
      dayId,
      production = production.sum(),
      consumption = consumption.sum(),
      charge = charge.sum(),
      discharge = discharge.sum(),
      import = import.sum(),
      export = export.sum(),
    )
    insertDayValues(values)
  }

  @Transaction
  suspend fun updateExportValues(
    date: LocalDate,
    production: List<Double>,
  ) {
    val dayId = getOrInsertDay(date.format())
    assert(production.size == 96)
    val values = (0..95).map {
      DayExportValues(
        dayId = dayId,
        index = it,
        production = production[it],
      )
    }
    updateExportTotals(dayId, production.sum())
    insertDayExportValues(values)
  }

  @Query(
    """
    UPDATE Day
    SET 
      production = :production,
      consumption = :consumption,
      charge = :charge,
      discharge = :discharge,
      import = :import,
      export = :export
      WHERE id = :id
  """
  )
  suspend fun updateTotals(
    id: Long,
    production: Double,
    consumption: Double,
    charge: Double,
    discharge: Double,
    import: Double,
    export: Double,
  )

  @Query(
    """
    UPDATE Day
    SET 
      exportProduction = :production
      WHERE id = :id
  """
  )
  suspend fun updateExportTotals(
    id: Long,
    production: Double,
  )

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
    val valuesFlow = getDayWithValuesFlow(date.format())
    val exportValuesFlow = getDayWithExportValuesFlow(date.format())
    return valuesFlow.combine(exportValuesFlow) { values, exportValues ->
      val energies = values.valuesOrEmpty().zip(exportValues.valuesOrEmpty()) { values, exportValues ->
        Energy(
          exportValues.production / 1000 * 4,
          values.production / 1000 * 4,
          values.consumption / 1000 * 4,
          values.charge / 1000 * 4,
          values.discharge / 1000 * 4,
          values.export / 1000 * 4,
          values.import / 1000 * 4,
          values.battery,
        )
      }
      DailyEnergy(date, energies)
    }
  }

  @Query("SELECT id FROM Day WHERE date = :date")
  suspend fun getDayId(date: String): Long?

  @Query(
    """
      SELECT 
        d.date as day,
        SUM(v.production) / 1000 as production,
        SUM(e.production) / 1000 as exportProduction,
        SUM(v.consumption) / 1000 as consumption,
        SUM(v.charge) / 1000 as charge,
        SUM(v.discharge) / 1000 as discharge,
        SUM(MAX(v.import - v.export - e.production, 0))  / 1000 as import,
        -SUM(MIN(v.import - v.export - e.production, 0))  / 1000 as export
      FROM Day as d
      JOIN DayValues as v ON d.id = v.day_id
      JOIN DayExportValues as e ON d.id = e.day_id AND v.`index` = e.`index`
      WHERE date BETWEEN :start  AND :end
      GROUP BY d.date
      ORDER BY d.date ASC 
    """
  )
  fun getTotalsFlow(start: String, end: String): Flow<List<DayTotals>>
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
