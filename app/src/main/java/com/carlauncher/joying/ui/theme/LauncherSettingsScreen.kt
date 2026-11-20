package com.carlauncher.joying.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarNavigationLauncher
import com.carlauncher.joying.InstalledApps
import com.carlauncher.joying.LauncherSettings
import com.carlauncher.joying.LauncherSettingsData
import com.carlauncher.joying.LocalLauncherSettings
import com.carlauncher.joying.SystemActions
import com.carlauncher.joying.ThemeMode
import com.carlauncher.joying.WeatherController
import com.carlauncher.joying.WeatherData
import com.carlauncher.joying.temperatureText
import com.carlauncher.joying.ui.theme.themes.LauncherStyles
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

// Paleta de la pantalla de ajustes (tarjeta clara con teselas azul marino)
private val CardBg = Color(0xFFF7F9FC)
private val CardBorder = Color(0xFFD5DDE8)
private val TileBg = Color(0xFF0F2B4D)
private val TileSub = Color(0xFFBFD4EA)
private val IconBlue = Color(0xFF4FC3F7)
private val TextDark = Color(0xFF0F1E33)
private val TextMuted = Color(0xFF5B6B80)
private val AccentBlue = Color(0xFF1E88E5)
private val OkGreen = Color(0xFF2E9E62)
private val ErrorRed = Color(0xFFD64545)

enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val icon: SettingsIconKind
) {
    APPEARANCE("Apariencia", "Tema oscuro o claro", SettingsIconKind.PALETTE),
    WEATHER("Meteorología", "OpenWeatherMap y ubicación", SettingsIconKind.WEATHER),
    NAVIGATION("Navegación", "Aplicación predeterminada", SettingsIconKind.NAVIGATION),
    MUSIC("Música", "Reproductor predeterminado", SettingsIconKind.MUSIC),
    AUTO("Modo AUTO", "Velocidad de activación", SettingsIconKind.GAUGE),
    SYSTEM("Sistema", "Inicio y versión", SettingsIconKind.MONITOR)
}

@Composable
fun LauncherSettingsScreen(
    weatherController: WeatherController,
    weatherData: WeatherData,
    onClose: () -> Unit
) {
    val launcherSettings = LocalLauncherSettings.current

    // Estado de los ajustes al abrir la pantalla (para "Volver sin guardar")
    val initialSettings = remember { launcherSettings.data }

    var sectionName by rememberSaveable { mutableStateOf<String?>(null) }
    val section = SettingsSection.values().firstOrNull { it.name == sectionName }

    BackHandler {
        if (section != null) sectionName = null else onClose()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            // Bloquea los toques hacia el launcher que hay debajo
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(22.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(22.dp))
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {

            // Cabecera
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (section == null) "AJUSTES" else "AJUSTES · ${section.title.uppercase()}",
                        color = TextDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = section?.subtitle ?: "Configuración general del launcher",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }

                SettingsIcon(
                    kind = SettingsIconKind.GEAR,
                    color = IconBlue,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Contenido
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (section == null) {
                    SettingsGrid(onSelect = { sectionName = it.name })
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        SectionContent(
                            section = section,
                            weatherController = weatherController,
                            weatherData = weatherData
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Pie
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (section != null) {
                    Text(
                        text = "‹  Volver",
                        color = AccentBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { sectionName = null }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    )
                } else {
                    Text(
                        text = "‹  Volver sin guardar",
                        color = TextMuted,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                // Descarta los cambios hechos desde que se abrió Ajustes
                                launcherSettings.update { initialSettings }
                                onClose()
                            }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Guardar y volver",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.size(8.dp))
                    SettingsIcon(
                        kind = SettingsIconKind.CHECK,
                        color = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGrid(onSelect: (SettingsSection) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SettingsSection.values().toList().chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { item ->
                    SettingsTile(
                        section = item,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onClick = { onSelect(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsTile(
    section: SettingsSection,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TileBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Center
    ) {
        SettingsIcon(
            kind = section.icon,
            color = IconBlue,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = section.title,
            color = Color.White,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = section.subtitle,
            color = TileSub,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SectionContent(
    section: SettingsSection,
    weatherController: WeatherController,
    weatherData: WeatherData
) {
    val settings = LocalLauncherSettings.current

    when (section) {
        SettingsSection.APPEARANCE -> AppearanceSection(settings)
        SettingsSection.WEATHER -> WeatherSection(settings, weatherController, weatherData)
        SettingsSection.NAVIGATION -> NavigationSection(settings)
        SettingsSection.MUSIC -> MusicSection(settings)
        SettingsSection.AUTO -> AutoSection(settings)
        SettingsSection.SYSTEM -> SystemSection(settings)
    }
}

// ─────────────────────────── APARIENCIA ───────────────────────────

@Composable
private fun AppearanceSection(settings: LauncherSettings) {
    val s = settings.data

    ChoiceChips(
        title = "Estilo visual",
        options = LauncherStyles.all.map { it.id to it.name },
        selected = s.styleId,
        onSelect = { value -> settings.update { it.copy(styleId = value) } }
    )
    HintText(
        "Textura del fondo y de los botones. Para añadir más estilos, crea uno en " +
                "la carpeta ui/theme/themes."
    )

    RowDivider()

    ChoiceChips(
        title = "Tema",
        options = listOf(
            ThemeMode.DARK to "Oscuro",
            ThemeMode.LIGHT to "Claro",
            ThemeMode.AUTO to "Automático"
        ),
        selected = s.themeMode,
        onSelect = { value -> settings.update { it.copy(themeMode = value) } }
    )
    HintText(
        "Automático: según horario o configuración del sistema. " +
                "Selector conceptual: el launcher se mantiene en tema oscuro " +
                "hasta que se implemente el tema claro."
    )

    RowDivider()

    SettingSlider(
        title = "Intensidad de iluminación nocturna",
        valueText = if (s.nightDimming == 0) "Desactivada" else "${s.nightDimming} %",
        value = s.nightDimming.toFloat(),
        range = 0f..80f,
        steps = 15,
        onChange = { v -> settings.update { it.copy(nightDimming = (v / 5f).roundToInt() * 5) } }
    )
    HintText("Oscurece la pantalla entre las 20:00 y las 07:00.")

    RowDivider()

    SettingRow(
        title = "Fondo de pantalla",
        subtitle = "Opcional · disponible en una próxima versión"
    ) {
        Text("Próximamente", color = TextMuted, fontSize = 13.sp)
    }
}

// ─────────────────────────── METEOROLOGÍA ───────────────────────────

@Composable
private fun WeatherSection(
    settings: LauncherSettings,
    weatherController: WeatherController,
    weatherData: WeatherData
) {
    val s = settings.data
    var apiKey by remember { mutableStateOf(weatherController.apiKey) }

    SettingToggle(
        title = "Mostrar meteorología",
        subtitle = "Temperatura y ciudad en la barra superior",
        checked = s.weatherEnabled,
        onChange = { v -> settings.update { it.copy(weatherEnabled = v) } }
    )

    RowDivider()

    Text("API Key de OpenWeatherMap", color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    OutlinedTextField(
        value = apiKey,
        onValueChange = { apiKey = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        label = { Text("API Key") }
    )

    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = { weatherController.setApiKey(apiKey) },
            enabled = !weatherData.loading
        ) { Text("Guardar clave") }

        Button(
            onClick = { weatherController.testConnection() },
            enabled = !weatherData.loading
        ) { Text("Comprobar conexión") }
    }

    if (weatherData.loading) {
        HintText("Consultando…")
    }

    weatherData.error?.let { report ->
        val ok = report.contains("TLS/HTTP: OK")
        Text(
            text = report,
            color = if (ok) OkGreen else ErrorRed,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    weatherData.temperature?.let { celsius ->
        Text(
            text = "Temperatura actual: ${temperatureText(celsius, s.fahrenheit)}  ${weatherData.city}",
            color = OkGreen,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    RowDivider()

    SettingToggle(
        title = "Ubicación automática mediante GPS",
        subtitle = "Si se desactiva, se usa la última ubicación guardada",
        checked = s.weatherAutoLocation,
        onChange = { v -> settings.update { it.copy(weatherAutoLocation = v) } }
    )

    RowDivider()

    ChoiceChips(
        title = "Unidades",
        options = listOf("C" to "Celsius (°C)", "F" to "Fahrenheit (°F)"),
        selected = if (s.fahrenheit) "F" else "C",
        onSelect = { v -> settings.update { it.copy(fahrenheit = v == "F") } }
    )

    RowDivider()

    ChoiceChips(
        title = "Frecuencia de actualización",
        options = listOf(5, 10, 15, 30, 60).map { it.toString() to "$it min" },
        selected = s.weatherIntervalMin.toString(),
        onSelect = { v -> settings.update { it.copy(weatherIntervalMin = v.toInt()) } }
    )
}

// ─────────────────────────── NAVEGACIÓN ───────────────────────────

@Composable
private fun NavigationSection(settings: LauncherSettings) {
    val s = settings.data
    val context = LocalContext.current
    val apps = remember { InstalledApps.navigationApps(context) }

    ChoiceChips(
        title = "Navegador predeterminado",
        options = listOf("" to "Automático") + apps.map { it.packageName to it.label },
        selected = s.navApp,
        onSelect = { v -> settings.update { it.copy(navApp = v) } }
    )

    if (apps.isEmpty()) {
        HintText("No se ha detectado ninguna app de navegación instalada (Waze, Sygic, Google Maps…).")
    } else {
        HintText("Automático abre la primera app disponible (Waze, Google Maps, Sygic).")
    }

    Button(
        onClick = { CarNavigationLauncher.openPreferred(context, s.navApp) },
        modifier = Modifier.padding(top = 8.dp)
    ) { Text("Probar apertura") }

    RowDivider()

    SettingToggle(
        title = "Priorizar navegación en el panel central",
        subtitle = "En modo AUTO, muestra las indicaciones de ruta mientras navegas",
        checked = s.navPriority,
        onChange = { v -> settings.update { it.copy(navPriority = v) } }
    )

    RowDivider()

    SettingToggle(
        title = "Mostrar mapa en el widget de navegación",
        subtitle = "Mapa de OpenStreetMap centrado en tu posición. Necesita Internet. " +
                "No es el mapa de tu app de navegación: Android no permite incrustarlo.",
        checked = s.navShowMap,
        onChange = { v -> settings.update { it.copy(navShowMap = v) } }
    )
}

// ─────────────────────────── MÚSICA ───────────────────────────

@Composable
private fun MusicSection(settings: LauncherSettings) {
    val s = settings.data
    val context = LocalContext.current
    val apps = remember { InstalledApps.mediaApps(context) }

    SettingToggle(
        title = "Detectar automáticamente el reproductor activo",
        subtitle = "Si se desactiva, solo se muestra el reproductor predeterminado",
        checked = s.musicAutoDetect,
        onChange = { v -> settings.update { it.copy(musicAutoDetect = v) } }
    )

    RowDivider()

    ChoiceChips(
        title = "Reproductor predeterminado (botón MÚSICA)",
        options = listOf("" to "Ninguno") + apps.map { it.packageName to it.label },
        selected = s.musicApp,
        onSelect = { v -> settings.update { it.copy(musicApp = v) } }
    )

    RowDivider()

    SettingToggle(
        title = "Mostrar carátula",
        checked = s.showArtwork,
        onChange = { v -> settings.update { it.copy(showArtwork = v) } }
    )
    SettingToggle(
        title = "Mostrar información de la canción",
        subtitle = "Título, artista y álbum",
        checked = s.showSongInfo,
        onChange = { v -> settings.update { it.copy(showSongInfo = v) } }
    )
    SettingToggle(
        title = "Mantener controles inferiores visibles",
        subtitle = "Barra con anterior, play/pausa y siguiente",
        checked = s.showBottomControls,
        onChange = { v -> settings.update { it.copy(showBottomControls = v) } }
    )
    SettingToggle(
        title = "Aviso al cambiar de canción",
        subtitle = "Muestra portada, título y artista un instante cuando no estás en el widget de música",
        checked = s.songToast,
        onChange = { v -> settings.update { it.copy(songToast = v) } }
    )
}

// ─────────────────────────── MODO AUTO ───────────────────────────

@Composable
private fun AutoSection(settings: LauncherSettings) {
    val s = settings.data
    val stopSpeed = (s.autoMinSpeed * 0.4f).coerceAtLeast(1f).roundToInt()

    SettingToggle(
        title = "Activar modo automático",
        subtitle = "Alterna entre música y velocímetro según la velocidad",
        checked = s.autoEnabled,
        onChange = { v -> settings.update { it.copy(autoEnabled = v) } }
    )

    RowDivider()

    SettingSlider(
        title = "Velocidad mínima para considerar el coche en marcha",
        valueText = "${s.autoMinSpeed} km/h",
        value = s.autoMinSpeed.toFloat(),
        range = 3f..30f,
        steps = 26,
        onChange = { v -> settings.update { it.copy(autoMinSpeed = v.roundToInt()) } }
    )
    HintText("Por encima de ${s.autoMinSpeed} km/h → velocímetro. Por debajo de $stopSpeed km/h → música.")

    RowDivider()

    SettingSlider(
        title = "Tiempo de confirmación antes de cambiar de widget",
        valueText = "${s.autoConfirmSeconds} s",
        value = s.autoConfirmSeconds.toFloat(),
        range = 1f..15f,
        steps = 13,
        onChange = { v -> settings.update { it.copy(autoConfirmSeconds = v.roundToInt()) } }
    )
}

// ─────────────────────────── SISTEMA ───────────────────────────

@Composable
private fun SystemSection(settings: LauncherSettings) {
    val s = settings.data
    val context = LocalContext.current

    // Launcher predeterminado (se vuelve a comprobar al volver de los ajustes de Android)
    var isDefault by remember { mutableStateOf(SystemActions.isDefaultLauncher(context)) }
    var currentLauncher by remember { mutableStateOf(SystemActions.defaultLauncherLabel(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            isDefault = SystemActions.isDefaultLauncher(context)
            currentLauncher = SystemActions.defaultLauncherLabel(context)
            delay(1500)
        }
    }

    Text(
        text = "Launcher predeterminado",
        color = TextDark,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        text = when {
            isDefault -> "Este launcher es el predeterminado"
            currentLauncher != null -> "Launcher actual: $currentLauncher"
            else -> "Este launcher no es el predeterminado"
        },
        color = if (isDefault) OkGreen else TextMuted,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 2.dp)
    )

    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(onClick = { SystemActions.openDefaultLauncherSettings(context) }) {
            Text("Cambiar launcher predeterminado")
        }

        Button(onClick = { SystemActions.openSystemSettings(context) }) {
            Text("Ajustes del sistema")
        }
    }

    HintText(
        "Abre la pantalla de Android para elegir el launcher de inicio. " +
                "Si tu unidad no la ofrece, se abren las apps predeterminadas o los ajustes generales."
    )

    RowDivider()

    SettingToggle(
        title = "Iniciar launcher automáticamente al arrancar Android",
        subtitle = "Si el launcher es la app de inicio, Android ya lo abre al arrancar. " +
                "Esta opción lo lanza además tras el arranque (Android 10+ puede limitarlo).",
        checked = s.startOnBoot,
        onChange = { v -> settings.update { it.copy(startOnBoot = v) } }
    )

    SettingToggle(
        title = "Mantener pantalla encendida",
        checked = s.keepScreenOn,
        onChange = { v -> settings.update { it.copy(keepScreenOn = v) } }
    )

    RowDivider()

    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (_: Exception) {
            "?"
        }
    }

    val metrics = context.resources.displayMetrics

    InfoLine("Versión del launcher", versionName)
    InfoLine("Dispositivo", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
    InfoLine("Android", "${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
    InfoLine("Pantalla", "${metrics.widthPixels} × ${metrics.heightPixels} px · ${metrics.densityDpi} dpi")
    InfoLine("Paquete", context.packageName)
}

// ─────────────────────────── COMPONENTES ───────────────────────────

@Composable
private fun RowDivider() {
    Spacer(Modifier.height(6.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CardBorder)
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        color = TextMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(label, color = TextMuted, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) {
                Text(subtitle, color = TextMuted, fontSize = 12.sp)
            }
        }
        trailing()
    }
}

@Composable
private fun SettingToggle(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    SettingRow(title = title, subtitle = subtitle) {
        Switch(
            checked = checked,
            onCheckedChange = onChange
        )
    }
}

@Composable
private fun SettingSlider(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(valueText, color = AccentBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            steps = steps
        )
    }
}

@Composable
private fun ChoiceChips(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = TextDark, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, label) ->
                FilterChip(
                    selected = value == selected,
                    onClick = { onSelect(value) },
                    label = { Text(label, fontSize = 14.sp) },
                    modifier = Modifier.height(44.dp)
                )
            }
        }
    }
}
