package com.alonalbert.enphase.monitor.ui.reserve

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.alonalbert.enphase.monitor.db.AppDatabase
import com.alonalbert.enphase.monitor.util.NetworkChecker
import com.alonalbert.solar.combiner.enphase.Enphase
import com.alonalbert.solar.combiner.enphase.ReserveCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Deferred
import timber.log.Timber
import java.io.IOException
import java.io.PrintWriter
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import kotlin.io.path.writer
import kotlin.text.Charsets.UTF_8

@HiltWorker
class ReserveManagerWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val db: AppDatabase,
  private val enphaseAsync: Deferred<Enphase>,
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    Timber.i("ReserveManagerWorker: doWork()")
    logWork("ReserveManagerWorker: doWork()")
    return try {
      if (!NetworkChecker.checkNetwork(applicationContext)) {
        Timber.w("Network connected but not validated. Might be an issue in Doze. Retrying.")
        logWork("Network connected but not validated. Might be an issue in Doze. Retrying.")
        return Result.retry()
      }

      val config = db.reserveConfigDao().getReserveConfig()
      if (config == null) {
        Timber.w("Reserve configuration not found")
        logWork("Reserve configuration not found")
        return Result.failure()
      }
      val settings = db.settingsDao().getSettings()
      if (settings == null) {
        Timber.w("Settings not found")
        logWork("Settings not found")
        return Result.failure()
      }

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
      Timber.d("Setting reserve to $reserve: $result")
      logWork("Setting reserve to $reserve: $result")
      Result.success()
    } catch (e: IOException) {
      Timber.e(e, "Failed to set reserve")
      logWork("Failed to set reserve", e)
      Result.retry()
    } catch (e: Exception) {
      Timber.e(e, "Failed to set reserve")
      logWork("Failed to set reserve", e)
      Result.failure()
    }
  }

  private fun logWork(message: String, e: Exception? = null) {
    val logFile = applicationContext.cacheDir.toPath().resolve("reserve.log")
    logFile.writer(UTF_8, APPEND, CREATE).use {
      it.write("${LocalDateTime.now().format(ISO_LOCAL_DATE_TIME)}: $message\n")
      e?.printStackTrace(PrintWriter(it))
    }
  }

  companion object {
    const val TAG = "ReserveManagerWorker"
  }
}