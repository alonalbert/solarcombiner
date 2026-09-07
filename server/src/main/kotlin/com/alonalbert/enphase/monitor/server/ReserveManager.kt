package com.alonalbert.enphase.monitor.server

import com.alonalbert.enphase.monitor.enphase.Credentials
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.ReserveCalculator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@Component
internal class ReserveManager(
  private val setting: SettingRepository,
) {
  private val logger = LoggerFactory.getLogger(Server::class.java)
  private val config by lazy { setting.getEnphaseConfig() }
  private var currentReserve = -1
  private var batteryCapacity: Double? = null
  private var lastCapacityFetchTime: Instant? = null

  suspend fun updateReserve() {
    val reserveConfig = setting.getReserveConfig()
    if (!reserveConfig.enabled) {
      return
    }
    val now = LocalTime.now(ZoneId.systemDefault())
    val batteryCapacity = getBatteryCapacity()
    logger.info("Battery capacity: $batteryCapacity")
    val reserve = ReserveCalculator.calculateReserve(
      now,
      reserveConfig.idleLoad,
      batteryCapacity,
      reserveConfig.minReserve,
      reserveConfig.chargeStart,
      reserveConfig.chargeEnd
    )
    if (reserve == currentReserve) {
      logger.info("Reserve is already at $reserve%")
      return
    }
    Enphase({ Credentials(config.email, config.password) }, logger).use { enphase ->
      enphase.getBatteryCapacity(config.mainSite)
      enphase.setBatteryReserve(config.mainSite, reserve)
    }
    logger.info("Reserve set to $reserve%")
    currentReserve = reserve
  }

  private suspend fun getBatteryCapacity(): Double {
    val now = Instant.now()
    val lastFetch = lastCapacityFetchTime
    val capacity = batteryCapacity
    if (capacity == null || lastFetch == null || Duration.between(lastFetch, now) >= CACHE_TTL) {
      logger.info("Getting battery capacity from server")
      val fetchedCapacity = Enphase({ Credentials(config.email, config.password) }, logger).use { enphase ->
        enphase.getBatteryCapacity(config.mainSite)
      }
      batteryCapacity = fetchedCapacity
      lastCapacityFetchTime = now
      return fetchedCapacity
    }
    return capacity
  }

  companion object {
    private val CACHE_TTL = Duration.ofDays(1)
  }
}