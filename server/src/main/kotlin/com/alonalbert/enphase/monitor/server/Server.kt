package com.alonalbert.enphase.monitor.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
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
) {
  @Scheduled(timeUnit = TimeUnit.SECONDS, fixedRate = 60)
  fun updateReserve() {
    runBlocking(Dispatchers.Default) {
      reserveManager.updateReserve()
    }
  }
}

fun main(args: Array<String>) {
  runApplication<Server>(*args)
}

