package com.alonalbert.solar.combiner.enphase

import com.alonalbert.solar.combiner.enphase.model.Energy
import com.alonalbert.solar.combiner.enphase.model.GetTokenRequest
import com.alonalbert.solar.combiner.enphase.model.LiveStatus
import com.alonalbert.solarsim.simulator.DailyEnergy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
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
import java.nio.file.Path
import java.time.LocalDate
import kotlin.LazyThreadSafetyMode.SYNCHRONIZED
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import com.google.gson.JsonObject as GsonObject

private const val LOGIN_URL = "https://enlighten.enphaseenergy.com/login/login.json"
private const val TOKEN_URL = "http://entrez.enphaseenergy.com/tokens"
private const val LIVE_STREAM_URL = "https://enlighten.enphaseenergy.com/pv/aws_sigv4/livestream.json"
private const val DAILY_ENERGY =
  "https://enlighten.enphaseenergy.com/pv/systems/%1\$s/daily_energy?start_date=%2\$d-%3$02d-%4$02d&end_date=%2\$d-%3$02d-%4$02d"
private const val COOKIE = "_enlighten_4_session"

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
) {
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

  suspend fun getDailyEnergy(date: LocalDate): DailyEnergy {
    return withContext(Dispatchers.Unconfined) {
      val mainData = gson.getObject(loadData(mainSiteId, date))
      val exportData = gson.getObject(loadData(exportSiteId, date))

      val outerStats = exportData.getAsJsonArray("stats")[0].asJsonObject
      val outerProduction = outerStats.getDoubles("production")

      val innerStats = mainData.getAsJsonArray("stats")[0].asJsonObject
      val innerProduction = innerStats.getDoubles("production")
      val consumption = innerStats.getDoubles("consumption")
      val charge = innerStats.getDoubles("charge")
      val discharge = innerStats.getDoubles("discharge")
      val innerExport = innerStats.getDoubles("solar_grid")
      val gridBattery = innerStats.getDoubles("grid_battery")
      val gridHome = innerStats.getDoubles("grid_home")
      val import = gridHome.zip(gridBattery) { h, b -> h + b }
      val batteryLevel = innerStats.getAsJsonArray("soc").map { if (it is JsonNull) null else it.asInt }

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
    return LiveStatus(pv, storage, grid, load)
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

  private suspend fun loadData(siteId: String, date: LocalDate): String {
    return withContext(IO) {
      val cached = cache.read(siteId, date)
      if (cached != null) {
        return@withContext cached
      }

      val response = client.get(DAILY_ENERGY.format(siteId, date.year, date.month.value, date.dayOfMonth)) {
        cookie(COOKIE, sessionId())
      }
      val data = response.bodyAsPrettyJson()
      if (date.shouldCache()) {
        cache.write(siteId, date, data)
      }
      return@withContext data
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

private fun GsonObject.getDoubles(key: String) = getAsJsonArray(key).map {
  if (it is JsonNull) 0.0 else it.asDouble
}

private fun LocalDate.shouldCache() = this < LocalDate.now()

private fun Double.kwh() = this / 1000 * 4

private fun createClient(): HttpClient {
  return HttpClient(CIO) {
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
}

private fun JsonObject.getKiloWatts(key: String) =
  getValue(key).jsonObject.getValue("agg_p_mw").jsonPrimitive.double / 1_000_000
