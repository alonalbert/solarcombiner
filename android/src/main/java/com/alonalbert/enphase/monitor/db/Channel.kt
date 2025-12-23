package com.alonalbert.enphase.monitor.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  indices = [Index(value = ["channelId"], unique = true)]
)
data class Channel(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "id")
  val id: Long = 0,

  val channelId: String,
  val name: String,
)
