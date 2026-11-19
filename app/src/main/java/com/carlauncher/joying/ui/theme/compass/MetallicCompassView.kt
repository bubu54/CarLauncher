package com.carlauncher.joying.ui.theme.compass

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint as NativePaint
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun MetallicCompassView() {
    val context = LocalContext.current

    var heading by remember { mutableFloatStateOf(0f) }
    var altitude by remember { mutableStateOf<Double?>(null) }
    var sensorAvailable by remember { mutableStateOf(false) }
    var gpsHeadingAvailable by remember { mutableStateOf(false) }

    val animatedHeading by animateFloatAsState(
        targetValue = heading,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "compass_heading"
    )

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val rotationSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    }

    // ORIENTACIÓN MAGNÉTICA
    DisposableEffect(rotationSensor) {
        if (rotationSensor == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val rotationMatrix = FloatArray(9)
                    val orientation = FloatArray(3)

                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()

                    if (!gpsHeadingAvailable) {
                        heading = (degrees + 360f) % 360f
                    }
                    sensorAvailable = true
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                        sensorAvailable = false
                    }
                }
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

    // ALTITUD Y RUMBO GPS COMPLEMENTARIO
    val hasLocationPermission = remember {
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (location.hasAltitude()) {
                        altitude = location.altitude
                    }

                    if (
                        location.hasBearing() &&
                        location.hasSpeed() &&
                        location.speed >= 2f &&
                        (!location.hasAccuracy() || location.accuracy <= 30f)
                    ) {
                        heading = location.bearing
                        gpsHeadingAvailable = true
                    } else {
                        gpsHeadingAvailable = false
                    }
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
            } catch (_: SecurityException) {
                gpsHeadingAvailable = false
            }

            onDispose {
                locationManager.removeUpdates(listener)
            }
        }
    }

    val cardinal = when {
        heading >= 337.5f || heading < 22.5f -> "N"
        heading < 67.5f -> "NE"
        heading < 112.5f -> "E"
        heading < 157.5f -> "SE"
        heading < 202.5f -> "S"
        heading < 247.5f -> "SW"
        heading < 292.5f -> "W"
        else -> "NW"
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
            ) {
                drawMetallicCompassEngine(animatedHeading)
            }
        }

        // PANEL DIGITAL COMPACTO INFERIOR
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .background(
                    color = Color(0xFF0A192F).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "%03d° %s".format(heading.roundToInt() % 360, cardinal),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "|",
                color = Color(0xFF36AFFF).copy(alpha = 0.5f),
                fontSize = 14.sp
            )

            Text(
                text = if (altitude != null) "ALT ${altitude!!.roundToInt()} m" else "ALT -- m",
                color = Color(0xFFB0C4DE),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "•",
                color = if (sensorAvailable || gpsHeadingAvailable) Color(0xFF45D483) else Color(0xFF68839D),
                fontSize = 14.sp
            )

            Text(
                text = when {
                    gpsHeadingAvailable -> "GPS"
                    sensorAvailable -> "MAG"
                    else -> "OFF"
                },
                color = if (sensorAvailable || gpsHeadingAvailable) Color(0xFF45D483) else Color(0xFF68839D),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun DrawScope.drawMetallicCompassEngine(heading: Float) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerRadius = size.minDimension * 0.47f
    val bezelWidth = outerRadius * 0.11f
    val innerBezelRadius = outerRadius - bezelWidth
    val dialRadius = innerBezelRadius * 0.96f
    val nativeCanvas = drawContext.canvas.nativeCanvas

    // 1. RESPLANDOR EXTERIOR AZUL ELÉCTRICO
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0x660088FF),
                Color(0x220066CC),
                Color.Transparent
            ),
            center = center,
            radius = outerRadius * 1.07f
        ),
        radius = outerRadius * 1.07f,
        center = center
    )

    // 2. BISEL EXTERIOR DE ACERO CROMADO 3D
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFF5A6B7C),
                Color(0xFFEBF3FA),
                Color(0xFF3A4856),
                Color(0xFFD6E4F0),
                Color(0xFF283440),
                Color(0xFFF0F6FC),
                Color(0xFF5A6B7C)
            ),
            center = center
        ),
        radius = outerRadius,
        center = center
    )
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1E2936), Color(0xFFC8D6E5)),
            start = Offset(center.x - innerBezelRadius, center.y - innerBezelRadius),
            end = Offset(center.x + innerBezelRadius, center.y + innerBezelRadius)
        ),
        radius = innerBezelRadius,
        center = center
    )
// 3. ESFERA INTERIOR AZUL MARINO PROFUNDO
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF0E253D),
                Color(0xFF071526),
                Color(0xFF030A12)
            ),
            center = center,
            radius = dialRadius
        ),
        radius = dialRadius,
        center = center
    )
    val glowRingRadius = dialRadius * 0.44f
    drawCircle(
        color = Color(0xFF1D8FE1).copy(alpha = 0.55f),
        radius = glowRingRadius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )
    drawCircle(
        color = Color(0xFF36AFFF).copy(alpha = 0.2f),
        radius = glowRingRadius * 1.12f,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
// 4. ANILLO GIRATORIO (ESCALA DE GRADOS + ROSA DE LOS VIENTOS + CARDINALES)
    rotate(degrees = -heading, pivot = center) {
        val tickOuter = dialRadius - 4.dp.toPx()
        for (deg in 0 until 360 step 2) {
            val rad = (deg - 90.0) * PI / 180.0
            val isCardinal = deg % 90 == 0
            val isThirty = deg % 30 == 0
            val isTen = deg % 10 == 0
            val inner = when {
                isCardinal -> dialRadius - 17.dp.toPx()
                isThirty -> dialRadius - 15.dp.toPx()
                isTen -> dialRadius - 12.dp.toPx()
                else -> dialRadius - 9.dp.toPx()
            }
            val tickColor = when {
                deg == 0 -> Color(0xFFFF3B3B)     // Norte 0° en rojo
                deg == 180 -> Color(0xFF38BDF8)   // Sur 180° en azul
                isCardinal -> Color.White
                isThirty -> Color.White
                isTen -> Color(0xFFD0E1F9)
                else -> Color(0xFF6B859E)
            }
            drawLine(
                color = tickColor,
                start = Offset(center.x + cos(rad).toFloat() * inner, center.y + sin(rad).toFloat() * inner),
                end = Offset(center.x + cos(rad).toFloat() * tickOuter, center.y + sin(rad).toFloat() * tickOuter),
                strokeWidth = when {
                    isCardinal -> 2.8.dp.toPx()
                    isThirty -> 2.dp.toPx()
                    else -> 1.dp.toPx()
                },
                cap = StrokeCap.Butt
            )
        }
// Números de grados cada 30°
        val degPaint = NativePaint().apply {
            color = Color(0xFFE8F1FA).toArgb()
            textSize = dialRadius * 0.072f
            textAlign = NativePaint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            isAntiAlias = true
        }
        for (deg in 0 until 360 step 30) {
            val rad = (deg - 90.0) * PI / 180.0
            val textRadius = dialRadius - 26.dp.toPx()
            val tx = center.x + cos(rad).toFloat() * textRadius
            val ty = center.y + sin(rad).toFloat() * textRadius + degPaint.textSize * 0.35f
            nativeCanvas.drawText(deg.toString(), tx, ty, degPaint)
        }
// Puntas secundarias de la rosa (NE, SE, SW, NW)
        val diagLength = dialRadius * 0.54f
        val diagWidth = dialRadius * 0.08f
        for (angleDeg in listOf(45f, 135f, 225f, 315f)) {
            drawFacetedStarPoint(
                center = center,
                angleDeg = angleDeg,
                length = diagLength,
                halfWidth = diagWidth,
                lightColor = Color(0xFF8FA1B3),
                darkColor = Color(0xFF3E4C59)
            )
        }
// Puntas principales Este (90°) y Oeste (270°) en blanco/plata
        val mainLength = dialRadius * 0.72f
        val mainWidth = dialRadius * 0.11f
        for (angleDeg in listOf(90f, 270f)) {
            drawFacetedStarPoint(
                center = center,
                angleDeg = angleDeg,
                length = mainLength,
                halfWidth = mainWidth,
                lightColor = Color(0xFFF2F6FA),
                darkColor = Color(0xFF7D8E9F)
            )
        }
// Punta NORTE (0°) de la rosa en ROJO
        drawFacetedStarPoint(
            center = center,
            angleDeg = 0f,
            length = mainLength,
            halfWidth = mainWidth,
            lightColor = Color(0xFFFF3B3B),
            darkColor = Color(0xFFA61111)
        )
// Punta SUR (180°) de la rosa en AZUL
        drawFacetedStarPoint(
            center = center,
            angleDeg = 180f,
            length = mainLength,
            halfWidth = mainWidth,
            lightColor = Color(0xFF38BDF8),
            darkColor = Color(0xFF0369A1)
        )
// Letras Cardinales Grandes (N rojo, S azul, E y W blanco)
        val cardinalPaint = NativePaint().apply {
            textSize = dialRadius * 0.17f
            textAlign = NativePaint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
            setShadowLayer(4f, 0f, 2f, Color(0xDD000000).toArgb())
        }
        val cardinals = listOf(
            Triple("N", 0.0, Color(0xFFFF3B3B)),
            Triple("E", 90.0, Color.White),
            Triple("S", 180.0, Color(0xFF38BDF8)),
            Triple("W", 270.0, Color.White)
        )
        val cardinalRadius = dialRadius - 50.dp.toPx()
        for ((letter, deg, col) in cardinals) {
            val rad = (deg - 90.0) * PI / 180.0
            val cx = center.x + cos(rad).toFloat() * cardinalRadius
            val cy = center.y + sin(rad).toFloat() * cardinalRadius + cardinalPaint.textSize * 0.35f
            cardinalPaint.color = col.toArgb()
            nativeCanvas.drawText(letter, cx, cy, cardinalPaint)
        }
    }
// 5. AGUJA INDICADORA CENTRAL EN VERDE ESMERALDA + ACERO TITANIO
    val needleLength = dialRadius * 0.60f
    val needleHalfWidth = dialRadius * 0.088f
// Sombra profunda debajo de la aguja
    drawFacetedNeedleHalf(
        center = Offset(center.x + 2.5.dp.toPx(), center.y + 5.dp.toPx()),
        isNorth = true,
        length = needleLength,
        halfWidth = needleHalfWidth,
        leftColor = Color(0xAA000000),
        rightColor = Color(0xAA000000)
    )
    drawFacetedNeedleHalf(
        center = Offset(center.x + 2.5.dp.toPx(), center.y + 5.dp.toPx()),
        isNorth = false,
        length = needleLength,
        halfWidth = needleHalfWidth,
        leftColor = Color(0xAA000000),
        rightColor = Color(0xAA000000)
    )
// Punta Superior de Rumbo en Verde Esmeralda Biselado (destaca sobre rojo, azul y blanco)
    drawFacetedNeedleHalf(
        center = center,
        isNorth = true,
        length = needleLength,
        halfWidth = needleHalfWidth,
        leftColor = Color(0xFF34D399),
        rightColor = Color(0xFF059669)
    )
// Punta Inferior en Acero Titanio Cepillado
    drawFacetedNeedleHalf(
        center = center,
        isNorth = false,
        length = needleLength,
        halfWidth = needleHalfWidth,
        leftColor = Color(0xFFCBD5E1),
        rightColor = Color(0xFF475569)
    )
// 6. PIVOTE CENTRAL METÁLICO 3D
    val pivotRadius = dialRadius * 0.115f
    drawCircle(
        color = Color(0xBB000000),
        radius = pivotRadius + 2.5.dp.toPx(),
        center = Offset(center.x, center.y + 2.5.dp.toPx())
    )
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFF334155)),
            start = Offset(center.x - pivotRadius, center.y - pivotRadius),
            end = Offset(center.x + pivotRadius, center.y + pivotRadius)
        ),
        radius = pivotRadius,
        center = center
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF94A3B8), Color(0xFF334155), Color(0xFF0F172A)),
            center = Offset(center.x - pivotRadius * 0.25f, center.y - pivotRadius * 0.25f),
            radius = pivotRadius * 1.1f
        ),
        radius = pivotRadius * 0.72f,
        center = center
    )
}
private fun DrawScope.drawFacetedStarPoint(
    center: Offset,
    angleDeg: Float,
    length: Float,
    halfWidth: Float,
    lightColor: Color,
    darkColor: Color
) {
    rotate(degrees = angleDeg, pivot = center) {
        val tip = Offset(center.x, center.y - length)
        val leftBase = Offset(center.x - halfWidth, center.y)
        val rightBase = Offset(center.x + halfWidth, center.y)
        val leftPath = Path().apply {
            moveTo(center.x, center.y)
            lineTo(leftBase.x, leftBase.y)
            lineTo(tip.x, tip.y)
            close()
        }
        val rightPath = Path().apply {
            moveTo(center.x, center.y)
            lineTo(tip.x, tip.y)
            lineTo(rightBase.x, rightBase.y)
            close()
        }
        drawPath(path = leftPath, color = lightColor)
        drawPath(path = rightPath, color = darkColor)
    }
}
private fun DrawScope.drawFacetedNeedleHalf(
    center: Offset,
    isNorth: Boolean,
    length: Float,
    halfWidth: Float,
    leftColor: Color,
    rightColor: Color
) {
    val tipY = if (isNorth) center.y - length else center.y + length
    val leftBase = Offset(center.x - halfWidth, center.y)
    val rightBase = Offset(center.x + halfWidth, center.y)
    val leftPath = Path().apply {
        moveTo(center.x, center.y)
        lineTo(leftBase.x, leftBase.y)
        lineTo(center.x, tipY)
        close()
    }
    val rightPath = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x, tipY)
        lineTo(rightBase.x, rightBase.y)
        close()
    }
    drawPath(path = leftPath, color = leftColor)
    drawPath(path = rightPath, color = rightColor)
}

/** Miniatura para el popup de selección: la rosa girando lentamente (sin sensores). */
@Composable
fun MetallicCompassPreview() {
    val transition = rememberInfiniteTransition(label = "compass_preview")

    val heading by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "compass_preview_heading"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawMetallicCompassEngine(heading)
    }
}
