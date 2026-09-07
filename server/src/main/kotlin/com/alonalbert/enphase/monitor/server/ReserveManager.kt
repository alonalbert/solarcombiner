package com.alonalbert.enphase.monitor.server

import com.alonalbert.enphase.monitor.enphase.Credentials
import com.alonalbert.enphase.monitor.enphase.Enphase
import com.alonalbert.enphase.monitor.enphase.ReserveCalculator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalTime
import java.time.ZoneId

@Component
internal class ReserveManager(
  private val setting: SettingRepository,
) {
  private val logger = LoggerFactory.getLogger(Server::class.java)
  private val config by lazy { setting.getEnphaseConfig() }
  private var currentReserve = -1

  suspend fun updateReserve() {
    val reserveConfig = setting.getReserveConfig()
    if (!reserveConfig.enabled) {
      return
    }
    val enphase = Enphase({ Credentials(config.email, config.password)}, logger)

    val now = LocalTime.now(ZoneId.systemDefault())
    val batteryCapacity = enphase.getBatteryCapacity(config.mainSite)
    val reserve = ReserveCalculator.calculateReserve(
      now,
      reserveConfig.idleLoad,
      batteryCapacity,
      reserveConfig.minReserve,
      reserveConfig.chargeStart,
      reserveConfig.chargeEnd
    )
    if (reserve == currentReserve) {
      return
    }
    enphase.setBatteryReserve(config.mainSite, reserve)
    logger.info("Reserve set to $reserve%")
    currentReserve = reserve
  }
}