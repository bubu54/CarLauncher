package com.carlauncher.joying.ui

import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarGpsController
import com.carlauncher.joying.CarGpsData

private val SportRed = Color(0xFFFF3B45)
private val GpsGreen = Color(0xFF45D483)

// Velocidad a partir de la cual se activa el modo SPORT y fondo de escala de la barra
private const val SPORT_SPEED_KMH = 120f
private const val BAR_MAX_KMH = 200f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GpsSpeedometer(
    gpsData: CarGpsData,
    gpsController: CarGpsController
) {
    val context = LocalContext.current

    val sportMode = gpsData.speedKmh >= SPORT_SPEED_KMH

    val speedColor = if (sportMode) SportRed else White
    val accentColor = if (sportMode) SportRed else Blue

    // Tipografía condensada y en negrita: dígitos más altos y deportivos
    val digitsFont = remember {
        FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // CABECERA
        Text(
            text = if (sportMode) "SPORT" else "GPS SPEED",
            color = accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        // VELOCIDAD: ocupa todo el espacio libre y escala los dígitos a él
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // El alto manda; el ancho limita para que "199" nunca desborde
            val speedSize = minOf(maxHeight.value * 0.95f, maxWidth.value * 0.42f)
                .coerceIn(90f, 260f)

            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (gpsData.gpsAvailable) {
                        gpsData.speedKmh.toInt().toString()
                    } else {
                        "--"
                    },
                    color = speedColor,
                    fontFamily = digitsFont,
                    fontSize = speedSize.sp,
                    lineHeight = speedSize.sp,
                    maxLines = 1,
                    // Cifras de ancho fijo (no baila al cambiar) y un leve resplandor
                    style = TextStyle(
                        fontFeatureSettings = "tnum",
                        shadow = Shadow(
                            color = speedColor.copy(alpha = 0.35f),
                            offset = Offset.Zero,
                            blurRadius = 28f
                        )
                    ),
                    modifier = Modifier.alignByBaseline()
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "km/h",
                    color = accentColor,
                    fontSize = (speedSize * 0.15f).sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }

        // BARRA DE VELOCIDAD (azul hasta el umbral SPORT, roja a partir de ahí)
        SpeedBar(
            speedKmh = if (gpsData.gpsAvailable) gpsData.speedKmh else 0f,
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // PIE: TRIP (pulsación larga para reiniciar) y estado del GPS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // TRIP
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x14FFFFFF))
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
                    .padding(horizontal = 28.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TRIP",
                    color = accentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )

                Text(
                    text = String.format(
                        java.util.Locale.getDefault(),
                        "%.1f km",
                        gpsData.tripKm
                    ),
                    color = White,
                    fontFamily = digitsFont,
                    fontSize = 30.sp
                )

                Text(
                    text = "MANTENER PARA REINICIAR",
                    color = Secondary,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }

            // ESTADO GPS
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(if (gpsData.gpsAvailable) GpsGreen else Secondary)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = when {
                        !gpsData.hasPermission -> "PERMISO GPS NECESARIO"
                        gpsData.gpsAvailable -> "GPS CONECTADO"
                        else -> "ESPERANDO SEÑAL GPS"
                    },
                    color = if (gpsData.gpsAvailable) GpsGreen else Secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

/** Barra horizontal de 0 a [BAR_MAX_KMH] km/h con marcas cada 40 km/h. */
@Composable
private fun SpeedBar(
    speedKmh: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {

        val radius = CornerRadius(size.height / 2f)

        // Pista
        drawRoundRect(
            color = Color(0x26FFFFFF),
            cornerRadius = radius
        )

        // Relleno
        val fraction = (speedKmh / BAR_MAX_KMH).coerceIn(0f, 1f)
        val sportStop = SPORT_SPEED_KMH / BAR_MAX_KMH

        if (fraction > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    0f to Blue,
                    sportStop to Blue,
                    (sportStop + 0.02f) to SportRed,
                    1f to SportRed,
                    startX = 0f,
                    endX = size.width
                ),
                size = Size(size.width * fraction, size.height),
                cornerRadius = radius
            )
        }

        // Marcas cada 40 km/h
        var mark = 40f
        while (mark < BAR_MAX_KMH) {
            val x = size.width * (mark / BAR_MAX_KMH)

            drawLine(
                color = Color(0x80000000),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.5.dp.toPx()
            )

            mark += 40f
        }
    }
}
