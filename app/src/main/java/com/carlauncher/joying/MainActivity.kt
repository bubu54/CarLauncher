
package com.carlauncher.joying

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke


import androidx.compose.foundation.Canvas
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

import android.graphics.Paint as NativePaint

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableFloatStateOf



import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.roundToInt

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.runtime.collectAsState

// COLORES DEL LAUNCHER
val Background = Color(0xFF070C15)
val Panel = Color(0xFF101B2B)
val ButtonBackground = Color(0xFF172940)
val Blue = Color(0xFF269FFF)
val BlueBorder = Color(0xFF174C79)
val White = Color(0xFFEAF3FF)
val Secondary = Color(0xFF829BB6)

class MainActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MainLauncherScreen()
            }
        }

        hideSystemUI()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            hideSystemUI()
        }
    }
}

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

    //API TIEMPO
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

    val context = LocalContext.current

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

@Composable
fun rememberCarMedia(): Pair<CarMediaController, CarMediaData> {

    val context = LocalContext.current

    val controller = remember {
        CarMediaController(context.applicationContext)
    }

    var media by remember {
        mutableStateOf(CarMediaData())
    }

    LaunchedEffect(Unit) {
        while (true) {
            media = controller.readMedia()
            delay(1000)
        }
    }

    return controller to media
}

private fun formatMediaTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000L).coerceAtLeast(0L)
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return "%d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun MusicInfoWidget(
    media: CarMediaData,
    controller: CarMediaController
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Carátula del álbum.
        Box(
            modifier = Modifier
                .size(190.dp)
                .background(
                    ButtonBackground,
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (media.artwork != null) {
                Image(
                    bitmap = media.artwork.asImageBitmap(),
                    contentDescription = "Carátula del álbum",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "♪",
                    color = Blue,
                    fontSize = 90.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(28.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {

            when {
                !media.hasAccess -> {
                    Text(
                        "ACCESO MULTIMEDIA",
                        color = Blue,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Autoriza el acceso a notificaciones para mostrar la información musical.",
                        color = Secondary,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.material3.Button(
                        onClick = {
                            controller.openPermissionSettings()
                        }
                    ) {
                        Text("CONCEDER ACCESO")
                    }
                }

                !media.hasSession -> {
                    Text(
                        "SIN REPRODUCCIÓN",
                        color = White,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "No hay ningún reproductor activo",
                        color = Secondary,
                        fontSize = 16.sp
                    )
                }

                else -> {
                    Text(
                        text = media.title.ifBlank { "TÍTULO DESCONOCIDO" },
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = media.artist.ifBlank { "ARTISTA DESCONOCIDO" },
                        color = Blue,
                        fontSize = 19.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (media.album.isNotBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = media.album,
                            color = Secondary,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Barra de progreso informativa.
                    val progress = if (media.duration > 0L) {
                        (media.position.toFloat() / media.duration)
                            .coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(
                                BlueBorder,
                                RoundedCornerShape(5.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(
                                    Blue,
                                    RoundedCornerShape(5.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatMediaTime(media.position),
                            color = Secondary,
                            fontSize = 14.sp
                        )

                        Text(
                            formatMediaTime(media.duration),
                            color = Secondary,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        if (media.isPlaying) "▶ REPRODUCIENDO"
                        else "Ⅱ EN PAUSA",
                        color = if (media.isPlaying) Blue else Secondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// BARRA SUPERIOR
// --------------------------------------------------

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

// --------------------------------------------------
// BOTONES LATERALES
// --------------------------------------------------

@Composable
fun SideButtons(
    buttons: List<String>,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {

        buttons.forEach { button ->

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(ButtonBackground)
                    .border(
                        width = 1.dp,
                        color = BlueBorder,
                        shape = RoundedCornerShape(11.dp)
                    )
                    .clickable {
                        Toast.makeText(
                            context,
                            "$button: función pendiente",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = button,
                    color = White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// --------------------------------------------------
// PANEL CENTRAL DE WIDGETS
// --------------------------------------------------

@Composable
fun CentralWidgetPanel(
    widget: String,
    onClick: () -> Unit,
    mediaData: CarMediaData,
    mediaController: CarMediaController,
    navigationData: CarNavigationData,
    gpsData: CarGpsData,
    gpsController: CarGpsController,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(Panel)
            .border(
                1.dp,
                BlueBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            when (widget) {

                "INICIO" -> {
                    Text(
                        "CAR LAUNCHER",
                        color = Blue,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "TOCA PARA ELEGIR WIDGET",
                        color = Secondary,
                        fontSize = 12.sp
                    )
                }

                "RELOJ" -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Text(
                            text = "PILOT CLASSIC",
                            color = Blue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        PilotClassicClock()

                        Text(
                            text = "AUTOMATIC  ·  LOCAL TIME",
                            color = Secondary,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                "VELOCÍMETRO" -> GpsSpeedometer(
                    gpsData = gpsData,
                    gpsController = gpsController
                )

                "BRÚJULA" -> {
                    CompassWidget()
                }

                "NAVEGACIÓN" -> {
                    NavigationWidget(
                        navigation = navigationData
                    )
                }

                "MÚSICA" -> {
                    MusicInfoWidget(
                        media = mediaData,
                        controller = mediaController
                    )
                }

                "AUTO" -> AutoWidget(
                    gpsController = gpsController,
                    mediaData = mediaData,
                    mediaController = mediaController
                )
            }
        }
    }
}

// --------------------------------------------------
// BARRA INFERIOR MULTIMEDIA
// --------------------------------------------------

@Composable
fun CarMediaBar(
    media: CarMediaData,
    controller: CarMediaController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Panel,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (media.hasSession) {
                    media.title.ifBlank { "REPRODUCTOR ACTIVO" }
                } else {
                    "SIN REPRODUCCIÓN"
                },
                color = White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = media.artist,
                color = Secondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "⏮",
            color = if (media.hasSession) Blue else Secondary,
            fontSize = 27.sp,
            modifier = Modifier
                .clickable(enabled = media.hasSession) {
                    controller.previous(media)
                }
                .padding(horizontal = 12.dp)
        )

        Text(
            text = if (media.isPlaying) "Ⅱ" else "▶",
            color = if (media.hasSession) Blue else Secondary,
            fontSize = 27.sp,
            modifier = Modifier
                .clickable(enabled = media.hasSession) {
                    controller.playPause(media)
                }
                .padding(horizontal = 12.dp)
        )

        Text(
            text = "⏭",
            color = if (media.hasSession) Blue else Secondary,
            fontSize = 27.sp,
            modifier = Modifier
                .clickable(enabled = media.hasSession) {
                    controller.next(media)
                }
                .padding(horizontal = 12.dp)
        )
    }
}

// --------------------------------------------------
// BOTONES MULTIMEDIA
// --------------------------------------------------

@Composable
fun MediaButton(
    symbol: String,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = symbol,
            color = if (highlighted) Blue else White,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

//
//Crear el selector radial
//

@Composable
fun WidgetSelectorDialog(
    selectedWidget: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {

    Dialog(onDismissRequest = onDismiss) {

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(390.dp),
            color = Panel,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, BlueBorder)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {

                Text(
                    text = "SELECCIONAR WIDGET",
                    color = Blue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                )

                WidgetChoice(
                    name = "RELOJ",
                    description = "Pilot Classic",
                    selected = selectedWidget == "RELOJ",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                    onClick = { onSelect("RELOJ") }
                )

                WidgetChoice(
                    name = "VELOCÍMETRO",
                    description = "GPS",
                    selected = selectedWidget == "VELOCÍMETRO",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp),
                    onClick = { onSelect("VELOCÍMETRO") }
                )

                WidgetChoice(
                    name = "BRÚJULA",
                    description = "Orientación",
                    selected = selectedWidget == "BRÚJULA",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    onClick = { onSelect("BRÚJULA") }
                )

                WidgetChoice(
                    name = "NAVEGACIÓN",
                    description = "Maniobras",
                    selected = selectedWidget == "NAVEGACIÓN",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 35.dp, bottom = 75.dp),
                    onClick = { onSelect("NAVEGACIÓN") }
                )

                WidgetChoice(
                    name = "MÚSICA",
                    description = "Reproductor",
                    selected = selectedWidget == "MÚSICA",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 35.dp, bottom = 75.dp),
                    onClick = { onSelect("MÚSICA") }
                )

                WidgetChoice(
                    name = "AUTO",
                    description = "Automático",
                    selected = selectedWidget == "AUTO",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 3.dp),
                    onClick = { onSelect("AUTO") }
                )
            }
        }
    }
}

@Composable
fun WidgetChoice(
    name: String,
    description: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val buttonColor = if (selected) {
        Color(0xFF204A70)
    } else {
        ButtonBackground
    }

    Box(
        modifier = modifier
            .width(142.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonColor)
            .border(
                width = 1.dp,
                color = if (selected) Blue else BlueBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = name,
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = Secondary,
                fontSize = 9.sp
            )
        }
    }
}


//
//Pintar reloj
//

@Composable
fun PilotClassicClock() {

    var currentTime by remember {
        mutableStateOf(LocalDateTime.now())
    }

    // Actualización del reloj cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    val navy = Color(0xFF071426)
    val dialBlue = Color(0xFF102C49)
    val luminous = Color(0xFFBFE8FF)
    val accent = Color(0xFF36AFFF)
    val muted = Color(0xFF6688A7)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {

        val center = Offset(
            x = size.width / 2f,
            y = size.height / 2f
        )

        val radius = minOf(size.width, size.height) * 0.45f

        // ESFERA
        drawCircle(
            color = navy,
            radius = radius,
            center = center
        )

        drawCircle(
            color = accent.copy(alpha = 0.7f),
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        drawCircle(
            color = muted.copy(alpha = 0.5f),
            radius = radius - 8.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // ÍNDICES Y NUMERACIÓN
        val nativeCanvas = drawContext.canvas.nativeCanvas

        val numberPaint = NativePaint().apply {
            color = luminous.toArgb()
            textSize = radius * 0.14f
            textAlign = NativePaint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                "sans-serif",
                android.graphics.Typeface.BOLD
            )
            isAntiAlias = true
        }

        val trianglePath = android.graphics.Path().apply {
            moveTo(center.x, center.y - radius + 12.dp.toPx())
            lineTo(center.x - 10.dp.toPx(), center.y - radius + 30.dp.toPx())
            lineTo(center.x + 10.dp.toPx(), center.y - radius + 30.dp.toPx())
            close()
        }

        nativeCanvas.drawPath(
            trianglePath,
            NativePaint().apply {
                color = accent.toArgb()
                style = NativePaint.Style.FILL
                isAntiAlias = true
            }
        )

        for (i in 0 until 60) {

            val angle = (i * 6.0 - 90.0) * PI / 180.0

            val isMajor = i % 5 == 0

            val outer = radius - 16.dp.toPx()
            val inner = if (isMajor) {
                radius - 36.dp.toPx()
            } else {
                radius - 24.dp.toPx()
            }

            val start = Offset(
                center.x + cos(angle).toFloat() * inner,
                center.y + sin(angle).toFloat() * inner
            )

            val end = Offset(
                center.x + cos(angle).toFloat() * outer,
                center.y + sin(angle).toFloat() * outer
            )

            drawLine(
                color = if (isMajor) luminous else muted,
                start = start,
                end = end,
                strokeWidth = if (isMajor) 3.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            if (isMajor) {

                val number = if (i == 0) 12 else i / 5

                val numberRadius = radius - 59.dp.toPx()

                val x = center.x +
                        cos(angle).toFloat() * numberRadius

                val y = center.y +
                        sin(angle).toFloat() * numberRadius +
                        numberPaint.textSize * 0.35f

                nativeCanvas.drawText(
                    number.toString(),
                    x,
                    y,
                    numberPaint
                )
            }
        }

        // SUBESFERA INFERIOR
        val subCenter = Offset(
            center.x,
            center.y + radius * 0.55f
        )

        drawCircle(
            color = dialBlue,
            radius = radius * 0.16f,
            center = subCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // AGUJAS
        val hour = currentTime.hour % 12
        val minute = currentTime.minute
        val second = currentTime.second

        val hourAngle = (hour * 30.0 + minute * 0.5 - 90.0) * PI / 180.0
        val minuteAngle = (minute * 6.0 - 90.0) * PI / 180.0
        val secondAngle = (second * 6.0 - 90.0) * PI / 180.0

        fun handEnd(angle: Double, length: Float): Offset {
            return Offset(
                center.x + cos(angle).toFloat() * length,
                center.y + sin(angle).toFloat() * length
            )
        }

        // Aguja horaria
        drawLine(
            color = luminous.copy(alpha = 0.3f),
            start = center,
            end = handEnd(hourAngle, radius * 0.47f),
            strokeWidth = 11.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = luminous,
            start = center,
            end = handEnd(hourAngle, radius * 0.47f),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Aguja minutera
        drawLine(
            color = luminous.copy(alpha = 0.3f),
            start = center,
            end = handEnd(minuteAngle, radius * 0.68f),
            strokeWidth = 9.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = luminous,
            start = center,
            end = handEnd(minuteAngle, radius * 0.68f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Segundero
        drawLine(
            color = accent,
            start = handEnd(secondAngle + PI, radius * 0.18f),
            end = handEnd(secondAngle, radius * 0.78f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Eje central
        drawCircle(
            color = accent,
            radius = 6.dp.toPx(),
            center = center
        )

        drawCircle(
            color = luminous,
            radius = 2.5.dp.toPx(),
            center = center
        )

        // FECHA
        val datePaint = NativePaint().apply {
            color = luminous.toArgb()
            textSize = radius * 0.09f
            textAlign = NativePaint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        nativeCanvas.drawText(
            currentTime.format(DateTimeFormatter.ofPattern("dd")),
            center.x + radius * 0.48f,
            center.y + radius * 0.48f,
            datePaint
        )
    }
}


//
//Velocimetro digital
//

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GpsSpeedometer(
    gpsData: CarGpsData,
    gpsController: CarGpsController
) {
    val context = LocalContext.current

    val sportMode = gpsData.speedKmh >= 120f

    val speedColor = if (sportMode) {
        Color(0xFFFF3B45)
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
            color = if (sportMode) Color(0xFFFF3B45) else Blue,
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
                Color(0xFF45D483)
            } else {
                Secondary
            },
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}


//
//Crear la brujula
//

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

                    // Orientación del dispositivo en su posición natural
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

                    // ALTITUD GPS
                    if (location.hasAltitude()) {
                        altitude = location.altitude
                    }

                    // GPS COURSE: solo si el vehículo se mueve
                    // a una velocidad suficiente.
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

                // ANILLO GIRATORIO: NORTE SEGÚN EL RUMBO
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

fun weatherEmoji(icon: String): String {
    return when (icon.take(2)) {
        "01" -> "☀"
        "02" -> "🌤"
        "03", "04" -> "☁"
        "09", "10" -> "🌧"
        "11" -> "⛈"
        "13" -> "❄"
        "50" -> "🌫"
        else -> "🌡"
    }
}