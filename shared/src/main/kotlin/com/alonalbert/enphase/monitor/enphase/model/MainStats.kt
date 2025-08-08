package com.alonalbert.enphase.monitor.enphase.model

class MainStats(
  val production: List<Double>,
  val consumption: List<Double>,
  val charge: List<Double>,
  val discharge: List<Double>,
  val import: List<Double>,
  val export: List<Double>,
  val battery: List<Int?>,
)
