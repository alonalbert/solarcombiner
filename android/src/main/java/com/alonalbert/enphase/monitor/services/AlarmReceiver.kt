package com.alonalbert.enphase.monitor.services // Or your appropriate package

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.ReserveCalculator
import com.alonalbert.enphase.monitor.util.checkNetwork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.PrintWriter
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField.DAY_OF_MONTH
import java.time.temporal.ChronoField.HOUR_OF_DAY
import java.time.temporal.ChronoField.MINUTE_OF_HOUR
import java.time.temporal.ChronoField.MONTH_OF_YEAR
import java.time.temporal.ChronoField.SECOND_OF_MINUTE
import java.time.temporal.ChronoField.YEAR
import javax.inject.Inject
import kotlin.io.path.writer
import kotlin.text.Charsets.UTF_8
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private val DELAY = 5.minutes
private val TIMESTAMP_FORMATTER = DateTimeFormatterBuilder()
  .parseCaseInsensitive()
  .appendValue(YEAR)
  .appendLiteral('-')
  .appendValue(MONTH_OF_YEAR, 2)
  .appendLiteral('-')
  .appendValue(DAY_OF_MONTH, 2)
  .appendLiteral(' ')
  .appendValue(HOUR_OF_DAY, 2)
  .appendLiteral(':')
  .appendValue(MINUTE_OF_HOUR, 2)
  .appendLiteral(':')
  .appendValue(SECOND_OF_MINUTE, 2)
  .toFormatter()

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
  @Inject
  lateinit var db: AppDatabase

  @Inject
  lateinit var enphaseAsync: Deferred<Enphase>

  private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onReceive(context: Context, intent: Intent) {
    coroutineScope.launch {
      val delay = setReserve(context)
      scheduleAlarm(context, delay)
    }
  }

  private suspend fun setReserve(context: Context): Duration {
    with(context) {
      try {
        log("AlarmReceiver.onReceive()")
        if (!checkNetwork()) {
          log("Network connected but not validated. Might be an issue in Doze.")
          return 1.minutes
        }
        val settings = db.settingsDao().getSettings()
        if (settings == null) {
          log("Settings not found")
          return DELAY
        }
        val config = db.reserveConfigDao().getReserveConfig() ?: ReserveConfig()
        val reserve = ReserveCalculator.calculateReserve(
          LocalTime.now(),
          config.idleLoad,
          20.16,
          config.minReserve,
          config.chargeStart,
        )
        val enphase = enphaseAsync.await()
        enphase.ensureLogin(settings.email, settings.password)
        val result = enphase.setBatteryReserve(settings.mainSiteId, reserve)
        log("Setting reserve to $reserve: $result")
      } catch (e: Exception) {
        log("Failed to set reserve", e)
      }
      return DELAY
    }
  }

  context(context: Context)
  private fun log(message: String, e: Exception? = null) {
    Timber.d(e, message)
    val logFile = context.cacheDir.toPath().resolve("reserve.log")
    logFile.writer(UTF_8, APPEND, CREATE).use {
      it.write("${LocalDateTime.now().format(TIMESTAMP_FORMATTER  )}: $message\n")
      e?.printStackTrace(PrintWriter(it))
    }
  }

  companion object {
    const val ALARM_REQUEST_CODE = 12345

    fun scheduleAlarm(context: Context, delay: Duration = 0.minutes) {
      val context = context.applicationContext
      val alarmManager = context.getSystemService<AlarmManager>()!!
      if (!alarmManager.canScheduleExactAlarms()) {
        Timber.w("CAnnot schedule exact alarms")
        return
      }
      val intent = Intent(context, AlarmReceiver::class.java)

      val pendingIntentFlags = FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        intent,
        pendingIntentFlags
      )

      val triggerAt = System.currentTimeMillis() + delay.inWholeMilliseconds

      try {
        Timber.d("Alarm scheduled to trigger in approx. $delay.")
        alarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, triggerAt, pendingIntent)
      } catch (e: SecurityException) {
        Timber.e(e, "SecurityException while scheduling alarm. Do you need SCHEDULE_EXACT_ALARM?")
      }
    }
  }
}
