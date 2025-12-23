package com.alonalbert.enphase.monitor.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alonalbert.enphase.monitor.emporia.model.ChannelUsage
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DayDao {

  @Insert(onConflict = IGNORE)
  suspend fun insertDay(day: Day): Long

  @Insert(onConflict = REPLACE)
  suspend fun insertDayValues(values: List<DayValues>)

  @Insert(onConflict = REPLACE)
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
    val dayId = getOrInsertDay(date)
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
    val dayId = getOrInsertDay(date)
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

  private suspend fun getOrInsertDay(date: LocalDate): Long {
    val id = insertDay(Day(date = date))
    if (id > 0) {
      return id
    }
    return getDayId(date) ?: throw IllegalStateException("Failed to get or insert a day: $date")
  }

  @Transaction
  @Query("SELECT * FROM Day WHERE date = :date")
  fun getDayWithValuesFlow(date: LocalDate): Flow<DayWithValues?>

  @Transaction
  @Query("SELECT * FROM Day WHERE date = :date")
  fun getDayWithExportValuesFlow(date: LocalDate): Flow<DayWithExportValues?>

  @Query("SELECT id FROM Day WHERE date = :date")
  suspend fun getDayId(date: LocalDate): Long?

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
  fun getTotalsFlow(start: LocalDate, end: LocalDate): Flow<List<DayTotals>>

  @Query(
    """
      SELECT 
        d.date
      FROM Day d
      JOIN DayValues v ON d.id = v.day_id AND v.`index`= 95
      JOIN DayExportValues e ON d.id = e.day_id AND e.`index` = 95
      WHERE date BETWEEN :start  AND :end
      GROUP BY date 
    """
  )
  suspend fun getAvailableDays(start: LocalDate, end: LocalDate): List<LocalDate>

  @Insert(onConflict = IGNORE)
  suspend fun insertChannel(channel: Channel): Long

  @Update
  suspend fun updateChannel(channel: Channel)

  @Query("SELECT * FROM Channel WHERE channelId = :channelId")
  suspend fun getChannel(channelId: String): Channel?

  @Query("SELECT * FROM Channel")
  suspend fun getChannels(): List<Channel>

  @Insert(onConflict = REPLACE)
  suspend fun insertChannelUsageValues(values: List<ChannelUsageValue>)

  @Transaction
  suspend fun updateChannelUsages(date: LocalDate, channelUsages: List<ChannelUsage>) {
    val dayId = getOrInsertDay(date)
    channelUsages.forEach {
      val channel = getChannel(it.channelId)
      val id = when {
        channel == null -> insertChannel(Channel(channelId = it.channelId, name = it.channelName))
        channel.name == it.channelName -> channel.id
        else -> updateChannel(channel.copy(name = it.channelName)).let { channel.id }
      }
      val values = it.usage.mapIndexed { index, value ->
        ChannelUsageValue(
          dayId = dayId,
          channelId = id,
          index = index,
          value = value
        )
      }
      insertChannelUsageValues(values)
    }
  }

  @Transaction
  @Query(
    """
      SELECT
        v.channel_id AS channelId,
        v.value AS value
      FROM ChannelUsageValue as v
      JOIN Day as d on v.day_id == d.id
      WHERE date = :date
      ORDER BY v.channel_id, v.`index`
    """
  )
  fun getChannelsUsagesFlow(date: LocalDate): Flow<List<UsageValue>>
}
