package com.alonalbert.enphase.monitor.server.emporia

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ChannelRepository : JpaRepository<Channel, Long> {
  fun findByDeviceIdAndChannelId(deviceId: Int, channelId: String): Optional<Channel>
}
