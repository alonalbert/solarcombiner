package com.alonalbert.enphase.monitor.client

import com.alonalbert.enphase.monitor.db.EnphaseConfig
import com.alonalbert.enphase.monitor.db.ReserveConfig
import com.alonalbert.enphase.monitor.emporia.model.ChannelUsage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate

class Client(
  private val server: String,
  private val username: String,
  private val password: String,
) {

  suspend fun getEnphaseConfig() = get<EnphaseConfig>("get-enphase-config")

  suspend fun getReserveConfig() = get<ReserveConfig>("get-reserve-config")

  suspend fun putReserveConfig(reserveConfig: ReserveConfig)  = put("put-reserve-config", reserveConfig)

  suspend fun getChannelUsage(date: LocalDate) = get<List<ChannelUsage>>("emporia/usage?date=$date")

  private fun httpClient() = HttpClient(Android) {
    install(Logging) {
      logger = TimberLogger
      this.level = LogLevel.INFO
    }
    install(ContentNegotiation) {
      json()
    }
    install(HttpTimeout) {
      requestTimeoutMillis = 60_000
    }
    install(Auth) {
      basic {
        sendWithoutRequest {
          true
        }
        credentials {
          BasicAuthCredentials(this@Client.username, this@Client.password)
        }
      }
    }

  }

  private suspend inline fun <reified T> get(url: String): T {
    return httpClient().use {
      withContext(Dispatchers.IO) {
        it.get(getUrl(url)).body()
      }
    }
  }

  private suspend inline fun <reified T> put(url: String, value: T): T {
    return httpClient().use {
      withContext(Dispatchers.IO) {
        it.put(getUrl(url)) {
          contentType(ContentType.Application.Json)
          setBody(value)
        }.body()
      }
    }
  }

  private fun getUrl(segment: String) = "http://$server/api/$segment"

  private object TimberLogger : Logger {
    override fun log(message: String) {
      Timber.tag("EM-HTTP").v(message)
    }
  }
}