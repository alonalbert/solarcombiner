package com.alonalbert.enphase.monitor.server

import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class ReserveManagerController(
    environment: Environment,
) {
    private val logger = LoggerFactory.getLogger(ReserveManagerController::class.java)

    @GetMapping("/get-reserve-config")
    fun getReserveConfig(): ReserveConfig {
        return ReserveConfig(false, 0.9, 30, 8, 15)
    }

    @PutMapping("/put-reserve-config")
    fun putReserveConfig(
        @Valid @RequestBody reserveConfig: ReserveConfig
    ): ResponseEntity<ReserveConfig> {
      return ResponseEntity.ok(ReserveConfig(false, 0.9, 30, 8, 15))
    }
}
