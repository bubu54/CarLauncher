
package com.carlauncher.joying.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.*
import com.carlauncher.joying.ui.WeatherSettingsDialog

@Composable
fun TopBar(
    weatherData: WeatherData,
    weatherController: WeatherController,
    gpsAvailable: Boolean = false
) {

    val context = androidx.compose.ui.platform.LocalContext.current
    var connectivity by remember { mutableStateOf(ConnectionType.NONE) }

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

        connectivity = label(cm.activeNetwork?.let { cm.getNetworkCapabilities(it) })

        val callback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(
                network: android.net.Network,
                networkCapabilities: android.net.NetworkCapabilities
            ) {
                connectivity = label(networkCapabilities)
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


    var showWeatherSettings by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = if (gpsAvailable) "GPS  ●" else "GPS  ○",
            color = if (gpsAvailable) White else Secondary,
            fontSize = 12.sp
        )

        Spacer(Modifier.width(20.dp))

        val weatherText = if (weatherData.temperature != null) {
            "${weatherEmoji(weatherData.icon)} " +
                    "${weatherData.temperature.toInt()}° " +
                    weatherData.city
        } else {
            "METEO --°"
        }

        Text(
            text = weatherText,
            color = White,
            fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.clickable {
                weatherController.refresh()
            }
        )

        Spacer(Modifier.weight(1f))

        ConnectivityIcon(type = connectivity)

        Spacer(Modifier.width(18.dp))

        Text(
            text = "⚙",
            color = White,
            modifier = Modifier.clickable {
                showWeatherSettings = true
            }
        )
    }

    if (showWeatherSettings) {
        WeatherSettingsDialog(
            controller = weatherController,
            weatherData = weatherData,
            onDismiss = {
                showWeatherSettings = false
            }
        )
    }
}
