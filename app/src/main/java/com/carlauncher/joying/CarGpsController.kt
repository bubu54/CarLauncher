package com.carlauncher.joying

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.GnssStatus
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

data class CarGpsData(
    val hasPermission: Boolean = false,
    val gpsAvailable: Boolean = false,
    val speedKmh: Float = 0f,
    val tripKm: Float = 0f,
    val lastValidLocationTime: Long = 0L,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: Double? = null,     // metros sobre el nivel del mar (GPS)
    val bearing: Float? = null,       // rumbo GPS en grados; solo con el coche en marcha
    val satellitesUsed: Int = 0,      // satélites usados en el fix actual
    val satellitesVisible: Int = 0    // satélites a la vista
)

class CarGpsController(context: Context) {

    private val appContext = context.applicationContext

    private val preferences = appContext.getSharedPreferences(
        "car_launcher_data",
        Context.MODE_PRIVATE
    )

    private val locationManager =
        appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    var data by mutableStateOf(
        CarGpsData(
            tripKm = preferences.getFloat("trip_km", 0f)
        )
    )
        private set

    private var lastLocation: Location? = null
    private var listener: LocationListener? = null
    private var gnssCallback: GnssStatus.Callback? = null

    private fun registerGnss() {
        if (gnssCallback != null) return

        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                var used = 0

                for (i in 0 until status.satelliteCount) {
                    if (status.usedInFix(i)) used++
                }

                val visible = status.satelliteCount

                // Evitar recomposiciones si no hay cambios
                if (used != data.satellitesUsed || visible != data.satellitesVisible) {
                    data = data.copy(satellitesUsed = used, satellitesVisible = visible)
                }
            }
        }

        try {
            @Suppress("DEPRECATION")
            locationManager.registerGnssStatusCallback(
                callback,
                android.os.Handler(Looper.getMainLooper())
            )
            gnssCallback = callback
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }

    fun setPermission(granted: Boolean) {
        data = data.copy(hasPermission = granted)
    }

    fun start() {
        if (!data.hasPermission || listener != null) return

        val newListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {

                val now = android.os.SystemClock.elapsedRealtime()

                // Rechazar posiciones antiguas. Se usa el reloj monotónico:
                // en muchas unidades Android de coche la hora del sistema no es fiable.
                val ageMs = (android.os.SystemClock.elapsedRealtimeNanos() -
                        location.elapsedRealtimeNanos) / 1_000_000L
                if (ageMs > 10000L) return

                // Rechazar posiciones poco precisas
                if (location.hasAccuracy() && location.accuracy > 50f) return

                // Guardar coordenadas GPS para la meteorología
                data = data.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    gpsAvailable = true,
                    lastValidLocationTime = now,
                    altitude = if (location.hasAltitude()) location.altitude else data.altitude,
                    // El rumbo GPS solo es fiable en movimiento
                    bearing = if (location.hasBearing() && location.hasSpeed() &&
                        location.speed >= 2f
                    ) {
                        location.bearing
                    } else {
                        null
                    }
                )

                // A partir de aquí necesitamos velocidad válida
                // para actualizar el velocímetro y calcular el TRIP.
                if (!location.hasSpeed()) return

                val speedMs = location.speed

                if (speedMs < 0f || speedMs > 100f) return

                val currentSpeed = speedMs * 3.6f

                data = data.copy(
                    speedKmh = currentSpeed
                )

                // Calcular distancia parcial
                val previous = lastLocation

                if (previous != null) {

                    val distanceMeters = previous.distanceTo(location)

                    val elapsedSeconds =
                        (location.elapsedRealtimeNanos - previous.elapsedRealtimeNanos) / 1_000_000_000f

                    val plausibleDistance =
                        distanceMeters > 0.5f &&
                                distanceMeters < 150f &&
                                elapsedSeconds > 0f &&
                                elapsedSeconds <= 30f

                    // Evitar acumular deriva GPS con el coche parado
                    if (plausibleDistance && currentSpeed > 2f) {

                        val newTrip = data.tripKm + distanceMeters / 1000f

                        data = data.copy(tripKm = newTrip)

                        preferences.edit()
                            .putFloat("trip_km", newTrip)
                            .apply()
                    }
                }

                lastLocation = Location(location)
            }

            // Obligatorios en API 29 (en API 30+ son opcionales)
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
        }

        try {
            // Se registra aunque el GPS aún no esté activo: empezará a recibir
            // posiciones en cuanto el proveedor se habilite (arranque del coche).
            // minDistance = 0 para seguir recibiendo datos con el coche parado.
            listener = newListener

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                newListener,
                Looper.getMainLooper()
            )

            registerGnss()
        } catch (e: IllegalArgumentException) {
            listener = null
            data = data.copy(gpsAvailable = false)
        } catch (e: SecurityException) {
            listener = null
            data = data.copy(gpsAvailable = false)
        }
    }

    fun stop() {
        listener?.let {
            locationManager.removeUpdates(it)
        }
        listener = null

        gnssCallback?.let {
            try {
                locationManager.unregisterGnssStatusCallback(it)
            } catch (_: Exception) {
            }
        }
        gnssCallback = null

        data = data.copy(satellitesUsed = 0, satellitesVisible = 0)
    }

    fun resetTrip() {
        preferences.edit()
            .putFloat("trip_km", 0f)
            .apply()

        data = data.copy(tripKm = 0f)
    }

    fun checkGpsTimeout() {
        val lastTime = data.lastValidLocationTime

        if (lastTime != 0L &&
            android.os.SystemClock.elapsedRealtime() - lastTime > 12000L
        ) {
            data = data.copy(
                gpsAvailable = false,
                speedKmh = 0f
            )
        }
    }
}

@Composable
fun rememberCarGpsController(): CarGpsController {

    val context = LocalContext.current

    val controller = remember {
        CarGpsController(context)
    }

    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        controller.setPermission(granted)
    }

    // Solicitar permiso si todavía no está concedido
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    // Mantener una única conexión GPS compartida
    DisposableEffect(hasPermission) {

        controller.setPermission(hasPermission)

        if (hasPermission) {
            controller.start()
        }

        onDispose {
            controller.stop()
        }
    }

    // Detectar pérdida de señal
    LaunchedEffect(Unit) {
        while (true) {
            controller.checkGpsTimeout()
            delay(1000)
        }
    }

    return controller
}