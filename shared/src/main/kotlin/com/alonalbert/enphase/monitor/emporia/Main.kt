package com.alonalbert.enphase.monitor.emporia

import com.alonalbert.enphase.monitor.emporia.model.Channel
import com.alonalbert.enphase.monitor.enphase.EnphaseException
import com.alonalbert.enphase.monitor.enphase.TrustingManager
import com.alonalbert.enphase.monitor.enphase.util.DefaultLogger
import com.jayway.jsonpath.Configuration.builder
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.TypeRef
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider
import io.ktor.client.HttpClient
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
import org.slf4j.Logger
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit.MINUTES
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

private val logger: Logger = DefaultLogger()
private val jsonPath = JsonPath.using(
  builder()
    .jsonProvider(GsonJsonProvider())
    .mappingProvider(GsonMappingProvider())
    .build()
)

private val channelsType = object : TypeRef<List<Channel>>() {}
private val doublesType = object : TypeRef<List<Double>>() {}

suspend fun main() {
  createClient().use { client ->
    client.getChannels().forEach {
      val usage = client.getUsage(it)
      println("${it.name}: ${(usage * 1000).toInt()} watts")
    }
  }
}

suspend fun HttpClient.getChannels(): List<Channel> {
  return withContext(IO) {
    val url = "https://api.emporiaenergy.com/customers/devices"
    val response = this@getChannels.get(url)
    val json = response.bodyAsText()

    jsonPath.parse(json).read("$.devices[*].devices[*].channels[*]", channelsType)
  }
}

suspend fun HttpClient.getUsage(channel: Channel): Double {
  val did = channel.deviceGid
  val channel = channel.channelId
  val end = Instant.now()
  val start = end.minus(15, MINUTES)
  val scale = "1MIN"
  val url =
    "https://c-api.emporiaenergy.com/v1/migrated/app-api/chart-usage?deviceGid=$did&channel=$channel&start=$start&end=$end&scale=$scale&energyUnit=KilowattHours"
  return withContext(IO) {
    val response = get(url)
    val json = response.bodyAsText()
    jsonPath.parse(json).read("$.usageList[*]", doublesType).sum()
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
