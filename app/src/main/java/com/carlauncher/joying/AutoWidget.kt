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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.ui.GpsSpeedometer
import com.carlauncher.joying.ui.MusicInfoWidget
import com.carlauncher.joying.ui.Secondary
import kotlinx.coroutines.delay

@Composable
fun AutoWidget(
    gpsController: CarGpsController,
    mediaData: CarMediaData,
    mediaController: CarMediaController,
    navigationData: CarNavigationData
) {
    val settings = LocalLauncherSettings.current.data
    val currentSettings by rememberUpdatedState(settings)

    // false = música, true = velocímetro
    var showSpeedometer by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(gpsController) {

        var movingSince = 0L
        var stoppedSince = 0L

        while (true) {

            val s = currentSettings
            val gps = gpsController.data
            val now = android.os.SystemClock.elapsedRealtime()

            val movingSpeed = s.autoMinSpeed.toFloat()
            val stoppedSpeed = (movingSpeed * 0.4f).coerceAtLeast(1f)
            val confirmMs = s.autoConfirmSeconds * 1000L

            when {

                // Sin señal GPS: se mantiene la vista actual (túneles, etc.)
                !gps.gpsAvailable -> {
                    movingSince = 0L
                    stoppedSince = 0L
                }

                // En movimiento: velocímetro tras el tiempo de confirmación
                gps.speedKmh > movingSpeed -> {
                    stoppedSince = 0L

                    if (movingSince == 0L) {
                        movingSince = now
                    }

                    if (now - movingSince >= confirmMs) {
                        showSpeedometer = true
                    }
                }

                // Detenido: música tras el tiempo de confirmación
                gps.speedKmh < stoppedSpeed -> {
                    movingSince = 0L

                    if (stoppedSince == 0L) {
                        stoppedSince = now
                    }

                    if (now - stoppedSince >= confirmMs) {
                        showSpeedometer = false
                    }
                }

                // Zona intermedia (histéresis)
                else -> {
                    movingSince = 0L
                    stoppedSince = 0L
                }
            }

            delay(500)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        when {

            // Modo AUTO desactivado: vista fija de música
            !settings.autoEnabled -> {
                MusicInfoWidget(
                    media = mediaData,
                    controller = mediaController
                )
            }

            // Navegación activa con prioridad
            settings.navPriority && navigationData.isNavigating -> {
                NavigationWidget(
                    navigation = navigationData,
                    gps = gpsController.data
                )
            }

            showSpeedometer -> {
                GpsSpeedometer(
                    gpsData = gpsController.data,
                    gpsController = gpsController
                )
            }

            else -> {
                MusicInfoWidget(
                    media = mediaData,
                    controller = mediaController
                )
            }
        }

        // Aviso GPS
        if (settings.autoEnabled && !gpsController.data.gpsAvailable) {
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
