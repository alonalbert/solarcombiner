package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.alonalbert.enphase.monitor.enphase.model.DailyEnergy
import com.alonalbert.enphase.monitor.enphase.model.Energy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
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

  @Insert
  suspend fun insertDayValue(dayValues: DayValues)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDayValues(dayValues: List<DayValues>)

  @Transaction
  suspend fun updateDay(
    date: LocalDate,
    produced: List<Double>,
    consumed: List<Double>,
    charged: List<Double>,
    discharged: List<Double>,
    imported: List<Double>,
    exported: List<Double>,
    battery: List<Int?>,
  ) {
    val dayId = getOrInsertDay(date.format(FORMATTER))
    assert(produced.size == 96)
    assert(consumed.size == 96)
    assert(charged.size == 96)
    assert(discharged.size == 96)
    assert(imported.size == 96)
    assert(exported.size == 96)
    assert(battery.size == 96)
    val values = (0..95).map {
      DayValues(
        dayId = dayId,
        index = it,
        produced = produced[it],
        consumed = consumed[it],
        charged = charged[it],
        discharged = discharged[it],
        imported = imported[it],
        exported = exported[it],
        battery = battery[it],
      )
    }
    insertDayValues(values)
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

  suspend fun getDailyEnergy(date: String): DailyEnergy? {
    val values = getDayWithValues(date) ?: return null
    val energies = values.values.map {
      Energy(
        exportProduced = 0.0,
        it.produced,
        it.consumed,
        it.charged,
        it.discharged,
        it.exported,
        it.imported,
        it.battery,
      )
    }
    return DailyEnergy(LocalDate.parse(date), energies)
  }

  fun getDailyEnergyFlow(date: LocalDate): Flow<DailyEnergy> {
    return getDayWithValuesFlow(date.format(FORMATTER)).filterNotNull().map { values ->
      val energies = values.values.map {
        Energy(
          exportProduced = 0.0,
          it.produced,
          it.consumed,
          it.charged,
          it.discharged,
          it.exported,
          it.imported,
          it.battery,
        )
      }
      DailyEnergy(date, energies)
    }
  }

  @Query("SELECT id FROM Day WHERE date = :date")
  suspend fun getDayId(date: String): Long?
}
