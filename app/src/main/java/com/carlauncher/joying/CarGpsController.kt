package com.carlauncher.joying

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
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
    val longitude: Double? = null
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

    fun setPermission(granted: Boolean) {
        data = data.copy(hasPermission = granted)
    }

    fun start() {
        if (!data.hasPermission || listener != null) return

        val newListener = object : LocationListener {

            override fun onLocationChanged(location: Location) {

                val now = System.currentTimeMillis()

                // Rechazar posiciones antiguas
                if (now - location.time > 10000) return

                // Rechazar posiciones poco precisas
                if (location.hasAccuracy() && location.accuracy > 50f) return

                // Guardar coordenadas GPS para la meteorología
                data = data.copy(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    gpsAvailable = true,
                    lastValidLocationTime = now
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
                        (location.time - previous.time) / 1000f

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
        }

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                listener = newListener

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    newListener,
                    Looper.getMainLooper()
                )
            }
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
            System.currentTimeMillis() - lastTime > 12000L
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