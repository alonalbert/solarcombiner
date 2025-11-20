package com.alonalbert.enphase.monitor.server

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

private const val ENABLED = "reserve.manager.enabled"
private const val IDLE_LOAD = "reserve.manager.idle.load"
private const val MIN = "reserve.manager.min"
private const val START = "reserve.manager.charge.start"
private const val END = "reserve.manager.charge.end"

private const val EMAIL = "login.email"
private const val PASSWORD = "login.password"

private const val MAIN_SITE = "site.main"
private const val MAIN_SERIAL = "envoy.main.serial"
private const val MAIN_HOST = "envoy.main.host"
private const val MAIN_PORT = "envoy.main.port"

private const val EXPORT_SITE = "site.export"
private const val EXPORT_SERIAL = "envoy.export.serial"
private const val EXPORT_HOST = "envoy.export.host"
private const val EXPORT_PORT = "envoy.export.port"

@Repository
internal interface SettingRepository : JpaRepository<Setting, String> {
  fun getEnphaseConfig(): EnphaseConfig {
    val email = getString(EMAIL)
    val password = getString(PASSWORD)
    val mainSite = getString(MAIN_SITE)
    val mainSerial = getString(MAIN_SERIAL)
    val mainHost = getString(MAIN_HOST)
    val mainPort = getInt(MAIN_PORT)
    val exportSite = getString(EXPORT_SITE)
    val exportSerial = getString(EXPORT_SERIAL)
    val exportHost = getString(EXPORT_HOST)
    val exportPort = getInt(EXPORT_PORT)
    return EnphaseConfig(email, password, mainSite, mainSerial, mainHost, mainPort, exportSite, exportSerial, exportHost, exportPort)
  }

  fun getReserveConfig(): ReserveConfig {
    val enabled = getBoolean(ENABLED)
    val idleLoad = getDouble(IDLE_LOAD)
    val minReserve = getInt(MIN)
    val chargeStart = getInt(START)
    val chargeEnd = getInt(END)
    return ReserveConfig(enabled, idleLoad, minReserve, chargeStart, chargeEnd)
  }

  fun putReserveConfig(reserveConfig: ReserveConfig) {
    val values = mapOf(
      ENABLED to reserveConfig.enabled.toString(),
      IDLE_LOAD to reserveConfig.idleLoad.toString(),
      MIN to reserveConfig.minReserve.toString(),
      START to reserveConfig.chargeStart.toString(),
      END to reserveConfig.chargeEnd.toString(),
    )
    saveAll(values)
  }

  private fun getString(name: String) = getReferenceById(name).value

  private fun getInt(name: String) = getString(name).toInt()

  private fun getDouble(name: String) = getString(name).toDouble()

  private fun getBoolean(name: String) = getString(name).toBoolean()

  fun saveAll(settings: Map<String, String>) {
    saveAll(settings.map { Setting(it.key, it.value) })
  }
}
