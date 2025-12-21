package com.alonalbert.enphase.monitor.server.emporia

import com.alonalbert.enphase.monitor.emporia.model.ChannelUsage
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import kotlin.time.measureTimedValue

@RestController
@RequestMapping("/api")
class EmporiaController(private val emporiaService: EmporiaService) {
  private val logger = LoggerFactory.getLogger(EmporiaController::class.java)

  @GetMapping("/emporia/usage")
  suspend fun getUsage(@RequestParam date: String): List<ChannelUsage> {
    val (value, duration) = measureTimedValue { emporiaService.getUsage(LocalDate.parse(date)) }
    logger.info("getUsage($date) took $duration")
    return value
  }
}