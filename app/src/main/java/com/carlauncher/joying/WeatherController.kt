
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

    fun updateLocation(lat: Double?, lon: Double?) {

        if (lat == null || lon == null) return

        latitude = lat
        longitude = lon

        val now = System.currentTimeMillis()

        if (now - lastRequestTime >= updateInterval) {
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

                Log.e("WeatherController", "Error consultando meteorología", e)

                data = data.copy(
                    loading = false,
                    error = e.message ?: "Error de conexión"
                )
            }
        }
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


    fun testConnection() {

        scope.launch {

            data = data.copy(
                loading = true,
                error = null
            )

            try {
                val result = withContext(Dispatchers.IO) {

                    val connection = URL(
                        "https://openweathermap.org"
                    ).openConnection() as HttpURLConnection

                    try {
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        connection.requestMethod = "GET"

                        connection.responseCode
                    } finally {
                        connection.disconnect()
                    }
                }

                data = data.copy(
                    loading = false,
                    error = "Conexión HTTPS correcta. HTTP $result"
                )

            } catch (e: Exception) {

                Log.e("WeatherController", "Prueba HTTPS fallida", e)

                data = data.copy(
                    loading = false,
                    error = "Error HTTPS: ${e.javaClass.simpleName}: ${e.message}"
                )
            }
        }
    }

}

@Composable
fun rememberWeatherController(): WeatherController {

    val context = androidx.compose.ui.platform.LocalContext.current

    return androidx.compose.runtime.remember {
        WeatherController(context)
    }



}
