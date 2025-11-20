package com.alonalbert.enphase.monitor.server

import jakarta.validation.Valid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
internal class SettingsController(
  private val setting: SettingRepository,
  private val reserveManager: ReserveManager,
) {
  private val logger = LoggerFactory.getLogger(SettingsController::class.java)

  @GetMapping("/get-enphase-config")
  fun getEnphaseConfig(): EnphaseConfig {
    return setting.getEnphaseConfig()
  }

  @GetMapping("/get-reserve-config")
  fun getReserveConfig(): ReserveConfig {
    return setting.getReserveConfig()
  }

  @PutMapping("/put-reserve-config")
  fun putReserveConfig(
    @Valid @RequestBody reserveConfig: ReserveConfig
  ): ResponseEntity<ReserveConfig> {
    setting.putReserveConfig(reserveConfig)
    logger.info("Updated $reserveConfig")
    runBlocking(Dispatchers.Default) {
      reserveManager.updateReserve()
    }
    return ResponseEntity.ok(reserveConfig)
  }
}
