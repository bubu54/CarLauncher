
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
    weatherController: WeatherController
) {

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
            text = "GPS  ●",
            color = White,
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

        Text(
            text = "4G",
            color = White,
            fontSize = 12.sp
        )

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
