package com.alonalbert.enphase.monitor.server

import com.alonalbert.enphase.monitor.server.emporia.EmporiaService
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit

@SpringBootApplication
@PropertySource("classpath:local.properties")
@EntityScan("com.alonalbert.enphase.monitor.server")
@EnableScheduling
internal class Server(
  private val reserveManager: ReserveManager,
  private val emporiaService: EmporiaService,
  private val databaseSeeder: DatabaseSeeder,
) {

  @PostConstruct
  fun seedDatabase() {
    databaseSeeder.seedDatabase()
    runBlocking {
      emporiaService.synchronizeToday()
    }
  }

  @Scheduled(timeUnit = TimeUnit.SECONDS, fixedRate = 60)
  suspend fun updateReserve() {
    reserveManager.updateReserve()
  }

}

fun main(args: Array<String>) {
  runApplication<Server>(*args)
}

