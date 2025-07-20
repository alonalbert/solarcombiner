package com.alonalbert.solar.combiner.enphase

import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.CACHE
import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.CACHE_ONLY
import com.alonalbert.solar.combiner.enphase.Enphase.CacheMode.NO_CACHE
import com.alonalbert.solar.combiner.enphase.model.BatteryState
import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import com.alonalbert.solar.combiner.enphase.model.Energy
import com.alonalbert.solar.combiner.enphase.model.GatewayConfig
import com.alonalbert.solar.combiner.enphase.model.GetTokenRequest
import com.alonalbert.solar.combiner.enphase.model.LiveStatus
import com.alonalbert.solar.combiner.enphase.model.SetProfileRequest
import com.alonalbert.solar.combiner.enphase.util.DefaultLogger
import com.alonalbert.solar.combiner.enphase.util.toText
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.request.accept
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
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
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
import java.nio.file.Path
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
private const val BAD_VALUE = 30_000

private val gson = GsonBuilder()
  .setPrettyPrinting()
  .create()

class Enphase(
  cacheDir: Path,
  private val logger: Logger = DefaultLogger()
) : Closeable {
  private val cache = Cache(cacheDir)
  private val client = createClient()
  private var sessionId: String? = null

  enum class CacheMode {
    NO_CACHE,
    CACHE_ONLY,
    CACHE,
  }

  suspend fun login(email: String, password: String) {
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

  suspend fun getDailyEnergy(mainSiteId: String, exportSiteId: String?, date: LocalDate, cacheMode: CacheMode = CACHE): DailyEnergy? {
    return withContext(Dispatchers.Unconfined) {
      val mainStats = loadDailyEnergy(mainSiteId, date, cacheMode)
      val exportStats = when (exportSiteId) {
        null -> null
        else -> loadDailyEnergy(exportSiteId, date, cacheMode)
      }
      if (mainStats == null && exportStats == null) {
        if (cacheMode == CACHE_ONLY) {
          return@withContext null
        } else {
          throw IllegalStateException("Failed to load data")
        }
      }

      val exportProduction = exportStats.getDoubles("production")
      val mainProduction = mainStats.getDoubles("production")
      val consumption = mainStats.getDoubles("consumption").map { it.coerceAtLeast(0.0) }
      val charge = mainStats.getDoubles("charge")
      val discharge = mainStats.getDoubles("discharge")
      val mainExport = mainStats.getDoubles("solar_grid")
      val gridBattery = mainStats.getDoubles("grid_battery")
      val gridHome = mainStats.getDoubles("grid_home")
      val import = gridHome.zip(gridBattery) { h, b -> h + b }
      val batteryLevel = mainStats?.getAsJsonArray("soc")?.map { if (it is JsonNull) null else it.asInt } ?: List(96) { 0 }

      val energies = buildList {
        repeat(96) {
          add(
            Energy(
              exportProduction[it].kwh(),
              mainProduction[it].kwh(),
              consumption[it].kwh(),
              charge[it].kwh(),
              discharge[it].kwh(),
              mainExport[it].kwh(),
              import[it].kwh(),
              batteryLevel[it],
            )
          )
        }
      }
      DailyEnergy(date, energies)
    }
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
        val mainStatus = withContext(IO) { mainGateway.getLiveStatus(mainToken) } ?: continue
        val exportStatus = withContext(IO) { exportGateway?.getLiveStatus(exportToken) }
        val exportPv = exportStatus?.pv ?: 0.0
        val combined = LiveStatus(
          pv = mainStatus.pv + exportPv,
          storage = mainStatus.storage,
          grid = mainStatus.grid - exportPv,
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
    val response = client.put("$BASE_URL/service/batteryConfig/api/v1/profile/$siteId}") {
      contentType(Application.Json)
      setBody(SetProfileRequest("self-consumption", reserve.toString()))
    }.body<JsonObject>()
    return response["message"]?.jsonPrimitive?.content ?: "Unexpected error"
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

  private suspend fun GatewayConfig.getLiveStatus(token: String?): LiveStatus? {
    return try {
      val response = client.get("$url/ivp/livedata/status") {
        accept(Application.Json)
        header("Authorization", "Bearer $token")
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

      if (load < 0) {
        throw IllegalStateException("Invalid status: $body")
      }
      LiveStatus(pv, storage, grid, load, soc, reserve)
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

  private suspend fun loadDailyEnergy(siteId: String, date: LocalDate, cacheMode: CacheMode): GsonObject? {
    return withContext(IO) {
      try {
        if (cacheMode != NO_CACHE) {
          val cached = cache.read(siteId, date)
          if (cached != null) {
            return@withContext gson.getObject(cached).getStats()
          }
        }

        val response = client.get(DAILY_ENERGY_URL.format(siteId, date.year, date.month.value, date.dayOfMonth))
        val data = response.bodyAsPrettyJson()
        cache.write(siteId, date, data)
        gson.getObject(data).getStats()
      } catch (e: IOException) {
        logger.error("Failed to load data for ${date.toText()}", e)
        null
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
  return when (result.size < 96) {
    true -> result + List(96 - result.size) { 0.0 }
    false -> result
  }
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

private fun JsonObject.getKiloWatts(key: String) =
  getValue(key).jsonObject.getValue("agg_p_mw").jsonPrimitive.double / 1_000_000
