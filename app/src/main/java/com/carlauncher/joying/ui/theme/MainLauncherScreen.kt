
package com.carlauncher.joying.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlauncher.joying.*

@Composable
fun MainLauncherScreen() {

    var selectedWidget by rememberSaveable {
        mutableStateOf("INICIO")
    }

    var showWidgetSelector by rememberSaveable {
        mutableStateOf(false)
    }

    // CONTROL GPS COMPARTIDO
    val gpsController = rememberCarGpsController()
    val gpsData = gpsController.data

    // API TIEMPO
    val weatherController = rememberWeatherController()
    val weatherData = weatherController.data

    LaunchedEffect(gpsData.latitude, gpsData.longitude) {
        weatherController.updateLocation(
            gpsData.latitude,
            gpsData.longitude
        )
    }

    // CONTROL DEL REPRODUCTOR
    val (mediaController, mediaData) = rememberCarMedia()

    val navigationData by
    CarNavigationRepository.data.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {

            TopBar(
                weatherData = weatherData,
                weatherController = weatherController
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                SideButtons(
                    buttons = listOf(
                        "RADIO",
                        "MÚSICA",
                        "TELÉFONO"
                    ),
                    modifier = Modifier.width(108.dp)
                )

                CentralWidgetPanel(
                    widget = selectedWidget,
                    onClick = { showWidgetSelector = true },
                    mediaData = mediaData,
                    mediaController = mediaController,
                    navigationData = navigationData,
                    gpsData = gpsData,
                    gpsController = gpsController,
                    modifier = Modifier.weight(1f)
                )

                SideButtons(
                    buttons = listOf(
                        "NAVEGACIÓN",
                        "ANDROID AUTO",
                        "APLICACIONES"
                    ),
                    modifier = Modifier.width(108.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            CarMediaBar(
                media = mediaData,
                controller = mediaController
            )
        }

        if (showWidgetSelector) {
            WidgetSelectorDialog(
                selectedWidget = selectedWidget,
                onDismiss = {
                    showWidgetSelector = false
                },
                onSelect = { widget ->
                    selectedWidget = widget
                    showWidgetSelector = false
                }
            )
        }
    }
}
