package com.alonalbert.solar.combiner.enphase

import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private const val KEY_FORMAT = "%1\$s/%2$04d/%3$02d/%2$04d-%3$02d-%4$02d.json"

internal class Cache(private val dir: Path) {

  fun read(siteId: String, date: LocalDate): String? {
    val path = dir.resolve(date.toCacheKey(siteId))
    if (path.notExists()) {
      return null
    }
    return path.readText()
  }

  fun write(siteId: String, date: LocalDate, content: String) {
    val path = dir.resolve(date.toCacheKey(siteId))
    path.parent.createDirectories()
    path.writeText(content)
  }
}

private fun LocalDate.toCacheKey(siteId: String) = KEY_FORMAT.format(siteId, year, month.value, dayOfMonth)
