
package com.carlauncher.joying.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import android.graphics.Paint as NativePaint
import kotlin.math.roundToInt

@Composable
fun CompassWidget() {

    val context = LocalContext.current
    val activity = context as ComponentActivity

    var heading by remember { mutableFloatStateOf(0f) }
    var altitude by remember { mutableStateOf<Double?>(null) }
    var sensorAvailable by remember { mutableStateOf(false) }
    var gpsHeadingAvailable by remember { mutableStateOf(false) }

    val sensorManager = remember {
        context.getSystemService(
            android.content.Context.SENSOR_SERVICE
        ) as SensorManager
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

                    SensorManager.getRotationMatrixFromVector(
                        rotationMatrix,
                        event.values
                    )

                    SensorManager.getOrientation(
                        rotationMatrix,
                        orientation
                    )

                    val degrees = Math.toDegrees(
                        orientation[0].toDouble()
                    ).toFloat()

                    heading = (degrees + 360f) % 360f
                    sensorAvailable = true
                }

                override fun onAccuracyChanged(
                    sensor: Sensor?,
                    accuracy: Int
                ) {
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
        activity.checkSelfPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                activity.checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    DisposableEffect(hasLocationPermission) {

        if (!hasLocationPermission) {
            onDispose { }
        } else {

            val locationManager =
                context.getSystemService(
                    android.content.Context.LOCATION_SERVICE
                ) as LocationManager

            val listener = object : android.location.LocationListener {

                override fun onLocationChanged(location: Location) {

                    if (location.hasAltitude()) {
                        altitude = location.altitude
                    }

                    if (
                        location.hasBearing() &&
                        location.hasSpeed() &&
                        location.speed >= 2f &&
                        (!location.hasAccuracy() ||
                                location.accuracy <= 30f)
                    ) {
                        heading = location.bearing
                        gpsHeadingAvailable = true
                    } else {
                        gpsHeadingAvailable = false
                    }
                }
            }

            try {
                if (locationManager.isProviderEnabled(
                        LocationManager.GPS_PROVIDER
                    )
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000L,
                        2f,
                        listener,
                        Looper.getMainLooper()
                    )
                }
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

    val navy = Color(0xFF071321)
    val bezel = Color(0xFF263E56)
    val luminous = Color(0xFFD7EDFF)
    val accent = Color(0xFF36AFFF)
    val muted = Color(0xFF68839D)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "COMPASS",
            color = accent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        // ESFERA GIRATORIA
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

                val center = Offset(
                    size.width / 2f,
                    size.height / 2f
                )

                val nativeCanvas = drawContext.canvas.nativeCanvas
                val radius = size.minDimension * 0.46f

                // BISEL EXTERIOR
                drawCircle(
                    color = bezel,
                    radius = radius,
                    center = center
                )

                drawCircle(
                    color = accent,
                    radius = radius - 3.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                drawCircle(
                    color = navy,
                    radius = radius - 8.dp.toPx(),
                    center = center
                )

                drawCircle(
                    color = muted.copy(alpha = 0.5f),
                    radius = radius - 13.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // ANILLO GIRATORIO
                rotate(degrees = -heading, pivot = center) {

                    val tickOuter = radius - 17.dp.toPx()

                    for (i in 0 until 72) {

                        val angle = Math.toRadians(i * 5.0 - 90.0)

                        val major = i % 6 == 0
                        val medium = i % 3 == 0

                        val inner = when {
                            major -> radius - 39.dp.toPx()
                            medium -> radius - 31.dp.toPx()
                            else -> radius - 24.dp.toPx()
                        }

                        val start = Offset(
                            center.x + kotlin.math.cos(angle).toFloat() * inner,
                            center.y + kotlin.math.sin(angle).toFloat() * inner
                        )

                        val end = Offset(
                            center.x + kotlin.math.cos(angle).toFloat() * tickOuter,
                            center.y + kotlin.math.sin(angle).toFloat() * tickOuter
                        )

                        drawLine(
                            color = if (major) luminous else muted,
                            start = start,
                            end = end,
                            strokeWidth = if (major) 2.5.dp.toPx() else 1.dp.toPx()
                        )
                    }

                    // PUNTOS CARDINALES
                    val paint = NativePaint().apply {
                        color = luminous.toArgb()
                        textSize = radius * 0.13f
                        textAlign = NativePaint.Align.CENTER
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }

                    val labels = listOf(
                        "N", "NE", "E", "SE",
                        "S", "SW", "W", "NW"
                    )

                    labels.forEachIndexed { index, label ->

                        val angle = Math.toRadians(
                            index * 45.0 - 90.0
                        )

                        val labelRadius = radius - 57.dp.toPx()

                        val x = center.x +
                                kotlin.math.cos(angle).toFloat() * labelRadius

                        val y = center.y +
                                kotlin.math.sin(angle).toFloat() * labelRadius +
                                paint.textSize * 0.35f

                        paint.color = if (index == 0) {
                            accent.toArgb()
                        } else {
                            luminous.toArgb()
                        }

                        nativeCanvas.drawText(
                            label,
                            x,
                            y,
                            paint
                        )
                    }
                }

                // INDICADOR FIJO SUPERIOR
                val pointer = android.graphics.Path().apply {
                    moveTo(center.x, center.y - radius + 10.dp.toPx())
                    lineTo(
                        center.x - 9.dp.toPx(),
                        center.y - radius + 28.dp.toPx()
                    )
                    lineTo(
                        center.x + 9.dp.toPx(),
                        center.y - radius + 28.dp.toPx()
                    )
                    close()
                }

                drawContext.canvas.nativeCanvas.drawPath(
                    pointer,
                    NativePaint().apply {
                        color = accent.toArgb()
                        style = NativePaint.Style.FILL
                        isAntiAlias = true
                    }
                )

                // CÍRCULO CENTRAL
                drawCircle(
                    color = bezel,
                    radius = radius * 0.34f,
                    center = center
                )

                drawCircle(
                    color = muted,
                    radius = radius * 0.34f,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // INFORMACIÓN CENTRAL
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "%03d°".format(heading.roundToInt() % 360),
                    color = White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light
                )

                Text(
                    text = cardinal,
                    color = Blue,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = if (altitude != null) {
                        "ALT ${altitude!!.roundToInt()} m"
                    } else {
                        "ALT -- m"
                    },
                    color = Secondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = when {
                gpsHeadingAvailable -> "RUMBO GPS"
                sensorAvailable -> "SENSOR MAGNÉTICO"
                else -> "ORIENTACIÓN NO DISPONIBLE"
            },
            color = if (sensorAvailable || gpsHeadingAvailable) {
                Color(0xFF45D483)
            } else {
                Secondary
            },
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}
