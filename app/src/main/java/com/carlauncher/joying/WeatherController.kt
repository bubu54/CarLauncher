
package com.carlauncher.joying

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class WeatherData(
    val temperature: Double? = null,
    val description: String = "",
    val icon: String = "",
    val city: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val lastUpdate: Long = 0L
)

class WeatherController(context: Context) {

    private val appContext = context.applicationContext

    private val preferences = appContext.getSharedPreferences(
        "weather_settings",
        Context.MODE_PRIVATE
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var requestJob: Job? = null

    private var latitude: Double? = null
    private var longitude: Double? = null

    private var lastRequestTime = 0L

    private val updateInterval = 10 * 60 * 1000L

    var data by mutableStateOf(WeatherData())
        private set

    val apiKey: String
        get() = preferences.getString("api_key", "") ?: ""

    fun setApiKey(key: String) {

        preferences.edit()
            .putString("api_key", key.trim())
            .apply()

        lastRequestTime = 0L

        if (key.isNotBlank()) {
            refresh()
        } else {
            requestJob?.cancel()
            data = WeatherData()
        }
    }

    /**
     * Solo se consulta con coordenadas válidas y VIGENTES:
     * [fixIsFresh] indica que el GPS tiene un fix reciente (no coordenadas antiguas).
     */
    fun updateLocation(lat: Double?, lon: Double?, fixIsFresh: Boolean = true) {
        if (lat == null || lon == null || !fixIsFresh) return
        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return
        if (lat == 0.0 && lon == 0.0) return

        latitude = lat
        longitude = lon

        val now = System.currentTimeMillis()

        // Sin API key no hay nada que pedir (evita refrescar en cada fix GPS)
        if (apiKey.isNotBlank() && now - lastRequestTime >= updateInterval) {
            refresh()
        }
    }

    fun refresh() {

        val key = apiKey
        val lat = latitude
        val lon = longitude

        if (key.isBlank()) {
            data = data.copy(
                error = "API Key no configurada"
            )
            return
        }

        if (lat == null || lon == null) {
            data = data.copy(
                error = "Esperando señal GPS"
            )
            return
        }

        requestJob?.cancel()

        lastRequestTime = System.currentTimeMillis()

        requestJob = scope.launch {

            data = data.copy(
                loading = true,
                error = null
            )

            try {

                val result = withContext(Dispatchers.IO) {
                    obtenerMeteorologia(lat, lon, key)
                }

                data = result.copy(
                    loading = false,
                    lastUpdate = System.currentTimeMillis()
                )

            } catch (e: Exception) {

                Log.e("WeatherController", "Error consultando meteorología: ${e.javaClass.simpleName}")

                // Reintentar en 1 minuto (p. ej. si aún no hay datos móviles al arrancar)
                lastRequestTime = System.currentTimeMillis() - updateInterval + 60_000L

                // No mostrar una temperatura obsoleta (> 60 min) como si fuera actual
                val stale = System.currentTimeMillis() - data.lastUpdate > 60 * 60 * 1000L
                val safeMessage = (e.message ?: "Error de conexión").replace(key, "***")

                data = (if (stale) WeatherData() else data).copy(
                    loading = false,
                    error = safeMessage
                )
            }
        }
    }

    fun close() {
        scope.cancel()
    }

    private fun obtenerMeteorologia(
        lat: Double,
        lon: Double,
        key: String
    ): WeatherData {

        val urlString =
            "https://api.openweathermap.org/data/2.5/weather" +
                    "?lat=$lat" +
                    "&lon=$lon" +
                    "&appid=$key" +
                    "&units=metric" +
                    "&lang=es"

        val connection = URL(urlString).openConnection() as HttpURLConnection

        try {

            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode

            val response = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }
                    ?: "Error HTTP $responseCode"
            }

            if (responseCode !in 200..299) {
                val message = try {
                    JSONObject(response).optString("message")
                } catch (_: Exception) {
                    response
                }

                throw Exception("OpenWeatherMap: $message")
            }

            val json = JSONObject(response)

            val main = json.getJSONObject("main")
            val weather = json.getJSONArray("weather").getJSONObject(0)

            return WeatherData(
                temperature = main.getDouble("temp"),
                description = weather.optString("description"),
                icon = weather.optString("icon"),
                city = json.optString("name")
            )

        } finally {
            connection.disconnect()
        }
    }


    /**
     * Diagnóstico por fases: red -> DNS -> TCP 443 -> TLS/HTTP.
     * Distingue problemas de red, DNS, socket (EPERM), TLS y servidor.
     */
    fun testConnection() {
        scope.launch {
            data = data.copy(loading = true, error = null)

            val report = withContext(Dispatchers.IO) { runDiagnostics() }

            data = data.copy(loading = false, error = report)
        }
    }

    private fun runDiagnostics(): String {
        val host = "api.openweathermap.org"
        val sb = StringBuilder()

        // 1) Red activa
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager
        val network = cm.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }

        if (network == null || caps == null) {
            return "1) RED: sin red activa"
        }

        val validated = caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        sb.append("1) RED: OK (validada=$validated)\n")

        // 2) DNS
        val address = try {
            network.getByName(host).also { sb.append("2) DNS: OK (${it.hostAddress})\n") }
        } catch (e: Exception) {
            return sb.append("2) DNS: FALLO ${e.javaClass.simpleName}: ${e.message}").toString()
        }

        // 3) Socket TCP 443 (aquí aparece el EPERM si el proceso no tiene permiso de red)
        try {
            network.socketFactory.createSocket().use { socket ->
                socket.connect(java.net.InetSocketAddress(address, 443), 8000)
            }
            sb.append("3) TCP 443: OK\n")
        } catch (e: Exception) {
            return sb.append("3) TCP 443: FALLO ${e.javaClass.simpleName}: ${e.message}").toString()
        }

        // 4) TLS + HTTP
        return try {
            val connection = network.openConnection(
                URL("https://$host/")
            ) as HttpURLConnection
            try {
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                sb.append("4) TLS/HTTP: OK (HTTP ${connection.responseCode})").toString()
            } finally {
                connection.disconnect()
            }
        } catch (e: javax.net.ssl.SSLException) {
            sb.append("4) TLS: FALLO ${e.javaClass.simpleName}: ${e.message}").toString()
        } catch (e: Exception) {
            sb.append("4) HTTP: FALLO ${e.javaClass.simpleName}: ${e.message}").toString()
        }
    }
}

@Composable
fun rememberWeatherController(): WeatherController {

    val context = androidx.compose.ui.platform.LocalContext.current

    val controller = androidx.compose.runtime.remember {
        WeatherController(context)
    }

    androidx.compose.runtime.DisposableEffect(controller) {
        onDispose { controller.close() }
    }

    return controller



}
