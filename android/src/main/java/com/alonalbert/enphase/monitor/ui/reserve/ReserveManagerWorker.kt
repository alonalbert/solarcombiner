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
import java.time.LocalTime

@HiltWorker
class ReserveManagerWorker @AssistedInject constructor(
  @Assisted appContext: Context,
  @Assisted workerParams: WorkerParameters,
  private val db: AppDatabase,
  private val enphaseAsync: Deferred<Enphase>,
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    Timber.i("ReserveManagerWorker: doWork()")
    return try {
      if (!NetworkChecker.checkNetwork(applicationContext)) {
        Timber.w("Network connected but not validated. Might be an issue in Doze. Retrying.")
        return Result.retry()
      }

      val config = db.reserveConfigDao().getReserveConfig()
      if (config == null) {
        Timber.w("Reserve configuration not found")
        return Result.failure()
      }
      val settings = db.settingsDao().getSettings()
      if (settings == null) {
        Timber.w("Settings not found")
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
      Result.success()
    } catch (e: IOException) {
      Timber.e(e, "Failed to set reserve")
      Result.retry()
    } catch (e: Exception) {
      Timber.e(e, "Failed to set reserve")
      Result.failure()
    }
  }

  companion object {
    const val TAG = "ReserveManagerWorker"
  }
}