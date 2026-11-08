
package com.carlauncher.joying.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarGpsData
import com.carlauncher.joying.CarGpsController

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GpsSpeedometer(
    gpsData: CarGpsData,
    gpsController: CarGpsController
) {
    val context = LocalContext.current

    val sportMode = gpsData.speedKmh >= 120f

    val speedColor = if (sportMode) {
        androidx.compose.ui.graphics.Color(0xFFFF3B45)
    } else {
        White
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // CABECERA
        Text(
            text = if (sportMode) "SPORT" else "GPS SPEED",
            color = if (sportMode) {
                androidx.compose.ui.graphics.Color(0xFFFF3B45)
            } else {
                Blue
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // VELOCIDAD
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = if (gpsData.gpsAvailable) {
                    gpsData.speedKmh.toInt().toString()
                } else {
                    "--"
                },
                color = speedColor,
                fontSize = 100.sp,
                fontWeight = FontWeight.Light,
                lineHeight = 105.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "km/h",
                color = Blue,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // TRIP: PULSACIÓN LARGA PARA REINICIAR
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        gpsController.resetTrip()

                        Toast.makeText(
                            context,
                            "TRIP reiniciado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
                .padding(
                    horizontal = 25.dp,
                    vertical = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TRIP",
                color = Blue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Text(
                text = String.format(
                    java.util.Locale.getDefault(),
                    "%.1f km",
                    gpsData.tripKm
                ),
                color = White,
                fontSize = 23.sp
            )

            Text(
                text = "MANTENER PARA REINICIAR",
                color = Secondary,
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ESTADO GPS
        Text(
            text = when {
                !gpsData.hasPermission -> "PERMISO GPS NECESARIO"
                gpsData.gpsAvailable -> "GPS CONECTADO"
                else -> "ESPERANDO SEÑAL GPS"
            },
            color = if (gpsData.gpsAvailable) {
                androidx.compose.ui.graphics.Color(0xFF45D483)
            } else {
                Secondary
            },
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}
