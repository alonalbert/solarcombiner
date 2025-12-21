package com.alonalbert.enphase.monitor.server.emporia

import com.alonalbert.enphase.monitor.emporia.Emporia
import com.alonalbert.enphase.monitor.emporia.model.ChannelUsage
import com.alonalbert.enphase.monitor.enphase.util.plusHours
import com.alonalbert.enphase.monitor.enphase.util.plusMinutes
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class EmporiaService(
  private val channelRepository: ChannelRepository,
  private val usageRepository: UsageRepository,
  private val emporia: Emporia
) {
  private val logger = LoggerFactory.getLogger(EmporiaService::class.java)

  suspend fun getUsage(day: LocalDate): List<ChannelUsage> {
    if (day == LocalDate.now()) {
      synchronizeDay(day)
    }
    val start = ZonedDateTime.of(day, LocalTime.MIDNIGHT, ZoneId.of("America/Los_Angeles")).toInstant()
    val end = start.plusHours(24)
    val usages = usageRepository.getUsages(start, end)
    return usages.groupBy { it.channel }.map { (channel, usage) ->
      ChannelUsage(channel.name, usage.sortedBy { it.timestamp }.map { it.value })
    }
  }

  @Scheduled(cron = "0 1 0 * * *")
  suspend fun synchronizeYesterday() {
    synchronizeDay(LocalDate.now().minusDays(1))
  }

  @Transactional
  private suspend fun synchronizeDay(day: LocalDate) {
    val start = ZonedDateTime.of(day, LocalTime.MIDNIGHT, ZoneId.of("America/Los_Angeles")).toInstant()
    try {
      val channelUsages = emporia.getDailyUsage(start)
      usageRepository.deleteUsages(start, start.plusHours(24))
      channelUsages.forEach {
        val first = it.first
        val channel = upsertChannel(it.channel.deviceGid, it.channel.channelId, it.channel.name, it.channel.channelMultiplier)
        val usages = it.usage.mapIndexed { i, usage ->
          Usage(channel = channel, timestamp = first.plusMinutes(i), value = usage)
        }
        usageRepository.saveAll(usages)
      }
      logger.info("Emporia data synchronization task finished successfully.")
    } catch (e: Exception) {
      logger.error("Error during Emporia data synchronization", e)
    }
  }

  @Transactional
  fun upsertChannel(deviceId: Int, channelId: String, name: String, multiplier: Double): Channel {
    val channel = channelRepository.findByDeviceIdAndChannelId(deviceId, channelId).getOrNull()
    val newChannel = when (channel) {
      null -> Channel(0, deviceId, channelId, name, multiplier)
      else -> channel.copy(name = name, multiplier = multiplier)
    }
    if (channel != newChannel) {
      channelRepository.save(newChannel)
    }
    return newChannel
  }
}
