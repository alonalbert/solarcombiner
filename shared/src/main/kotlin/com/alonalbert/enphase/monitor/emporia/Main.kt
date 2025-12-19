package com.alonalbert.enphase.monitor.emporia

import com.alonalbert.enphase.monitor.emporia.model.Channel
import com.alonalbert.enphase.monitor.enphase.EnphaseException
import com.alonalbert.enphase.monitor.enphase.TrustingManager
import com.alonalbert.enphase.monitor.enphase.util.DefaultLogger
import com.google.gson.GsonBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.Logger
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

private val logger: Logger = DefaultLogger()
private val gson = GsonBuilder()
  .setPrettyPrinting()
  .create()


suspend fun main() {
  createClient().use { client ->
    client.getChannels().forEach {
      val usage = client.getUsage(it.number)
      println("${it.name}: $usage")
    }
  }
}

suspend fun HttpClient.getChannels(): List<Channel> {
  return withContext(IO) {
    val url = "https://api.emporiaenergy.com/customers/devices"
    val response = this@getChannels.get(url)
    response.body<JsonObject>().getArray("devices").flatMap { outerDevice ->
      outerDevice.getArray("devices").flatMap { innerDevice ->
        val deviceId = innerDevice.getInt("deviceGid")
        innerDevice.getArray("channels").map {
          val name = it.getString("name")
          val number = it.getInt("channelNum")
          Channel(deviceId, name, number)
        }
      }
    }
  }
}

suspend fun HttpClient.getUsage(branch: Int): Double {
  val did = 470706
  val channel = "Branch_$branch"
  val end = Instant.now()
  val start = end.minus(15, MINUTES)
  val scale = "1MIN"
  val url =
    "https://c-api.emporiaenergy.com/v1/migrated/app-api/chart-usage?deviceGid=$did&channel=$channel&start=$start&end=$end&scale=$scale&energyUnit=KilowattHours"
  return withContext(IO) {
    val response = get(url)
    val body = response.body<JsonObject>()
    body.getArray("usageList").sumOf {
      it.getDouble()
    }
  }
}


private fun createClient(): HttpClient {
  logger.info("Creating Enphase client")
  return HttpClient(OkHttp) {
    HttpResponseValidator {
      validateResponse {
        val status = it.status
        if (!status.isSuccess()) {
          val url = it.request.url
          logger.error("Failed to load from $url: $status:\n${it.bodyAsText()}")
          throw EnphaseException("Failed to load from $url: $status", status.value)
        }
      }
    }
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
    install(HttpCookies) {
      storage = AcceptAllCookiesStorage()
    }

    install(Auth) {
      providers.add(EmporiaAuthProvider())
    }

    engine {
      config {
        val trustAllCertificates = arrayOf(TrustingManager())
        val sslContext = SSLContext.getInstance("SSL") // Or "TLS"
        sslContext.init(null, trustAllCertificates, SecureRandom())
        sslSocketFactory(sslContext.socketFactory, trustAllCertificates[0] as X509TrustManager)
        hostnameVerifier { _, _ -> true }
      }
    }
  }
}

private fun JsonElement.getArray(key: String) = jsonObject.getValue(key).jsonArray
private fun JsonElement.getString(key: String) = jsonObject.getValue(key).jsonPrimitive.content
private fun JsonElement.getInt(key: String) = jsonObject.getValue(key).jsonPrimitive.int
private fun JsonElement.getDouble() = jsonPrimitive.double