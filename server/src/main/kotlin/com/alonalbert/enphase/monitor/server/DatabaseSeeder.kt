package com.alonalbert.enphase.monitor.server

import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.io.support.ResourcePropertySource
import org.springframework.stereotype.Service

@Service
internal class DatabaseSeeder(
  private val environment: Environment,
  private val settingRepository: SettingRepository,
) {

  fun seedDatabase() {
    val properties = environment.getAllProperties()
    val settings = properties.filterNot { it.key.startsWith("default.") }
    val defaultSettings = (properties - settings.keys).mapKeys { it.key.removePrefix("default.") }
    settingRepository.saveAll(settings)
    settingRepository.saveAll(defaultSettings.filterNot { settingRepository.existsById(it.key) })
  }
}

private fun Environment.getAllProperties(): Map<String, String> {
  val sources = (this as ConfigurableEnvironment).propertySources
  val properties = sources.find { it.name.contains("local.properties") } as? ResourcePropertySource ?: return emptyMap()

  return buildMap {
    properties.propertyNames.filterNot { it.startsWith("server.") }.forEach { name ->
      val value = getProperty(name) ?: return@forEach
      put(name, value)
    }
    properties.propertyNames
  }
}