package com.alonalbert.solar.combiner.enphase.util

val Double.kwh get() = "%.2f kWh".format(this / 1000)