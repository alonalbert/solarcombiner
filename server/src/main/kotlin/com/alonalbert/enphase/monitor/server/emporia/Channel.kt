package com.alonalbert.enphase.monitor.server.emporia

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

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
