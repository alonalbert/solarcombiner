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
class Server {

  @Scheduled(timeUnit = TimeUnit.MINUTES, fixedRate = 1)
  fun autoWatch() {
    runBlocking(Dispatchers.Default) {
    }
  }
}

fun main(args: Array<String>) {
  runApplication<Server>(*args)
}

