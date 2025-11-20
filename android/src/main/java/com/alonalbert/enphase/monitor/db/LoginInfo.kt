package com.alonalbert.enphase.monitor.db

data class LoginInfo(
  val server: String = "",
  val username: String = "",
  val password: String = "",
) {
  fun isValid() = server.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
}
