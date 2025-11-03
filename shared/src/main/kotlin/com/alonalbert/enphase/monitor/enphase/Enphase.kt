package com.alonalbert.enphase.monitor.enphase

import com.alonalbert.enphase.monitor.enphase.model.BatteryState
import com.alonalbert.enphase.monitor.enphase.model.ExportStats
import com.alonalbert.enphase.monitor.enphase.model.GatewayConfig
import com.alonalbert.enphase.monitor.enphase.model.GatewayLiveStatus
import com.alonalbert.enphase.monitor.enphase.model.GetTokenRequest
import com.alonalbert.enphase.monitor.enphase.model.LiveStatus
import com.alonalbert.enphase.monitor.enphase.model.MainStats
import com.alonalbert.enphase.monitor.enphase.model.SetProfileRequest
import com.alonalbert.enphase.monitor.enphase.util.DefaultLogger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.cookies
import io.ktor.client.plugins.cookies.get
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
import io.ktor.client.statement.request
import io.ktor.http.ContentType.Application
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.internal.closeQuietly
import okio.IOException
import org.slf4j.Logger
import java.io.Closeable
import java.security.SecureRandom
import java.time.LocalDate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.google.gson.JsonObject as GsonObject

private const val BASE_URL = "https://enlighten.enphaseenergy.com"

private const val LOGIN_URL = "$BASE_URL/login/login.json"
private const val TOKEN_URL = "https://entrez.enphaseenergy.com/tokens"
private const val LIVE_STREAM_URL = "$BASE_URL/pv/aws_sigv4/livestream.json"
private const val DAILY_ENERGY_URL = "$BASE_URL/pv/systems/%1\$s/daily_energy?start_date=%2\$d-%3$02d-%4$02d&end_date=%2\$d-%3$02d-%4$02d"
private const val TODAY_URL = "$BASE_URL/pv/systems/%s/today"
private const val BATTERY_CONFIG_URL = "$BASE_URL/pv/settings/%s/battery_status.json"
private const val XSRF_TOKEN_URL = "$BASE_URL/service/pes_management/systems/2565630/inapp?type=RMA"
private const val XSRF_HEADER = "x-xsrf-token"
private const val XSRF_COOKIE = "XSRF-TOKEN"
private const val XSRF_BATTERY_PROFILE_COOKIE = "BP-XSRF-Token"

private const val BAD_VALUE = 30_000

private val gson = GsonBuilder()
  .setPrettyPrinting()
  .create()

class Enphase(
  private val logger: Logger = DefaultLogger()
) : Closeable {
  private val client = createClient()
  private var sessionId: String? = null

  suspend fun ensureLogin(email: String, password: String) {
    if (sessionId != null) {
      return
    }
    val response = client.submitForm(
      LOGIN_URL,
      parameters {
        append("user[email]", email)
        append("user[password]", password.trim())
      })
    if (response.status.isSuccess()) {
      sessionId = gson.getObject(response.bodyAsText())["session_id"].asString
    }
  }

  suspend fun getBatteryState(siteId: String): BatteryState {
    val response = client.get(TODAY_URL.format(siteId))
    val body = response.body<JsonObject>()
    val soc = body.jsonObject["battery_details"]?.jsonObject["aggregate_soc"]?.jsonPrimitive?.int
    val reserve = body.jsonObject["batteryConfig"]?.jsonObject["battery_backup_percentage"]?.jsonPrimitive?.int

    return BatteryState(soc, reserve)
  }

  suspend fun getMainStats(mainSiteId: String, date: LocalDate): MainStats {
    val stats = loadStats(mainSiteId, date)
    val gridBattery = stats.getDoubles("grid_battery")
    val gridHome = stats.getDoubles("grid_home")
    val battery = stats.getSoc()

    return MainStats(
      stats.getDoubles("production"),
      stats.getDoubles("consumption").map { it.coerceAtLeast(0.0) },
      stats.getDoubles("charge"),
      stats.getDoubles("discharge"),
      gridHome.zip(gridBattery) { gridHome, gridBattery -> gridHome + gridBattery },
      stats.getDoubles("solar_grid"),
      battery
    )
  }

  suspend fun getExportStats(exportSiteId: String, date: LocalDate): ExportStats {
    val stats = loadStats(exportSiteId, date)
    return ExportStats(
      stats.getDoubles("production"),
    )
  }

  suspend fun streamLiveStatus(
    email: String,
    mainGateway: GatewayConfig,
    exportGateway: GatewayConfig?,
    delay: Duration = 1.seconds,
  ): Flow<LiveStatus> {

    val mainToken = mainGateway.getToken(email)
    val exportToken = exportGateway?.getToken(email)


    return flow {
      while (true) {
        val deferredMainStatus = withContext(IO) {
          async { mainGateway.getLiveStatus(mainToken) }
        }
        val deferredExportStatus = withContext(IO) {
          async { exportGateway?.getLiveStatus(exportToken) }
        }
        val mainStatus = deferredMainStatus.await() ?: continue
        val exportStatus = deferredExportStatus.await()
        val exportPv = exportStatus?.pv
        val combined = LiveStatus(
          pv = mainStatus.pv,
          exportPv = exportPv,
          storage = mainStatus.storage,
          grid = mainStatus.grid - (exportPv ?: 0.0),
          load = mainStatus.load,
          soc = mainStatus.soc,
          reserve = mainStatus.reserve,
        )
        emit(combined)
        delay(delay)
      }
    }
  }

  suspend fun setBatteryReserve(siteId: String, reserve: Int): String {
    client.get(XSRF_TOKEN_URL)
    val response = client.put("$BASE_URL/service/batteryConfig/api/v1/profile/$siteId") {
      contentType(Application.Json)
      setBody(SetProfileRequest("self-consumption", reserve))
      val token = client.cookies(BASE_URL)[XSRF_COOKIE]?.value ?: return@put
      logger.info("Using XSRF: $token")
      header(XSRF_HEADER, token)
      cookie(XSRF_BATTERY_PROFILE_COOKIE, token)
    }
    val body = response.body<JsonObject>()
    return when (response.status.isSuccess()) {
      true -> body["message"]?.jsonPrimitive?.content ?: "Unexpected response: $body"
      false -> body["error"]?.jsonObject["message"]?.jsonPrimitive?.content ?: "Unexpected response $body"
    }
  }

  suspend fun getBatteryCapacity(mainSiteId: String): Double {
    val batteryConfigResponse = client.get(BATTERY_CONFIG_URL.format(mainSiteId))
    val body = batteryConfigResponse.body<JsonObject>()
    return body.getValue("max_capacity").jsonPrimitive.double
  }

  override fun close() {
    client.closeQuietly()
  }

  private suspend fun GatewayConfig.getToken(email: String): String {
    enableLiveStatus(serialNumber)
    val sessionId = sessionId ?: throw IllegalStateException("Not logged in")
    val response = client.post(TOKEN_URL) {
      contentType(Application.Json)
      setBody(GetTokenRequest(sessionId, serialNumber, email))
    }
    return response.bodyAsText()
  }

  private suspend fun GatewayConfig.getLiveStatus(token: String?): GatewayLiveStatus? {
    return try {
      val response = client.get("$url/ivp/livedata/status") {
        accept(Application.Json)
        header("Authorization", "Bearer $token")
      }
      if (!response.status.isSuccess()) {
        logger.error("Failed to get Live Status: ${response.bodyAsText()}")
        // TODO: Handle error
        throw IllegalStateException("Failed to get Live Status")
      }

      val body = response.bodyAsText()
      val json = Json.decodeFromString<JsonObject>(body)

      val meters = json.getValue("meters").jsonObject
      val pv = meters.getKiloWatts("pv")
      val storage = meters.getKiloWatts("storage")
      val grid = meters.getKiloWatts("grid")
      val load = meters.getKiloWatts("load")
      val soc = meters.getValue("soc").jsonPrimitive.int
      val reserve = meters.getValue("backup_soc").jsonPrimitive.int

      GatewayLiveStatus(pv, storage, grid, load, soc, reserve)
    } catch (e: IOException) {
      logger.atTrace().setCause(e).log("Failed to get Live Status from $url")
      logger.atError().log("Failed to get Live Status from $url")
      null
    }
  }


  private fun GsonObject.getStats(): GsonObject? {
    val array = getAsJsonArray("stats")
    return if (array.size() < 1) null else array[0].asJsonObject
  }

  private suspend fun enableLiveStatus(serialNum: String) {
    client.get("$LIVE_STREAM_URL?serial_num=$serialNum").bodyAsText()
  }

  private suspend fun loadStats(siteId: String, date: LocalDate): GsonObject? {
    return withContext(IO) {
      val url = DAILY_ENERGY_URL.format(siteId, date.year, date.month.value, date.dayOfMonth)
      val response = client.get(url)
      val data = response.bodyAsPrettyJson()
      gson.getObject(data).getStats()
    }
  }

  private fun createClient(): HttpClient {
    return HttpClient(OkHttp) {
      HttpResponseValidator {
        validateResponse {
          val status = it.status
          if (!status.isSuccess()) {
            val url = it.request.url
            if (url.toString() == XSRF_TOKEN_URL) {
              return@validateResponse
            }
            logger.error("Failed to load from $url: $status")
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

private suspend fun HttpResponse.bodyAsPrettyJson() = gson.toJson(JsonParser.parseString(bodyAsText()))

private fun Gson.getObject(json: String) = fromJson(json, GsonObject::class.java)

private fun GsonObject?.getDoubles(key: String): List<Double> {
  val values = this?.getAsJsonArray(key)
  if (values == null) {
    return List(96) { 0.0 }
  }
  val result = values.map {
    when {
      it is JsonNull -> 0.0
      (abs(it.asDouble)) > BAD_VALUE -> 0.0
      else -> it.asDouble
    }
  }
  return when {
    result.size < 96 -> result + List(96 - result.size) { 0.0 }
    result.size == 96 -> result
    else -> result.drop(result.size - 96)
  }
}

private fun GsonObject?.getSoc(): List<Int?> {
  val values = this?.getAsJsonArray("soc")
  if (values == null) {
    return List(96) { 0 }
  }
  val result = values.map {
    when (it is JsonNull) {
      true -> null
      false -> it.asInt
    }
  }
  return when {
    result.size < 96 -> result + List(96 - result.size) { null }
    result.size == 96 -> result
    else -> result.drop(result.size - 96)
  }
}

private fun JsonObject.getKiloWatts(key: String) =
  getValue(key).jsonObject.getValue("agg_p_mw").jsonPrimitive.double / 1_000_000
