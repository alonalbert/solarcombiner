package com.alonalbert.solar.combiner.enphase

import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.CACHE
import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.CACHE_ONLY
import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.NO_CACHE
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import com.alonalbert.solar.combiner.enphase.model.Energy
import com.alonalbert.solar.combiner.enphase.model.GetTokenRequest
import com.alonalbert.solar.combiner.enphase.model.LiveStatus
import com.alonalbert.solar.combiner.enphase.model.SetProfileRequest
import com.alonalbert.solar.combiner.enphase.util.DefaultLogger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.internal.closeQuietly
import okio.IOException
import org.slf4j.Logger
import java.io.Closeable
import java.nio.file.Path
import java.security.SecureRandom
import java.time.LocalDate
import java.util.Properties
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.LazyThreadSafetyMode.SYNCHRONIZED
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.google.gson.JsonObject as GsonObject

private const val LOGIN_URL = "https://enlighten.enphaseenergy.com/login/login.json"
private const val TOKEN_URL = "https://entrez.enphaseenergy.com/tokens"
private const val LIVE_STREAM_URL = "https://enlighten.enphaseenergy.com/pv/aws_sigv4/livestream.json"
private const val DAILY_ENERGY =
  "https://enlighten.enphaseenergy.com/pv/systems/%1\$s/daily_energy?start_date=%2\$d-%3$02d-%4$02d&end_date=%2\$d-%3$02d-%4$02d"
private const val COOKIE = "_enlighten_4_session"
private const val BAD_VALUE = 30_000

private val gson = GsonBuilder()
  .setPrettyPrinting()
  .create()

class Enphase(
  private val email: String,
  password: String,
  private val mainSiteId: String,
  private val mainSiteSerialNum: String,
  mainSiteHost: String,
  mainSitePort: Int,
  private val exportSiteId: String,
  private val exportSiteSerialNum: String,
  exportSiteHost: String,
  exportSitePort: Int,
  cacheDir: Path,
  coroutineScope: CoroutineScope,
  private val logger: Logger = DefaultLogger()
) : Closeable {
  private val cache = Cache(cacheDir)
  private val client = createClient()
  private val _sessionId by lazy(SYNCHRONIZED) {
    coroutineScope.async {
      client.getSessionId(email, password)
    }
  }

  private suspend fun sessionId() = _sessionId.await()
  private val mainEnvoyUrl = "https://$mainSiteHost:$mainSitePort"
  private val exportEnvoyUrl = "https://$exportSiteHost:$exportSitePort"

  enum class CacheMode {
    NO_CACHE,
    CACHE_ONLY,
    CACHE,
  }

  suspend fun getDailyEnergy(date: LocalDate, cacheMode: CacheMode = CACHE): DailyEnergy? {
    return withContext(Dispatchers.Unconfined) {
      val innerStats = loadData(mainSiteId, date, cacheMode)
      val outerStats = loadData(exportSiteId, date, cacheMode)
      if (innerStats == null && outerStats == null) {
        if (cacheMode == CACHE_ONLY) {
          return@withContext null
        } else {
          throw IllegalStateException("Failed to load data")
        }
      }

      val outerProduction = outerStats.getDoubles("production")
      val innerProduction = innerStats.getDoubles("production")
      val consumption = innerStats.getDoubles("consumption")
      val charge = innerStats.getDoubles("charge")
      val discharge = innerStats.getDoubles("discharge")
      val innerExport = innerStats.getDoubles("solar_grid")
      val gridBattery = innerStats.getDoubles("grid_battery")
      val gridHome = innerStats.getDoubles("grid_home")
      val import = gridHome.zip(gridBattery) { h, b -> h + b }
      val batteryLevel = innerStats?.getAsJsonArray("soc")?.map { if (it is JsonNull) null else it.asInt } ?: emptyList()

      val energies = buildList {
        repeat(96) {
          add(
            Energy(
              outerProduction[it].kwh(),
              innerProduction[it].kwh(),
              consumption[it].kwh(),
              charge[it].kwh(),
              discharge[it].kwh(),
              innerExport[it].kwh(),
              import[it].kwh(),
              batteryLevel[it],
            )
          )
        }
      }
      DailyEnergy(date, energies)
    }
  }

  suspend fun streamLiveStatus(delay: Duration = 1.seconds): Flow<LiveStatus> {
    enableLiveStatus(mainSiteSerialNum)
    enableLiveStatus(exportSiteSerialNum)
    val mainToken = getEnvoyToken(mainSiteSerialNum)
    val exportToken = getEnvoyToken(exportSiteSerialNum)

    return flow {
      while (true) {
        val mainStatus = withContext(IO) { getLiveStatus(mainEnvoyUrl, mainToken) }
        val exportStatus = withContext(IO) { getLiveStatus(exportEnvoyUrl, exportToken) }
        val combined = LiveStatus(
          pv = mainStatus.pv + exportStatus.pv,
          storage = mainStatus.storage,
          grid = mainStatus.grid - exportStatus.pv,
          load = mainStatus.load,
        )
        emit(combined)
        delay(delay)
      }
    }
  }

  private suspend fun getLiveStatus(url: String, token: Any): LiveStatus {
    return try {
      val response = client.get("$url/ivp/livedata/status") {
        accept(Application.Json)
        header("Authorization", "Bearer $token")
      }

      val json = Json.decodeFromString<JsonObject>(response.bodyAsText())

      val meters = json.getValue("meters").jsonObject
      val pv = meters.getKiloWatts("pv")
      val storage = meters.getKiloWatts("storage")
      val grid = meters.getKiloWatts("grid")
      val load = meters.getKiloWatts("load")
      LiveStatus(pv, storage, grid, load)
    } catch (e: IOException) {
      logger.atTrace().setCause(e).log("Failed to get Live Status from $url")
      logger.atError().log("Failed to get Live Status from $url")
      LiveStatus(pv = 0.0, storage = 0.0, grid = 0.0, load = 0.0)
    }
  }

  private fun GsonObject.getStats(): GsonObject? {
    val array = getAsJsonArray("stats")
    return if (array.size() < 1) null else array[0].asJsonObject
  }

  private suspend fun getEnvoyToken(serialNum: String): String {
    val response = client.post(TOKEN_URL) {
      contentType(Application.Json)
      setBody(GetTokenRequest(sessionId(), serialNum, email))
    }
    return response.bodyAsText()
  }


  private suspend fun enableLiveStatus(serialNum: String) {
    client.get("$LIVE_STREAM_URL?serial_num=$serialNum") {
      cookie(COOKIE, sessionId())
    }.bodyAsText()
  }

  private suspend fun loadData(siteId: String, date: LocalDate, cacheMode: CacheMode): GsonObject? {
    return withContext(IO) {
      if (cacheMode != NO_CACHE) {
        val cached = cache.read(siteId, date)
        if (cached != null) {
          return@withContext gson.getObject(cached).getStats()
        }
      }

      val response = client.get(DAILY_ENERGY.format(siteId, date.year, date.month.value, date.dayOfMonth)) {
        cookie(COOKIE, sessionId())
      }
      val data = response.bodyAsPrettyJson()
      cache.write(siteId, date, data)
      return@withContext gson.getObject(data).getStats()
    }
  }

  suspend fun setBatteryReserve(reserve: Int): String {
    val response = client.put("https://enlighten.enphaseenergy.com/service/batteryConfig/api/v1/profile/${this.mainSiteId}") {
      cookie(COOKIE, sessionId())
      contentType(Application.Json)
      setBody(SetProfileRequest("self-consumption", reserve.toString()))
    }.body<JsonObject>()
    return response["message"]?.jsonPrimitive?.content ?: "Unexpected error"
  }

  override fun close() {
    client.closeQuietly()
  }

  companion object {
    fun fromProperties(
      coroutineScope: CoroutineScope,
      logger: Logger = DefaultLogger(),
    ): Enphase {
      val properties = ClassLoader.getSystemClassLoader().getResourceAsStream("local.properties").use {
        Properties().apply {
          load(it)
        }
      }
      val email = properties.getProperty("login.email")
      val password = properties.getProperty("login.password")
      val mainSiteId = properties.getProperty("site.main")
      val exportSiteId = properties.getProperty("site.export")
      val mainSerialNum = properties.getProperty("envoy.main.serial")
      val mainHost = properties.getProperty("envoy.main.host")
      val mainPort = properties.getProperty("envoy.main.port").toInt()
      val exportSerialNum = properties.getProperty("envoy.export.serial")
      val exportHost = properties.getProperty("envoy.export.host")
      val exportPort = properties.getProperty("envoy.export.port").toInt()
      return Enphase(
        email,
        password,
        mainSiteId,
        mainSerialNum,
        mainHost,
        mainPort,
        exportSiteId,
        exportSerialNum,
        exportHost,
        exportPort,
        Path.of("cache"),
        coroutineScope,
        logger,
      )
    }
  }
}

private suspend fun HttpClient.getSessionId(email: String, password: String): String {
  return withContext(IO) {
    val response = submitForm(
      LOGIN_URL,
      parameters {
        append("user[email]", email)
        append("user[password]", password.trim())
      })
    return@withContext gson.getObject(response.bodyAsText())["session_id"].asString
  }
}

private suspend fun HttpResponse.bodyAsPrettyJson() = gson.toJson(JsonParser.parseString(bodyAsText()))

private fun Gson.getObject(json: String) = fromJson(json, GsonObject::class.java)

private fun GsonObject?.getDoubles(key: String): List<Double> {
  return this?.getAsJsonArray(key)?.map {
    when {
      it is JsonNull -> 0.0
      (abs(it.asDouble)) > BAD_VALUE -> 0.0
      else -> it.asDouble
    }
  } ?: List(96) { 0.0 }
}

private fun Double.kwh() = this / 1000 * 4

private fun createClient(): HttpClient {
  return HttpClient(OkHttp) {
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

private fun JsonObject.getKiloWatts(key: String) =
  getValue(key).jsonObject.getValue("agg_p_mw").jsonPrimitive.double / 1_000_000
