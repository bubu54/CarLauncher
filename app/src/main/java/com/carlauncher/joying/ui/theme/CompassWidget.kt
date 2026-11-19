package com.carlauncher.joying.ui

import android.content.Context
import android.graphics.Paint as NativePaint
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ArtificialHorizonWidget() {
    val context = LocalContext.current

    var rawPitch by remember { mutableFloatStateOf(0f) }
    var rawRoll by remember { mutableFloatStateOf(0f) }

    // Offsets de calibración para compensar la inclinación propia de la pantalla del coche
    var pitchOffset by remember { mutableFloatStateOf(0f) }
    var rollOffset by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(false) }

    val calibratedPitch = (rawPitch - pitchOffset).coerceIn(-45f, 45f)
    val calibratedRoll = (rawRoll - rollOffset).coerceIn(-60f, 60f)

    // Suavizado fluido para absorber vibraciones del motor y baches
    val animatedPitch by animateFloatAsState(
        targetValue = calibratedPitch,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "pitch_anim"
    )
    val animatedRoll by animateFloatAsState(
        targetValue = calibratedRoll,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "roll_anim"
    )

    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Usamos el acelerómetro (presente en el 99% de radios Android) para calcular gravedad/inclinación
    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    DisposableEffect(accelerometer) {
        if (accelerometer == null) {
            sensorAvailable = false
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val ax = event.values[0]
                    val ay = event.values[1]
                    val az = event.values[2]

                    // Cálculo trigonométrico de inclinación lateral (Roll) y longitudinal (Pitch)
                    // En pantallas apaisadas de coche (landscape):
                    // ax = inclinación adelante/atrás, ay = inclinación izquierda/derecha
                    val rollRad = atan2(ax.toDouble(), sqrt((ay * ay + az * az).toDouble()))
                    val pitchRad = atan2(az.toDouble(), ay.toDouble())

                    rawRoll = Math.toDegrees(rollRad).toFloat()
                    rawPitch = Math.toDegrees(pitchRad).toFloat()
                    sensorAvailable = true
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ESFERA DE HORIZONTE ARTIFICIAL DE AVIACIÓN
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
                drawAviationHorizonEngine(
                    pitchDeg = animatedPitch,
                    rollDeg = animatedRoll
                )
            }
        }

        // PANEL DIGITAL INFERIOR CON BOTÓN DE CALIBRACIÓN (Puesta a 0°)
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .background(
                    color = Color(0xFF0E1318).copy(alpha = 0.88f),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "ROLL ${calibratedRoll.roundToInt()}°",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "|",
                color = Color(0xFF4B5563),
                fontSize = 13.sp
            )

            Text(
                text = "PITCH ${calibratedPitch.roundToInt()}°",
                color = Color(0xFF38BDF8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            // Botón táctil CAL para poner a cero la posición actual del coche
            Box(
                modifier = Modifier
                    .background(
                        color = if (sensorAvailable) Color(0xFF1E3A5F) else Color(0xFF27272A),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable(enabled = sensorAvailable) {
                        pitchOffset = rawPitch
                        rollOffset = rawRoll
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (sensorAvailable) "CAL 0°" else "NO SENSOR",
                    color = if (sensorAvailable) Color(0xFF38BDF8) else Color(0xFF9CA3AF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun DrawScope.drawAviationHorizonEngine(
    pitchDeg: Float,
    rollDeg: Float
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerRadius = size.minDimension * 0.47f
    val bezelRadius = outerRadius * 0.92f
    val dialRadius = bezelRadius * 0.92f
    val nativeCanvas = drawContext.canvas.nativeCanvas

    // 1. CAJA EXTERIOR Y BISEL METÁLICO DE AVIACIÓN (CON TORNILLOS EN LAS ESQUINAS)
    drawCircle(
        color = Color(0xFF0A0C0E),
        radius = outerRadius,
        center = center
    )

    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFF525860),
                Color(0xFFD1D5DB),
                Color(0xFF2F343A),
                Color(0xFF9CA3AF),
                Color(0xFF1F2328),
                Color(0xFFE5E7EB),
                Color(0xFF525860)
            ),
            center = center
        ),
        radius = bezelRadius,
        center = center
    )

    // Aro interior oscuro biselado
    drawCircle(
        color = Color(0xFF111317),
        radius = dialRadius + 2.dp.toPx(),
        center = center
    )

    // 2. ESFERA MÓVIL DEL HORIZONTE (CIELO AZUL + TIERRA MARRÓN + ESCALA DE CABECEO)
    val dialClip = Path().apply {
        addOval(Rect(center = center, radius = dialRadius))
    }

    clipPath(dialClip) {
        // Cada grado de cabeceo (pitch) desplaza el horizonte verticalmente
        val pixelsPerDegree = dialRadius / 35f
        val pitchOffsetY = pitchDeg * pixelsPerDegree

        rotate(degrees = -rollDeg, pivot = center) {
            translate(left = 0f, top = pitchOffsetY) {
                val bigSize = dialRadius * 3f

                // Mitad Superior: CIELO (Degradado azul aviación)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFF38BDF8)),
                        startY = center.y - bigSize,
                        endY = center.y
                    ),
                    topLeft = Offset(center.x - bigSize, center.y - bigSize),
                    size = Size(bigSize * 2f, bigSize)
                )

                // Mitad Inferior: TIERRA (Degradado marrón/ocre clásico de cabina)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF78350F), Color(0xFF451A03), Color(0xFF1C0A00)),
                        startY = center.y,
                        endY = center.y + bigSize
                    ),
                    topLeft = Offset(center.x - bigSize, center.y),
                    size = Size(bigSize * 2f, bigSize)
                )
// Línea blanca brillante del horizonte
                drawLine(
                    color = Color.White,
                    start = Offset(center.x - bigSize, center.y),
                    end = Offset(center.x + bigSize, center.y),
                    strokeWidth = 2.5.dp.toPx()
                )
// Escala de grados de cabeceo (Pitch Ladder: +20, +10, -10, -20)
                val pitchPaint = NativePaint().apply {
                    color = Color.White.toArgb()
                    textSize = dialRadius * 0.065f
                    textAlign = NativePaint.Align.CENTER
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    isAntiAlias = true
                }
                for (deg in listOf(-20, -15, -10, -5, 5, 10, 15, 20)) {
                    val y = center.y - (deg * pixelsPerDegree)
                    val isMajor = deg % 10 == 0
                    val halfLine = if (isMajor) dialRadius * 0.24f else dialRadius * 0.12f
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x - halfLine, y),
                        end = Offset(center.x + halfLine, y),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.3.dp.toPx()
                    )
                    if (isMajor) {
                        val label = kotlin.math.abs(deg).toString()
                        nativeCanvas.drawText(
                            label,
                            center.x - halfLine - 14.dp.toPx(),
                            y + pitchPaint.textSize * 0.35f,
                            pitchPaint
                        )
                        nativeCanvas.drawText(
                            label,
                            center.x + halfLine + 14.dp.toPx(),
                            y + pitchPaint.textSize * 0.35f,
                            pitchPaint
                        )
                    }
                }
            }
// Triángulo móvil superior de alabeo (Bank Pointer blanco)
            val pointerTop = center.y - dialRadius + 4.dp.toPx()
            val pointerPath = Path().apply {
                moveTo(center.x, pointerTop)
                lineTo(center.x - 7.dp.toPx(), pointerTop + 11.dp.toPx())
                lineTo(center.x + 7.dp.toPx(), pointerTop + 11.dp.toPx())
                close()
            }
            drawPath(path = pointerPath, color = Color.White)
        }
// 3. ANILLO FIJO EXTERIOR DE ALABEO (BANK ANGLE SCALE: 0°, 10°, 20°, 30°, 60°)
        val bankAngles = listOf(-60f, -45f, -30f, -20f, -10f, 10f, 20f, 30f, 45f, 60f)
        for (angle in bankAngles) {
            val rad = (angle - 90.0) * PI / 180.0
            val isMajor = angle % 30f == 0f
            val outer = dialRadius - 2.dp.toPx()
            val inner = if (isMajor) dialRadius - 13.dp.toPx() else dialRadius - 8.dp.toPx()
            drawLine(
                color = Color.White,
                start = Offset(center.x + cos(rad).toFloat() * inner, center.y + sin(rad).toFloat() * inner),
                end = Offset(center.x + cos(rad).toFloat() * outer, center.y + sin(rad).toFloat() * outer),
                strokeWidth = if (isMajor) 2.4.dp.toPx() else 1.4.dp.toPx()
            )
        }
// Triángulo fijo superior de referencia 0° en naranja/amarillo aviación
        val zeroTop = center.y - dialRadius + 15.dp.toPx()
        val zeroTriangle = Path().apply {
            moveTo(center.x, zeroTop)
            lineTo(center.x - 6.dp.toPx(), zeroTop - 9.dp.toPx())
            lineTo(center.x + 6.dp.toPx(), zeroTop - 9.dp.toPx())
            close()
        }
        drawPath(path = zeroTriangle, color = Color(0xFFF59E0B))
// 4. SILUETA FIJA DEL AVIÓN EN EL CENTRO (AMARILLO / NARANJA DE ALTA VISIBILIDAD)
        val planeColor = Color(0xFFF59E0B)
        val wingSpan = dialRadius * 0.46f
        val wingInner = dialRadius * 0.14f
        val wingDrop = 8.dp.toPx()
// Sombra de las alas
        drawLine(
            color = Color(0x99000000),
            start = Offset(center.x - wingSpan, center.y + 3.dp.toPx()),
            end = Offset(center.x - wingInner, center.y + 3.dp.toPx()),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0x99000000),
            start = Offset(center.x + wingInner, center.y + 3.dp.toPx()),
            end = Offset(center.x + wingSpan, center.y + 3.dp.toPx()),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
// Ala izquierda con pestaña hacia abajo
        val leftWing = Path().apply {
            moveTo(center.x - wingSpan, center.y)
            lineTo(center.x - wingInner, center.y)
            lineTo(center.x - wingInner, center.y + wingDrop)
        }
        drawPath(
            path = leftWing,
            color = planeColor,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )
// Ala derecha con pestaña hacia abajo
        val rightWing = Path().apply {
            moveTo(center.x + wingSpan, center.y)
            lineTo(center.x + wingInner, center.y)
            lineTo(center.x + wingInner, center.y + wingDrop)
        }
        drawPath(
            path = rightWing,
            color = planeColor,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )
// Punto central del morro del avión
        drawCircle(Color(0x99000000), 4.5.dp.toPx(), Offset(center.x, center.y + 2.dp.toPx()))
        drawCircle(planeColor, 4.dp.toPx(), center)
// 5. SOMBRA INTERIOR DEL BISEL (PROFUNDIDAD DE INSTRUMENTO DE CABINA)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color(0x99000000)
                ),
                center = center,
                radius = dialRadius
            ),
            radius = dialRadius,
            center = center
        )
    }
// 6. TORNILLOS DE INSTRUMENTO AERONÁUTICO EN LAS 4 ESQUINAS DEL BISEL
    val screwDist = outerRadius * 0.88f
    for (angleDeg in listOf(45.0, 135.0, 225.0, 315.0)) {
        val rad = angleDeg * PI / 180.0
        val sx = center.x + cos(rad).toFloat() * screwDist
        val sy = center.y + sin(rad).toFloat() * screwDist
        drawCircle(Color(0xFF1F2937), 4.dp.toPx(), Offset(sx, sy))
        drawCircle(Color(0xFF6B7280), 2.5.dp.toPx(), Offset(sx, sy))
        drawLine(
            color = Color(0xFF111827),
            start = Offset(sx - 2.dp.toPx(), sy - 2.dp.toPx()),
            end = Offset(sx + 2.dp.toPx(), sy + 2.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
    }
}