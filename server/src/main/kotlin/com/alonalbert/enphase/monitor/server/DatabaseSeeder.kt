package com.alonalbert.enphase.monitor.server

import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment
import org.springframework.core.io.support.ResourcePropertySource

@Configuration
internal class DatabaseSeeder {

  /**
   * Creates a CommandLineRunner bean that seeds the database with initial settings.
   *
   * This runner is executed on application startup. It checks if the settings
   * already exist before inserting them to prevent creating duplicates on
   * subsequent launches.
   *
   * @param settingRepository The repository for accessing setting data.
   * @return A CommandLineRunner instance that performs the seeding logic.
   */
  @Bean
  fun seedDatabase(
    environment: Environment,
    settingRepository: SettingRepository,
  ): CommandLineRunner {
    return CommandLineRunner {
      val properties = environment.getAllProperties()

      val settings = properties.filterNot { it.key.startsWith("default.") }
      val defaultSettings = (properties - settings.keys).mapKeys { it.key.removePrefix("default.") }

      settingRepository.saveAll(settings)
      settingRepository.saveAll(defaultSettings.filterNot { settingRepository.existsById(it.key) })
    }
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