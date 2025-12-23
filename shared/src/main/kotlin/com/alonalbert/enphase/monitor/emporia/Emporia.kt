package com.alonalbert.enphase.monitor.emporia

import com.alonalbert.enphase.monitor.emporia.model.EmporiaChannel
import com.alonalbert.enphase.monitor.emporia.model.EmporiaChannelUsage
import com.alonalbert.enphase.monitor.enphase.EnphaseException
import com.alonalbert.enphase.monitor.enphase.TrustingManager
import com.alonalbert.enphase.monitor.enphase.util.DefaultLogger
import com.alonalbert.enphase.monitor.enphase.util.plusHours
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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import java.security.SecureRandom
import java.time.Instant
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

private const val APP_API_URL = "https://c-api.emporiaenergy.com/v1/migrated/app-api"
private const val API_URL = "https://api.emporiaenergy.com"

private const val DEVICES_URL = "$API_URL/customers/devices"
private const val USAGE_URL = "$APP_API_URL/chart-usage?deviceGid=%d&channel=%s&start=%s&end=%s&scale=1MIN&energyUnit=KilowattHours"

private val jsonPath = JsonPath.using(
  builder()
    .jsonProvider(GsonJsonProvider())
    .mappingProvider(GsonMappingProvider())
    .build()
)
private val channelsType = object : TypeRef<List<EmporiaChannel>>() {}

private val doublesType = object : TypeRef<List<Double?>>() {}

class Emporia(
  private val username: String,
  private val password: String,
  private val logger: Logger = DefaultLogger()
) : AutoCloseable {
  private val client = createClient()

  suspend fun getChannels(): List<EmporiaChannel> {
    return withContext(IO) {
      val response = client.get(DEVICES_URL)
      val json = response.bodyAsText()

      jsonPath.parse(json).read("$.devices[*].devices[*].channels[*]", channelsType)
    }
  }

  suspend fun getUsage(
    channel: EmporiaChannel,
    start: Instant,
    end: Instant,
  ): List<Double> {
    return withContext(IO) {
      val url = USAGE_URL.format(channel.deviceGid, channel.channelId, start, end)

      var usages: List<Double>?
      while (true) {
        usages = getUsages(url).filterNotNull()
        if (usages.isNotEmpty()) {
          break
        }
        logger.info("Channel ${channel.channelId} was null, retrying")
      }
      usages
    }
  }

  suspend fun getDailyUsage(start: Instant): List<EmporiaChannelUsage> {
    val channels = getChannels()
    return coroutineScope {
      channels.map {
        async {
          getDailyUsage(start, it)
        }
      }
    }.awaitAll()
  }

  suspend fun getDailyUsage(start: Instant, channel: EmporiaChannel): EmporiaChannelUsage {
    val usage = coroutineScope {
      listOf(
        async { getUsage(channel, start, start.plusHours(12)) },
        async { getUsage(channel, start.plusHours(12), start.plusHours(24)) }
      )
    }.awaitAll().flatten()
    return EmporiaChannelUsage(start, channel, usage)
  }

  private suspend fun getUsages(url: String): List<Double?> {
    val response = client.get(url)
    val json = response.bodyAsText()
    val usages = jsonPath.parse(json).read("$.usageList[*]", doublesType)
    return usages
  }

  override fun close() {
    client.close()
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
        providers.add(EmporiaAuthProvider(username, password))
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
}