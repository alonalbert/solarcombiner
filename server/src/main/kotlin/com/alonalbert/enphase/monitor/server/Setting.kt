package com.alonalbert.enphase.monitor.server

import jakarta.persistence.Entity
import jakarta.persistence.Id
import kotlinx.serialization.Serializable

@Serializable
@Entity
internal data class Setting(
  @Id
  val name: String,
  val value: String,
)
