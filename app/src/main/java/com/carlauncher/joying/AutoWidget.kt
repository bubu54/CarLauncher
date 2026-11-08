package com.carlauncher.joying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AutoWidget(
    gpsController: CarGpsController,
    mediaData: CarMediaData,
    mediaController: CarMediaController
) {
    // false = música, true = velocímetro
    var showSpeedometer by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(gpsController) {

        var stoppedSince = 0L

        while (true) {

            val gps = gpsController.data

            when {

                // GPS SIN SEÑAL.
                !gps.gpsAvailable -> {

                    if (stoppedSince == 0L) {
                        stoppedSince = System.currentTimeMillis()
                    }

                    if (System.currentTimeMillis() - stoppedSince >= 4000L) {
                        showSpeedometer = false
                    }
                }

                // COCHE EN MOVIMIENTO
                gps.speedKmh > 5f -> {
                    showSpeedometer = true
                    stoppedSince = 0L
                }

                // COCHE DETENIDO
                gps.speedKmh < 2f -> {

                    if (stoppedSince == 0L) {
                        stoppedSince = System.currentTimeMillis()
                    }

                    if (System.currentTimeMillis() - stoppedSince >= 4000L) {
                        showSpeedometer = false
                    }
                }

                // ZONA INTERMEDIA
                else -> {
                    stoppedSince = 0L
                }
            }

            delay(500)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        if (showSpeedometer) {
            GpsSpeedometer(
                gpsData = gpsController.data,
                gpsController = gpsController
            )
        } else {
            MusicInfoWidget(
                media = mediaData,
                controller = mediaController
            )
        }

        // AVISO GPS
        if (!gpsController.data.gpsAvailable) {
            Text(
                text = "GPS SIN SEÑAL",
                color = Secondary,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        }
    }
}