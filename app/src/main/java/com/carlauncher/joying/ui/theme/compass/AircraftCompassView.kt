package com.carlauncher.joying.ui.theme.compass

import android.content.Context
import android.graphics.Paint as NativePaint
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarGpsData
import com.carlauncher.joying.ui.StyledButton
import com.carlauncher.joying.ui.theme.themes.LocalLauncherStyle
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Brújula tipo avión: horizonte artificial circular (pitch y roll), cinta de rumbo,
 * altitud y velocidad.
 *
 * Supone la unidad montada en VERTICAL en el salpicadero con la pantalla mirando al
 * conductor. El rumbo, el pitch y el roll salen del sensor de rotación (si existe);
 * con el coche en marcha el rumbo se toma del GPS, que es más fiable.
 * "NIVELAR" guarda la inclinación actual del montaje como punto cero.
 */
@Composable
fun AircraftCompassView(gps: CarGpsData) {

    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("compass_calibration", Context.MODE_PRIVATE)
    }

    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }
    var magHeading by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(false) }

    var pitchOffset by remember { mutableFloatStateOf(prefs.getFloat("pitch0", 0f)) }
    var rollOffset by remember { mutableFloatStateOf(prefs.getFloat("roll0", 0f)) }

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val rotationSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    // ───────── Sensor de rotación: rumbo magnético, pitch y roll ─────────
    DisposableEffect(rotationSensor) {
        if (rotationSensor == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val matrix = FloatArray(9)
            var first = true

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(matrix, event.values)

                    // Eje Z del dispositivo (sale de la pantalla) en coordenadas del mundo:
                    // la pantalla mira hacia atrás, así que el coche avanza hacia -Z.
                    val zEast = matrix[2]
                    val zNorth = matrix[5]
                    val zUp = matrix[8]

                    val newHeading = Math.toDegrees(
                        atan2((-zEast).toDouble(), (-zNorth).toDouble())
                    ).toFloat()

                    val newPitch = Math.toDegrees(
                        asin((-zUp).coerceIn(-1f, 1f).toDouble())
                    ).toFloat()

                    // "Derecha de la pantalla" según la rotación de la pantalla
                    val rightUp = when (displayRotation(context)) {
                        Surface.ROTATION_90 -> matrix[7]
                        Surface.ROTATION_180 -> -matrix[6]
                        Surface.ROTATION_270 -> -matrix[7]
                        else -> matrix[6]
                    }

                    // Positivo = alabeo a la derecha
                    val newRoll = -Math.toDegrees(
                        asin(rightUp.coerceIn(-1f, 1f).toDouble())
                    ).toFloat()

                    if (first) {
                        pitch = newPitch
                        roll = newRoll
                        magHeading = (newHeading + 360f) % 360f
                        first = false
                    } else {
                        pitch += (newPitch - pitch) * 0.2f
                        roll += (newRoll - roll) * 0.2f
                        magHeading = smoothAngle(magHeading, newHeading, 0.15f)
                    }

                    sensorAvailable = true
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                listener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_UI
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    // ───────── Rumbo: GPS en marcha, magnético si no ─────────
    val gpsHeading: Float? = if (gps.gpsAvailable) gps.bearing else null
    val heading: Float? = gpsHeading ?: if (sensorAvailable) magHeading else null

    val source = when {
        gpsHeading != null -> "GPS"
        sensorAvailable -> "MAG"
        else -> "OFF"
    }

    // Rumbo continuo (sin saltos al pasar de 359° a 0°) y suavizado
    var headingContinuous by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(heading) {
        heading?.let { h ->
            val diff = ((h - headingContinuous + 540f) % 360f) - 180f
            headingContinuous += diff
        }
    }

    val animatedHeading by animateFloatAsState(
        targetValue = headingContinuous,
        animationSpec = tween(durationMillis = 600),
        label = "aircraft_heading"
    )

    val shownPitch = if (sensorAvailable) pitch - pitchOffset else 0f
    val shownRoll = if (sensorAvailable) roll - rollOffset else 0f

    val headingText = heading?.let {
        "%03d° %s".format(it.roundToInt() % 360, cardinal(it))
    } ?: "---"

    val altitudeText = if (gps.gpsAvailable) {
        gps.altitude?.let { "${it.roundToInt()} m" } ?: "-- m"
    } else {
        "-- m"
    }

    val speedText = if (gps.gpsAvailable) "${gps.speedKmh.roundToInt()} km/h" else "-- km/h"
    val pitchText = if (sensorAvailable) "%+.1f°".format(shownPitch) else "--"
    val rollText = if (sensorAvailable) "%+.1f°".format(shownRoll) else "--"

    val styleContent = LocalLauncherStyle.current.buttonContentColor

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Horizonte artificial
        Canvas(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
        ) {
            drawAttitudeIndicator(shownPitch, shownRoll)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            // Cinta de rumbo
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                drawHeadingTape(((animatedHeading % 360f) + 360f) % 360f)
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Readout("RUMBO", headingText, Modifier.weight(1.3f))
                Readout("ALTITUD", altitudeText, Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Readout("VELOCIDAD", speedText, Modifier.weight(1.3f))
                Readout("PITCH", pitchText, Modifier.weight(0.7f))
                Readout("ROLL", rollText, Modifier.weight(0.7f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val sourceColor = if (source == "OFF") Color(0xFF68839D) else Color(0xFF45D483)

                Text(
                    text = "• $source",
                    color = sourceColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!sensorAvailable) {
                    Text(
                        text = "SIN SENSOR DE INCLINACIÓN",
                        color = Color(0xFF68839D),
                        fontSize = 10.sp
                    )
                }

                androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))

                StyledButton(
                    modifier = Modifier
                        .width(92.dp)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = sensorAvailable,
                    onClick = {
                        // El montaje actual pasa a ser el "nivel" (pitch 0, roll 0)
                        pitchOffset = pitch
                        rollOffset = roll

                        prefs.edit()
                            .putFloat("pitch0", pitch)
                            .putFloat("roll0", roll)
                            .apply()
                    }
                ) {
                    Text(
                        text = "NIVELAR",
                        color = styleContent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun Readout(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color(0xFF8FA6BF),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Miniatura para el popup: el horizonte balanceándose (sin sensores). */
@Composable
fun AircraftCompassPreview() {
    val transition = rememberInfiniteTransition(label = "aircraft_preview")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aircraft_preview_phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val radians = Math.toRadians(phase.toDouble())

        drawAttitudeIndicator(
            pitch = (7.0 * sin(radians * 2.0)).toFloat(),
            roll = (22.0 * sin(radians)).toFloat()
        )
    }
}

// ─────────────────────────── Utilidades ───────────────────────────

@Suppress("DEPRECATION")
private fun displayRotation(context: Context): Int =
    (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        .defaultDisplay.rotation

private fun smoothAngle(current: Float, target: Float, alpha: Float): Float {
    val diff = ((target - current + 540f) % 360f) - 180f
    return (current + diff * alpha + 360f) % 360f
}

private fun cardinal(heading: Float): String = when {
    heading >= 337.5f || heading < 22.5f -> "N"
    heading < 67.5f -> "NE"
    heading < 112.5f -> "E"
    heading < 157.5f -> "SE"
    heading < 202.5f -> "S"
    heading < 247.5f -> "SO"
    heading < 292.5f -> "O"
    else -> "NO"
}

// ─────────────────────── Horizonte artificial ───────────────────────

private fun DrawScope.drawAttitudeIndicator(pitch: Float, roll: Float) {

    val center = Offset(size.width / 2f, size.height / 2f)
    val outer = size.minDimension / 2f * 0.98f
    val inner = outer * 0.87f           // radio del instrumento
    val pxPerDeg = inner / 32f          // 32° de pitch hasta el borde

    // 1. Caja y bisel metálico
    drawCircle(Color(0x99000000), outer, Offset(center.x, center.y + 3.dp.toPx()))
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFF9AA0A6),
                Color(0xFFE6E9ED),
                Color(0xFF6B7075),
                Color(0xFFD9DDE2),
                Color(0xFF5C6065),
                Color(0xFFE6E9ED),
                Color(0xFF9AA0A6)
            ),
            center = center
        ),
        radius = outer,
        center = center
    )
    drawCircle(Color(0xFF0B0D10), inner + 4.dp.toPx(), center)

    // 2. Horizonte (cielo, tierra y escala de pitch), recortado al círculo
    val clip = Path().apply {
        addOval(Rect(center, inner))
    }

    clipPath(clip) {
        rotate(degrees = -roll, pivot = center) {

            val horizonY = center.y + pitch * pxPerDeg
            val big = inner * 3f

            // Cielo
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B64B3), Color(0xFF63B2F5)),
                    startY = horizonY - inner * 1.6f,
                    endY = horizonY
                ),
                topLeft = Offset(center.x - big, horizonY - big),
                size = Size(big * 2f, big)
            )

            // Tierra
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB5793A), Color(0xFF53351A)),
                    startY = horizonY,
                    endY = horizonY + inner * 1.6f
                ),
                topLeft = Offset(center.x - big, horizonY),
                size = Size(big * 2f, big)
            )

            // Línea de horizonte
            drawLine(
                color = Color.White,
                start = Offset(center.x - big, horizonY),
                end = Offset(center.x + big, horizonY),
                strokeWidth = 2.dp.toPx()
            )

            // Escala de pitch cada 5° (cifras cada 10°)
            val paint = NativePaint().apply {
                color = Color.White.toArgb()
                textSize = inner * 0.10f
                textAlign = NativePaint.Align.CENTER
                typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
                isAntiAlias = true
            }

            for (deg in -30..30 step 5) {
                if (deg == 0) continue

                val y = horizonY - deg * pxPerDeg
                val major = deg % 10 == 0
                val half = inner * (if (major) 0.26f else 0.13f)

                drawLine(
                    color = Color.White,
                    start = Offset(center.x - half, y),
                    end = Offset(center.x + half, y),
                    strokeWidth = (if (major) 2.dp else 1.4.dp).toPx(),
                    cap = StrokeCap.Round
                )

                if (major) {
                    val label = kotlin.math.abs(deg).toString()
                    val baseline = y + paint.textSize * 0.35f

                    drawContext.canvas.nativeCanvas.drawText(label, center.x - half - paint.textSize, baseline, paint)
                    drawContext.canvas.nativeCanvas.drawText(label, center.x + half + paint.textSize, baseline, paint)
                }
            }
        }
    }

    // 3. Escala de alabeo (fija en la caja): 0, ±10, ±20, ±30, ±45, ±60
    for (deg in listOf(-60, -45, -30, -20, -10, 0, 10, 20, 30, 45, 60)) {
        val major = deg == 0 || deg % 30 == 0

        rotate(degrees = deg.toFloat(), pivot = center) {
            drawLine(
                color = Color.White,
                start = Offset(center.x, center.y - inner + 1.dp.toPx()),
                end = Offset(center.x, center.y - inner + (if (major) 14.dp else 8.dp).toPx()),
                strokeWidth = (if (major) 2.6.dp else 1.6.dp).toPx(),
                cap = StrokeCap.Butt
            )
        }
    }

    // Puntero de alabeo (gira con el horizonte)
    rotate(degrees = -roll, pivot = center) {
        val tipY = center.y - inner + 16.dp.toPx()
        val pointer = Path().apply {
            moveTo(center.x, tipY)
            lineTo(center.x - 7.dp.toPx(), tipY + 13.dp.toPx())
            lineTo(center.x + 7.dp.toPx(), tipY + 13.dp.toPx())
            close()
        }
        drawPath(pointer, Color.White)
    }

    // 4. Avión de referencia (fijo)
    val yellow = Color(0xFFFFC21A)
    val stroke = 4.dp.toPx()

    drawLine(yellow, Offset(center.x - inner * 0.62f, center.y), Offset(center.x - inner * 0.20f, center.y), stroke, StrokeCap.Round)
    drawLine(yellow, Offset(center.x - inner * 0.20f, center.y), Offset(center.x - inner * 0.20f, center.y + inner * 0.07f), stroke, StrokeCap.Round)
    drawLine(yellow, Offset(center.x + inner * 0.20f, center.y), Offset(center.x + inner * 0.62f, center.y), stroke, StrokeCap.Round)
    drawLine(yellow, Offset(center.x + inner * 0.20f, center.y), Offset(center.x + inner * 0.20f, center.y + inner * 0.07f), stroke, StrokeCap.Round)
    drawCircle(yellow, 4.5.dp.toPx(), center)

    // 5. Reflejo de cristal y borde interior
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x26FFFFFF), Color(0x00FFFFFF)),
            center = Offset(center.x - inner * 0.30f, center.y - inner * 0.42f),
            radius = inner * 0.9f
        ),
        radius = inner,
        center = center
    )
    drawCircle(Color(0xFF05070A), inner, center, style = Stroke(width = 2.dp.toPx()))
}

// ─────────────────────────── Cinta de rumbo ───────────────────────────

private fun DrawScope.drawHeadingTape(heading: Float) {

    val w = size.width
    val h = size.height
    val spanDeg = 90f                  // grados visibles
    val pxPerDeg = w / spanDeg

    drawRoundRect(
        color = Color(0xFF0B1320),
        cornerRadius = CornerRadius(8.dp.toPx())
    )

    val paint = NativePaint().apply {
        color = Color.White.toArgb()
        textSize = h * 0.30f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        isAntiAlias = true
    }

    clipRect(0f, 0f, w, h) {

        val firstMark = floor((heading - spanDeg / 2f - 5f) / 5f).toInt() * 5

        var mark = firstMark
        while (mark <= heading + spanDeg / 2f + 5f) {

            val x = w / 2f + (mark - heading) * pxPerDeg
            val value = ((mark % 360) + 360) % 360
            val major = value % 10 == 0

            drawLine(
                color = Color(0xFFB0C4DE),
                start = Offset(x, h * 0.18f),
                end = Offset(x, h * (if (major) 0.46f else 0.36f)),
                strokeWidth = (if (major) 2.dp else 1.2.dp).toPx()
            )

            if (value % 30 == 0) {
                val label = when (value) {
                    0 -> "N"
                    90 -> "E"
                    180 -> "S"
                    270 -> "O"
                    else -> "%02d".format(value / 10)
                }

                paint.color = if (value % 90 == 0) Color(0xFFFFC21A).toArgb() else Color.White.toArgb()
                drawContext.canvas.nativeCanvas.drawText(label, x, h * 0.80f, paint)
            }

            mark += 5
        }
    }

    // Puntero central fijo
    val pointer = Path().apply {
        moveTo(w / 2f, h * 0.02f)
        lineTo(w / 2f - 6.dp.toPx(), h * 0.02f + 9.dp.toPx())
        lineTo(w / 2f + 6.dp.toPx(), h * 0.02f + 9.dp.toPx())
        close()
    }
    drawPath(pointer, Color(0xFFFFC21A))
}
