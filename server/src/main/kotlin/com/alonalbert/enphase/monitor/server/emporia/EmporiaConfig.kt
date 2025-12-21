package com.alonalbert.enphase.monitor.server.emporia

import com.alonalbert.enphase.monitor.emporia.Emporia
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EmporiaConfig {
  @Bean
  fun emporia(
    @Value($$"${emporia.username}") username: String,
    @Value($$"${emporia.password}") password: String
  ): Emporia {
    return Emporia(username, password, LoggerFactory.getLogger(EmporiaService::class.java))
  }
}