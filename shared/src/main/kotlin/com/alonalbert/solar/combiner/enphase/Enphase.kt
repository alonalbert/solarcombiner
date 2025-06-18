package com.alonalbert.solar.combiner.enphase

import com.alonalbert.solar.combiner.enphase.model.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val LOGIN_URL = "https://enlighten.enphaseenergy.com/login/login.json"

class Enphase private constructor(private val client: HttpClient, private val sessionId: String) {

  companion object {
    suspend fun create(email: String, password: String): Enphase {
      val client = HttpClient(CIO) {
        install(ContentNegotiation) {
          json(Json {
            prettyPrint = true
            isLenient = true
          })
        }
        followRedirects = true
      }
      val response = client.submitForm(
        LOGIN_URL,
        parameters {
          append("user[email]", email)
          append("user[password]", password)
        })
      val loginData = response.body<LoginResponse>()
      return Enphase(client, loginData.sessionId)
    }

  }
}