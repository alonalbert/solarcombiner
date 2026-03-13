package com.alonalbert.enphase.monitor.server.emporia

import jakarta.persistence.*

private val ANNOTATION_REGEX = """^.* [x-]\d+$""".toRegex()

@Entity
@Table(
  name = "channels",
  uniqueConstraints = [UniqueConstraint(columnNames = ["deviceId", "channelId"])]
)
data class Channel(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long = 0,

  val deviceId: Int,
  val channelId: String,
  val name: String,
  val multiplier: Double,
)

fun Channel.displayName() = when {
  name.matches(ANNOTATION_REGEX) -> name.substringBeforeLast(' ')
  else -> name
}

fun main() {
  println(Channel(0, 0, "", "foo", 1.0).displayName())
  println(Channel(0, 0, "", "foo -14", 1.0).displayName())
  println(Channel(0, 0, "", "foo x2", 1.0).displayName())
}