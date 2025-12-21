package com.alonalbert.enphase.monitor.emporia.model

import java.time.Instant

data class ChannelUsage(val first: Instant, val channel: Channel, val usage: List<Double>)