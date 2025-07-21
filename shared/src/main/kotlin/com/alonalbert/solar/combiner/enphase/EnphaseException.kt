package com.alonalbert.solar.combiner.enphase

class EnphaseException private constructor(
  message: String,
  val reason: String,
  cause: Exception? = null,
) : Exception(message, cause) {
  constructor(message: String, httpStatus: Int) : this(message, "$httpStatus")
  constructor(message: String, cause: Exception) : this(message, "${cause.message}")
}