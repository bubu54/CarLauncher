package com.carlauncher.joying.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.carlauncher.joying.*
import kotlinx.coroutines.delay

@Composable
fun MainLauncherScreen() {

    val settings = rememberLauncherSettings()

    CompositionLocalProvider(LocalLauncherSettings provides settings) {
        LauncherContent(settings)
    }
}

@Composable
private fun LauncherContent(settings: LauncherSettings) {

    val s = settings.data

    var selectedWidget by rememberSaveable {
        mutableStateOf("INICIO")
    }

    var showWidgetSelector by rememberSaveable {
        mutableStateOf(false)
    }

    var showSettings by rememberSaveable {
        mutableStateOf(false)
    }

    var showApps by rememberSaveable {
        mutableStateOf(false)
    }

    // CONTROL GPS COMPARTIDO
    val gpsController = rememberCarGpsController()
    val gpsData = gpsController.data

    // API TIEMPO
    val weatherController = rememberWeatherController()
    val weatherData = weatherController.data

    LaunchedEffect(s.weatherIntervalMin, s.weatherAutoLocation) {
        weatherController.setIntervalMinutes(s.weatherIntervalMin)
        weatherController.setAutoLocation(s.weatherAutoLocation)
    }

    // Comprobación periódica: respeta el intervalo configurado y la vigencia del GPS
    val latestGps = rememberUpdatedState(gpsData)

    LaunchedEffect(s.weatherEnabled) {
        if (s.weatherEnabled) {
            while (true) {
                val g = latestGps.value

                weatherController.updateLocation(
                    g.latitude,
                    g.longitude,
                    fixIsFresh = g.gpsAvailable
                )

                delay(3000)
            }
        }
    }

    // CONTROL DEL REPRODUCTOR
    val (mediaController, mediaData) = rememberCarMedia()

    SideEffect {
        mediaController.preferredPackage =
            if (!s.musicAutoDetect) s.musicApp else ""
    }

    val navigationData by
    CarNavigationRepository.data.collectAsState()

    // MANTENER PANTALLA ENCENDIDA
    val view = LocalView.current

    DisposableEffect(s.keepScreenOn) {
        view.keepScreenOn = s.keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    // ATENUACIÓN NOCTURNA (20:00 - 07:00)
    var hour by remember {
        mutableStateOf(java.time.LocalTime.now().hour)
    }

    LaunchedEffect(Unit) {
        while (true) {
            hour = java.time.LocalTime.now().hour
            delay(60_000)
        }
    }

    val isNight = hour >= 20 || hour < 7
    val dimAlpha = if (isNight) s.nightDimming / 100f * 0.75f else 0f

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {

            TopBar(
                weatherData = weatherData,
                weatherController = weatherController,
                gpsAvailable = gpsData.gpsAvailable,
                onOpenSettings = { showSettings = true }
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
                    modifier = Modifier.width(108.dp),
                    onAppsClick = { showApps = true }
                )
            }

            if (s.showBottomControls) {

                Spacer(modifier = Modifier.height(10.dp))

                CarMediaBar(
                    media = mediaData,
                    controller = mediaController
                )
            }
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

        if (showApps) {
            AppDrawerScreen(onClose = { showApps = false })
        }

        if (showSettings) {
            LauncherSettingsScreen(
                weatherController = weatherController,
                weatherData = weatherData,
                onClose = { showSettings = false }
            )
        }

        // Capa de atenuación nocturna (no intercepta toques)
        if (dimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimAlpha))
            )
        }
    }
}
