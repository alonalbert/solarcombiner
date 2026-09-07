package com.alonalbert.enphase.monitor.server

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit.MINUTES

@SpringBootApplication
@PropertySource("classpath:local.properties")
@EntityScan("com.alonalbert.enphase.monitor.server")
@EnableScheduling
internal class Server(
  private val reserveManager: ReserveManager,
  private val databaseSeeder: DatabaseSeeder,
) {

  @PostConstruct
  fun seedDatabase() {
    databaseSeeder.seedDatabase()
  }

  @Scheduled(timeUnit = MINUTES, fixedRate = 5)
  suspend fun updateReserve() {
    reserveManager.updateReserve()
  }

}

fun main(args: Array<String>) {
  runApplication<Server>(*args)
}

