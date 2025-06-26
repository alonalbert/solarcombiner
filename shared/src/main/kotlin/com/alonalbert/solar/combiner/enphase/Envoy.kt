package com.alonalbert.solar.combiner.enphase

import com.alonalbert.solar.combiner.enphase.model.GetTokenRequest
import com.alonalbert.solar.combiner.enphase.model.RealtimeData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val LOGIN_URL = "https://enlighten.enphaseenergy.com/login/login.json"
private const val TOKEN_URL = "http://entrez.enphaseenergy.com/tokens"
private const val LIVE_STREAM_URL = "https://enlighten.enphaseenergy.com/pv/aws_sigv4/livestream.json"

class Envoy private constructor(
  private val client: HttpClient,
  private val baseUrl: String,
  private val serialNum: String,
  private val token: String,
) {

  suspend fun enableRealtimeData() {
    client.get("$LIVE_STREAM_URL?serial_num=$serialNum").bodyAsText()
  }

  suspend fun getRealtimeData(): RealtimeData {
    val response = client.get("$baseUrl/ivp/livedata/status") {
      accept(Application.Json)
      header("Authorization", "Bearer $token")
    }

    val json = Json.decodeFromString<JsonObject>(response.bodyAsText())

    val meters = json.getValue("meters").jsonObject
    val pv = meters.getKiloWatts("pv")
    val storage = meters.getKiloWatts("storage")
    val grid = meters.getKiloWatts("grid")
    val load = meters.getKiloWatts("load")
    return RealtimeData(pv, storage, grid, load)
  }

  private fun JsonObject.getKiloWatts(key: String) =
    getValue(key).jsonObject.getValue("agg_p_mw").jsonPrimitive.double / 1_000_000

  companion object {
    suspend fun create(
      email: String,
      password: String,
      host: String,
      port: Int,
      serialNum: String,
    ): Envoy {
      val client = HttpClient(CIO) {
        install(ContentNegotiation) {
          json(Json {
            prettyPrint = true
            isLenient = true
          })
        }
        // Enable redirect for all methods
        install(HttpRedirect) {
          checkHttpMethod = false
        }
        engine {
          https {
            trustManager = TrustingManager()
          }
        }
      }
      val sessionId = client.getSessionId(email, password)

      val token = client.getToken(sessionId, serialNum, email)
      return Envoy(client, "https://$host:$port", serialNum, token)
    }

    private suspend fun HttpClient.getToken(sessionId: String, serialNum: String, email: String): String {
      val response = post(TOKEN_URL) {
        contentType(Application.Json)
        setBody(GetTokenRequest(sessionId, serialNum, email))
      }
      return response.bodyAsText()

    }
  }
}

private val gson = GsonBuilder()
  .setPrettyPrinting()
  .create()

private suspend fun HttpClient.getSessionId(email: String, password: String): String {
  val response = submitForm(
    LOGIN_URL,
    parameters {
      append("user[email]", email)
      append("user[password]", password.trim())
    })
  return gson.getObject(response.bodyAsText())["session_id"].asString
}

private fun Gson.getObject(json: String) = fromJson(json, com.google.gson.JsonObject::class.java)

