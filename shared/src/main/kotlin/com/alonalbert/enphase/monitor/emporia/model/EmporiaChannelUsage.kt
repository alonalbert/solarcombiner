package com.alonalbert.enphase.monitor.emporia.model

import java.time.Instant

data class EmporiaChannelUsage(val first: Instant, val channel: EmporiaChannel, val usage: List<Double>)