package com.carlauncher.joying

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/** Widgets que puede mostrar el panel central. */
private val VALID_WIDGETS = setOf(
    "INICIO", "RELOJ", "VELOCÍMETRO", "BRÚJULA", "NAVEGACIÓN", "MÚSICA", "AUTO"
)

object ThemeMode {
    const val DARK = "DARK"
    const val LIGHT = "LIGHT"
    const val AUTO = "AUTO"
}

/** Todos los ajustes del launcher (se guardan en SharedPreferences). */
data class LauncherSettingsData(
    // Apariencia
    val themeMode: String = ThemeMode.DARK,
    val nightDimming: Int = 0,              // 0..80 %
    val lastWidget: String = "INICIO",       // último widget elegido en el panel central
    val compassId: String = "metallic",      // brújula del widget BRÚJULA
    val clockId: String = "speedmaster",    // reloj del widget RELOJ
    val styleId: String = "carbon_aluminum", // estilo visual (carpeta ui/theme/themes)

    // Meteorología
    val weatherEnabled: Boolean = true,
    val weatherAutoLocation: Boolean = true,
    val fahrenheit: Boolean = false,
    val weatherIntervalMin: Int = 10,

    // Navegación
    val navApp: String = "",                // "" = automático
    val navPriority: Boolean = true,

    // Música
    val musicApp: String = "",              // "" = ninguno
    val musicAutoDetect: Boolean = true,
    val showArtwork: Boolean = true,
    val showSongInfo: Boolean = true,
    val showBottomControls: Boolean = true,
    val songToast: Boolean = true,          // aviso al cambiar de canción

    // Modo AUTO
    val autoEnabled: Boolean = true,
    val autoMinSpeed: Int = 5,              // km/h
    val autoConfirmSeconds: Int = 3,

    // Sistema
    val startOnBoot: Boolean = false,
    val keepScreenOn: Boolean = false
)

class LauncherSettings(context: Context) {

    private val appContext = context.applicationContext

    private val prefs = appContext.getSharedPreferences(
        "launcher_settings",
        Context.MODE_PRIVATE
    )

    var data by mutableStateOf(load())
        private set

    init {
        applyBootReceiver(data.startOnBoot)
    }

    /**
     * [sync] = true escribe en disco de forma síncrona (para cambios que no se deben
     * perder aunque el proceso se cierre justo después, como el reloj elegido).
     */
    fun update(
        sync: Boolean = false,
        transform: (LauncherSettingsData) -> LauncherSettingsData
    ) {
        val old = data
        val new = transform(old)

        if (new == old) return

        data = new
        save(new, sync)

        if (new.startOnBoot != old.startOnBoot) {
            applyBootReceiver(new.startOnBoot)
        }
    }

    private fun load(): LauncherSettingsData {
        val d = LauncherSettingsData()

        return LauncherSettingsData(
            themeMode = prefs.getString("theme_mode", d.themeMode) ?: d.themeMode,
            nightDimming = prefs.getInt("night_dimming", d.nightDimming).coerceIn(0, 80),
            lastWidget = prefs.getString("last_widget", d.lastWidget)
                ?.takeIf { it in VALID_WIDGETS } ?: d.lastWidget,
            compassId = prefs.getString("compass_id", d.compassId) ?: d.compassId,
            clockId = prefs.getString("clock_id", d.clockId) ?: d.clockId,
            styleId = prefs.getString("style_id", d.styleId) ?: d.styleId,

            weatherEnabled = prefs.getBoolean("weather_enabled", d.weatherEnabled),
            weatherAutoLocation = prefs.getBoolean("weather_auto_location", d.weatherAutoLocation),
            fahrenheit = prefs.getBoolean("fahrenheit", d.fahrenheit),
            weatherIntervalMin = prefs.getInt("weather_interval_min", d.weatherIntervalMin)
                .coerceIn(1, 180),

            navApp = prefs.getString("nav_app", d.navApp) ?: d.navApp,
            navPriority = prefs.getBoolean("nav_priority", d.navPriority),

            musicApp = prefs.getString("music_app", d.musicApp) ?: d.musicApp,
            musicAutoDetect = prefs.getBoolean("music_auto_detect", d.musicAutoDetect),
            showArtwork = prefs.getBoolean("show_artwork", d.showArtwork),
            showSongInfo = prefs.getBoolean("show_song_info", d.showSongInfo),
            showBottomControls = prefs.getBoolean("show_bottom_controls", d.showBottomControls),
            songToast = prefs.getBoolean("song_toast", d.songToast),

            autoEnabled = prefs.getBoolean("auto_enabled", d.autoEnabled),
            autoMinSpeed = prefs.getInt("auto_min_speed", d.autoMinSpeed).coerceIn(3, 30),
            autoConfirmSeconds = prefs.getInt("auto_confirm_seconds", d.autoConfirmSeconds)
                .coerceIn(1, 15),

            startOnBoot = prefs.getBoolean("start_on_boot", d.startOnBoot),
            keepScreenOn = prefs.getBoolean("keep_screen_on", d.keepScreenOn)
        )
    }

    private fun save(s: LauncherSettingsData, sync: Boolean = false) {
        val editor = prefs.edit()
            .putString("theme_mode", s.themeMode)
            .putInt("night_dimming", s.nightDimming)
            .putString("last_widget", s.lastWidget)
            .putString("compass_id", s.compassId)
            .putString("clock_id", s.clockId)
            .putString("style_id", s.styleId)
            .putBoolean("weather_enabled", s.weatherEnabled)
            .putBoolean("weather_auto_location", s.weatherAutoLocation)
            .putBoolean("fahrenheit", s.fahrenheit)
            .putInt("weather_interval_min", s.weatherIntervalMin)
            .putString("nav_app", s.navApp)
            .putBoolean("nav_priority", s.navPriority)
            .putString("music_app", s.musicApp)
            .putBoolean("music_auto_detect", s.musicAutoDetect)
            .putBoolean("show_artwork", s.showArtwork)
            .putBoolean("show_song_info", s.showSongInfo)
            .putBoolean("show_bottom_controls", s.showBottomControls)
            .putBoolean("song_toast", s.songToast)
            .putBoolean("auto_enabled", s.autoEnabled)
            .putInt("auto_min_speed", s.autoMinSpeed)
            .putInt("auto_confirm_seconds", s.autoConfirmSeconds)
            .putBoolean("start_on_boot", s.startOnBoot)
            .putBoolean("keep_screen_on", s.keepScreenOn)

        if (sync) editor.commit() else editor.apply()
    }

    /** Activa o desactiva el receptor que lanza el launcher tras el arranque. */
    private fun applyBootReceiver(enabled: Boolean) {
        try {
            appContext.packageManager.setComponentEnabledSetting(
                ComponentName(appContext, BootReceiver::class.java),
                if (enabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                },
                PackageManager.DONT_KILL_APP
            )
        } catch (_: Exception) {
        }
    }
}

val LocalLauncherSettings = staticCompositionLocalOf<LauncherSettings> {
    error("LauncherSettings no proporcionado")
}

@Composable
fun rememberLauncherSettings(): LauncherSettings {
    val context = LocalContext.current
    return remember { LauncherSettings(context) }
}

data class InstalledApp(
    val packageName: String,
    val label: String
)

/** Búsqueda de apps instaladas (navegación y multimedia). */
object InstalledApps {

    private val knownNavigation = listOf(
        "com.waze" to "Waze",
        "com.google.android.apps.maps" to "Google Maps",
        "com.sygic.aura" to "Sygic"
    )

    @Suppress("DEPRECATION")
    fun navigationApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val result = linkedMapOf<String, String>()

        try {
            val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=test"))
            pm.queryIntentActivities(geo, 0).forEach { info ->
                val pkg = info.activityInfo.packageName
                if (pkg != context.packageName) {
                    result[pkg] = info.loadLabel(pm).toString()
                }
            }
        } catch (_: Exception) {
        }

        knownNavigation.forEach { (pkg, label) ->
            if (pkg !in result && pm.getLaunchIntentForPackage(pkg) != null) {
                result[pkg] = label
            }
        }

        return result.map { InstalledApp(it.key, it.value) }
            .sortedBy { it.label.lowercase() }
    }

    @Suppress("DEPRECATION")
    fun mediaApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val result = linkedMapOf<String, String>()

        try {
            val intent = Intent("android.media.browse.MediaBrowserService")
            pm.queryIntentServices(intent, 0).forEach { info ->
                val pkg = info.serviceInfo.packageName
                if (pkg != context.packageName) {
                    result[pkg] = info.loadLabel(pm).toString()
                }
            }
        } catch (_: Exception) {
        }

        return result.map { InstalledApp(it.key, it.value) }
            .sortedBy { it.label.lowercase() }
    }
}
