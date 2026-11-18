
package com.carlauncher.joying.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.*

@Composable
fun TopBar(
    weatherData: WeatherData,
    weatherController: WeatherController,
    gpsAvailable: Boolean = false,
    satellites: Int = 0,
    satellitesVisible: Int = 0,
    locationGranted: Boolean = false,
    onOpenSettings: () -> Unit = {}
) {

    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = LocalLauncherSettings.current.data
    var connectivity by remember { mutableStateOf(ConnectionType.NONE) }
    var downKbps by remember { mutableIntStateOf(0) }
    var cellularLabel by remember { mutableStateOf("4G") }

    // Permiso para leer el tipo real de red móvil (2G/3G/4G/5G).
    // Sin él se estima por el ancho de banda.
    var phoneStateGranted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> phoneStateGranted = granted }

    // Se pide después del permiso de ubicación para no solapar los diálogos
    LaunchedEffect(locationGranted) {
        if (locationGranted && !phoneStateGranted) {
            phonePermissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }
    }

    val latestDownKbps by rememberUpdatedState(downKbps)

    // Con datos móviles activos, se relee la generación de red cada pocos segundos
    LaunchedEffect(connectivity, phoneStateGranted) {
        if (connectivity == ConnectionType.CELLULAR) {
            while (true) {
                cellularLabel = CellularInfo.generation(context, latestDownKbps)
                delay(3000)
            }
        }
    }

    DisposableEffect(Unit) {
        val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE)
                as android.net.ConnectivityManager

        fun label(caps: android.net.NetworkCapabilities?): ConnectionType = when {
            caps == null -> ConnectionType.NONE
            !caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) -> ConnectionType.NONE
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            else -> ConnectionType.OTHER
        }

        val initialCaps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        connectivity = label(initialCaps)
        downKbps = initialCaps?.linkDownstreamBandwidthKbps ?: 0

        val callback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: android.net.Network,
                networkCapabilities: android.net.NetworkCapabilities
            ) {
                connectivity = label(networkCapabilities)
                downKbps = networkCapabilities.linkDownstreamBandwidthKbps
            }

            override fun onLost(network: android.net.Network) {
                connectivity = ConnectionType.NONE
            }
        }

        try {
            cm.registerDefaultNetworkCallback(callback)
        } catch (_: Exception) {
        }

        onDispose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (_: Exception) {
            }
        }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // GPS: tachado si no hay señal; con señal, número de satélites usados
        Text(
            text = "GPS",
            color = if (gpsAvailable) White else Secondary,
            fontSize = 12.sp,
            textDecoration = if (gpsAvailable) TextDecoration.None else TextDecoration.LineThrough
        )

        if (gpsAvailable) {
            Spacer(Modifier.width(6.dp))

            Text(
                text = "$satellites/${maxOf(satellites, satellitesVisible)}",
                color = Blue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(20.dp))

        val weatherText = if (weatherData.temperature != null) {
            "${weatherEmoji(weatherData.icon)} " +
                    "${temperatureText(weatherData.temperature, settings.fahrenheit)} " +
                    weatherData.city
        } else {
            "METEO --°"
        }

        if (settings.weatherEnabled) {
        Text(
            text = weatherText,
            color = White,
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.clickable {
                weatherController.refresh()
            }
        )
        }

        Spacer(Modifier.weight(1f))

        ConnectivityIcon(type = connectivity, cellularLabel = cellularLabel)

        Spacer(Modifier.width(18.dp))

        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 38.dp)
                .clickable { onOpenSettings() },
            contentAlignment = Alignment.Center
        ) {
            SettingsIcon(
                kind = SettingsIconKind.GEAR,
                color = White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
