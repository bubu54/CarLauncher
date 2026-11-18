
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

private const val MAX_SAVED_AGE_MS = 60 * 60 * 1000L

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

    private var updateInterval = 10 * 60 * 1000L

    // Ajustes del launcher
    private var autoLocation = true

    init {
        // Última ubicación conocida (se usa si la ubicación automática está desactivada)
        latitude = preferences.getString("lat", null)?.toDoubleOrNull()
        longitude = preferences.getString("lon", null)?.toDoubleOrNull()
    }

    fun setIntervalMinutes(minutes: Int) {
        updateInterval = minutes.coerceIn(1, 180) * 60_000L
    }

    fun setAutoLocation(auto: Boolean) {
        autoLocation = auto
    }

    // Se recupera el último dato guardado (si es reciente) para mostrarlo al arrancar
    var data by mutableStateOf(loadSavedWeather())
        private set

    private var gotFreshFix = false

    private fun loadSavedWeather(): WeatherData {
        val savedAt = preferences.getLong("w_time", 0L)
        val temperature = preferences.getString("w_temp", null)?.toDoubleOrNull()
        val age = System.currentTimeMillis() - savedAt

        if (savedAt == 0L || temperature == null || age !in 0..MAX_SAVED_AGE_MS) {
            return WeatherData()
        }

        return WeatherData(
            temperature = temperature,
            description = preferences.getString("w_desc", "") ?: "",
            icon = preferences.getString("w_icon", "") ?: "",
            city = preferences.getString("w_city", "") ?: "",
            lastUpdate = savedAt
        )
    }

    private fun saveWeather(weather: WeatherData) {
        val temperature = weather.temperature ?: return

        preferences.edit()
            .putString("w_temp", temperature.toString())
            .putString("w_desc", weather.description)
            .putString("w_icon", weather.icon)
            .putString("w_city", weather.city)
            .putLong("w_time", weather.lastUpdate)
            .apply()
    }

    private fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val out = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, out)
        return out[0] / 1000.0
    }

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
     * Actualiza la ubicación y, si toca (según el intervalo configurado), consulta el tiempo.
     *
     * - Con un fix GPS válido y reciente ([fixIsFresh]) se usa esa posición.
     * - Si todavía no hay fix (arranque, interior...) se usa la ÚLTIMA ubicación guardada,
     *   de modo que el tiempo aparece sin esperar al GPS.
     * - Con la ubicación automática desactivada solo se usa la ubicación guardada.
     */
    fun updateLocation(lat: Double?, lon: Double?, fixIsFresh: Boolean = true) {

        if (autoLocation && lat != null && lon != null && fixIsFresh &&
            lat in -90.0..90.0 && lon in -180.0..180.0 &&
            !(lat == 0.0 && lon == 0.0)
        ) {
            val previousLat = latitude
            val previousLon = longitude

            latitude = lat
            longitude = lon

            // Primer fix real de la sesión: si la ubicación guardada estaba lejos
            // (viaje entre sesiones), se vuelve a consultar sin esperar al intervalo.
            if (!gotFreshFix) {
                gotFreshFix = true

                if (previousLat == null || previousLon == null ||
                    distanceKm(previousLat, previousLon, lat, lon) > 3.0
                ) {
                    lastRequestTime = 0L
                }
            }
        }

        // Sin ninguna ubicación (ni GPS ni guardada) no hay nada que consultar
        if (latitude == null || longitude == null) return

        val now = System.currentTimeMillis()

        // Sin API key no hay nada que pedir (evita refrescar continuamente)
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
        preferences.edit()
            .putString("lat", lat.toString())
            .putString("lon", lon.toString())
            .apply()

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

                saveWeather(data)

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

/** Formatea una temperatura en °C o °F según el ajuste del launcher. */
fun temperatureText(celsius: Double, fahrenheit: Boolean): String =
    if (fahrenheit) {
        "${(celsius * 9.0 / 5.0 + 32.0).toInt()}°F"
    } else {
        "${celsius.toInt()}°"
    }
