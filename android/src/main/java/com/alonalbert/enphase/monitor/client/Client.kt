package com.alonalbert.enphase.monitor.client

import com.alonalbert.enphase.monitor.db.EnphaseConfig
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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class Client(
  private val server: String,
  private val username: String,
  private val password: String,
) {

  suspend fun getEnphaseConfig() = get<EnphaseConfig>("get-enphase-config")

  private fun httpClient() = HttpClient(Android) {
    install(Logging) {
      logger = TimberLogger
      this.level = LogLevel.INFO
    }
    install(ContentNegotiation) {
      json()
    }
    install(HttpTimeout) {
      requestTimeoutMillis = 5_000
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

  private fun getUrl(segment: String) = "http://$server/api/$segment"

  private object TimberLogger : Logger {
    override fun log(message: String) {
      Timber.tag("PAD-HTTP").v(message)
    }
  }
}