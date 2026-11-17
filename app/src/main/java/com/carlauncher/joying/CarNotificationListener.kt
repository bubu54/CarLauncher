package com.carlauncher.joying

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import android.widget.TextView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CarNavigationRepository {

    private val _data = MutableStateFlow(CarNavigationData())
    val data = _data.asStateFlow()

    private val _listenerConnected = MutableStateFlow(false)

    /** true cuando Android ha enlazado el servicio de notificaciones. */
    val listenerConnected: StateFlow<Boolean> = _listenerConnected.asStateFlow()

    private val _seen = MutableStateFlow("")

    /** Resumen de las notificaciones de apps de navegación vistas (diagnóstico). */
    val seen: StateFlow<String> = _seen.asStateFlow()

    fun update(data: CarNavigationData) {
        _data.value = data
    }

    fun clear() {
        _data.value = CarNavigationData()
    }

    fun setListenerConnected(connected: Boolean) {
        _listenerConnected.value = connected
    }

    fun setSeen(text: String) {
        _seen.value = text
    }
}

// ───────────────────────── Patrones de texto ─────────────────────────

private val DISTANCE = Regex(
    """\d+(?:[.,]\d+)?\s?(?:km|kilómetros|kilometros|metros|mi|ft|yd|m)\b""",
    RegexOption.IGNORE_CASE
)

private val DISTANCE_ONLY = Regex(
    """^\s*(?:${DISTANCE.pattern})\s*$""",
    RegexOption.IGNORE_CASE
)

private val TIME = Regex(
    """(?<![\d:.])\d+\s?h(?:\s?\d+\s?min)?\b|(?<![\d:.])\d+\s?min\b""",
    RegexOption.IGNORE_CASE
)

private val ARRIVAL = Regex(
    """\b\d{1,2}:\d{2}\b(?:\s?(?:am|pm))?""",
    RegexOption.IGNORE_CASE
)

class CarNotificationListener : NotificationListenerService() {

    private val navigationPackages = setOf(
        "com.google.android.apps.maps",
        "com.waze",
        "com.sygic.aura"
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        CarNavigationRepository.setListenerConnected(true)
        refreshNavigation()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        CarNavigationRepository.setListenerConnected(false)
        CarNavigationRepository.clear()

        // Pedir a Android que vuelva a enlazar el servicio
        try {
            NotificationListenerService.requestRebind(
                ComponentName(this, CarNotificationListener::class.java)
            )
        } catch (_: Exception) {
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        refreshNavigation()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        refreshNavigation()
    }

    private fun refreshNavigation() {

        val notifications = try {
            activeNotifications
        } catch (e: Exception) {
            null
        }

        if (notifications == null) {
            CarNavigationRepository.clear()
            return
        }

        val seenLines = mutableListOf<String>()
        val parsed = mutableListOf<Pair<Long, CarNavigationData>>()

        for (sbn in notifications) {

            if (sbn.packageName == packageName) continue

            val notification = sbn.notification
            val isNavCategory = notification.category == Notification.CATEGORY_NAVIGATION
            val isKnownApp = sbn.packageName in navigationPackages

            if (!isNavCategory && !isKnownApp) continue

            val texts = collectTexts(sbn)

            val description =
                "${sbn.packageName} [cat=${notification.category}, ongoing=${sbn.isOngoing}] " +
                        texts.joinToString(" | ")

            Log.d("CarNav", description)
            seenLines += description.take(220)

            // Apps conocidas sin categoría de navegación: solo si es "en curso"
            // y su texto parece de guiado (distancia o tiempo).
            val looksLikeGuidance = texts.any {
                DISTANCE.containsMatchIn(it) || TIME.containsMatchIn(it)
            }

            val valid = isNavCategory || (isKnownApp && sbn.isOngoing && looksLikeGuidance)

            if (valid) {
                parsed += sbn.postTime to parseNavigation(sbn, texts)
            }
        }

        CarNavigationRepository.setSeen(seenLines.joinToString("\n"))

        val best = parsed.maxByOrNull { it.first }?.second

        if (best == null) {
            CarNavigationRepository.clear()
        } else {
            CarNavigationRepository.update(best)
        }
    }

    // ───────────────────────── Lectura de textos ─────────────────────────

    private fun collectTexts(sbn: StatusBarNotification): List<String> {

        val notification = sbn.notification
        val extras = notification.extras
        val out = LinkedHashSet<String>()

        fun add(value: CharSequence?) {
            val text = value?.toString()?.replace('\n', ' ')?.trim()
            if (!text.isNullOrBlank()) out.add(text)
        }

        add(extras.getCharSequence(Notification.EXTRA_BIG_TEXT))
        add(extras.getCharSequence(Notification.EXTRA_TEXT))
        add(extras.getCharSequence(Notification.EXTRA_TITLE))
        add(extras.getCharSequence(Notification.EXTRA_TITLE_BIG))
        add(extras.getCharSequence(Notification.EXTRA_SUB_TEXT))
        add(extras.getCharSequence(Notification.EXTRA_INFO_TEXT))
        add(extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT))

        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach { add(it) }

        // Muchas apps (Google Maps incluido) usan vistas personalizadas y dejan
        // los extras vacíos: se leen los textos de la propia vista.
        @Suppress("DEPRECATION")
        val views = listOf(
            notification.contentView,
            notification.bigContentView,
            notification.headsUpContentView
        )

        views.forEach { remoteViews ->
            remoteViewTexts(remoteViews).forEach { add(it) }
        }

        return out.toList()
    }

    private fun remoteViewTexts(remoteViews: RemoteViews?): List<String> {

        if (remoteViews == null) return emptyList()

        return try {
            val context: Context = applicationContext
            val view = remoteViews.apply(context, FrameLayout(context))
            val result = mutableListOf<String>()
            gatherTextViews(view, result)
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun gatherTextViews(view: View, out: MutableList<String>) {

        if (view is TextView) {
            val text = view.text?.toString()?.trim()
            if (!text.isNullOrBlank()) out.add(text)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                gatherTextViews(view.getChildAt(i), out)
            }
        }
    }

    // ───────────────────────── Interpretación ─────────────────────────

    private fun parseNavigation(
        sbn: StatusBarNotification,
        texts: List<String>
    ): CarNavigationData {

        val extras = sbn.notification.extras

        fun extra(key: String) =
            extras.getCharSequence(key)?.toString()?.replace('\n', ' ')?.trim().orEmpty()

        val bigText = extra(Notification.EXTRA_BIG_TEXT)
        val text = extra(Notification.EXTRA_TEXT)
        val title = extra(Notification.EXTRA_TITLE)

        val distanceOnly = texts.filter { DISTANCE_ONLY.matches(it) }

        // Resumen del trayecto: "12 min · 5,2 km · 14:35"
        val summary = texts.firstOrNull {
            TIME.containsMatchIn(it) &&
                    (DISTANCE.containsMatchIn(it) || ARRIVAL.containsMatchIn(it))
        } ?: texts.firstOrNull {
            TIME.containsMatchIn(it) && it.length <= 40
        }

        val candidates = texts.filter { it !in distanceOnly && it != summary }

        // Instrucción: texto ampliado, texto normal, título (en ese orden)
        val instruction = listOf(bigText, text, title)
            .firstOrNull { it.isNotBlank() && it in candidates }
            ?: candidates.firstOrNull()
            ?: "Navegación en curso"

        val destination = listOf(title, text, bigText)
            .firstOrNull { it.isNotBlank() && it in candidates && it != instruction }
            .orEmpty()

        val distanceToManeuver = distanceOnly.firstOrNull()
            ?: DISTANCE.find(instruction)?.value
            ?: ""

        val remainingTime = summary?.let { TIME.find(it)?.value }.orEmpty()
        val remainingDistance = summary?.let { DISTANCE.find(it)?.value }.orEmpty()
        val arrival = texts.firstNotNullOfOrNull { ARRIVAL.find(it)?.value }.orEmpty()

        // Orden importa: primero casos especiales, después izquierda/derecha.
        val maneuver = when {
            Regex("""\b(rotonda|glorieta|roundabout)\b""", RegexOption.IGNORE_CASE)
                .containsMatchIn(instruction) -> "ROUNDABOUT"

            Regex("""\b(cambio de sentido|retorno|u-?turn)\b""", RegexOption.IGNORE_CASE)
                .containsMatchIn(instruction) -> "UTURN"

            Regex("""\b(izquierda|left)\b""", RegexOption.IGNORE_CASE)
                .containsMatchIn(instruction) -> "LEFT"

            Regex("""\b(derecha|right)\b""", RegexOption.IGNORE_CASE)
                .containsMatchIn(instruction) -> "RIGHT"

            else -> "STRAIGHT"
        }

        return CarNavigationData(
            isNavigating = true,
            instruction = instruction,
            distanceToManeuver = distanceToManeuver,
            maneuver = maneuver,
            destination = destination,
            remainingDistance = remainingDistance,
            remainingTime = remainingTime,
            arrivalTime = arrival,
            source = appLabel(sbn.packageName)
        )
    }

    private fun appLabel(pkg: String): String {
        return try {
            val pm = applicationContext.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) {
            when (pkg) {
                "com.google.android.apps.maps" -> "Google Maps"
                "com.waze" -> "Waze"
                "com.sygic.aura" -> "Sygic"
                else -> pkg
            }
        }
    }
}
