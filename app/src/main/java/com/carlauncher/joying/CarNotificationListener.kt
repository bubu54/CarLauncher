package com.carlauncher.joying

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object CarNavigationRepository {

    private val _data = MutableStateFlow(CarNavigationData())
    val data = _data.asStateFlow()

    fun update(data: CarNavigationData) {
        _data.value = data
    }

    fun clear() {
        _data.value = CarNavigationData()
    }
}

class CarNotificationListener : NotificationListenerService() {

    private val navigationPackages = setOf(
        "com.google.android.apps.maps",
        "com.waze",
        "com.sygic.aura"
    )

    override fun onListenerConnected() {
        super.onListenerConnected()
        refreshNavigation()
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

        val navigationNotification = notifications
            ?.filter { isNavigationNotification(it) }
            ?.maxByOrNull { it.postTime }

        if (navigationNotification == null) {
            CarNavigationRepository.clear()
            return
        }

        CarNavigationRepository.update(
            parseNavigationNotification(navigationNotification)
        )
    }

    private fun isNavigationNotification(
        sbn: StatusBarNotification
    ): Boolean {

        val notification = sbn.notification

        val isKnownApp =
            sbn.packageName in navigationPackages

        val isNavigationCategory =
            notification.category == Notification.CATEGORY_NAVIGATION

        return isNavigationCategory ||
                (isKnownApp && sbn.isOngoing)
    }

    private fun parseNavigationNotification(
        sbn: StatusBarNotification
    ): CarNavigationData {

        val extras = sbn.notification.extras

        val title = extras
            .getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
            .orEmpty()

        val text = extras
            .getCharSequence(Notification.EXTRA_TEXT)
            ?.toString()
            .orEmpty()

        val bigText = extras
            .getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?.toString()
            .orEmpty()

        val subText = extras
            .getCharSequence(Notification.EXTRA_SUB_TEXT)
            ?.toString()
            .orEmpty()

        // Preferimos el texto ampliado, después el texto normal.
        val instruction = when {
            bigText.isNotBlank() -> bigText
            text.isNotBlank() -> text
            else -> title
        }

        if (instruction.isBlank()) {
            return CarNavigationData()
        }

        val completeText = "$title $instruction $subText"

        val distance = Regex(
            """\b\d+(?:[,.]\d+)?\s?(?:km|m|metros|kilómetros|mi)\b""",
            RegexOption.IGNORE_CASE
        ).find(completeText)?.value.orEmpty()

        val maneuver = when {
            Regex(
                """\b(gira|gire|izquierda|left)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(instruction) -> "LEFT"

            Regex(
                """\b(derecha|right)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(instruction) -> "RIGHT"

            Regex(
                """\b(rotonda|roundabout)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(instruction) -> "ROUNDABOUT"

            Regex(
                """\b(cambio de sentido|retorno|uturn)\b""",
                RegexOption.IGNORE_CASE
            ).containsMatchIn(instruction) -> "UTURN"

            else -> "STRAIGHT"
        }

        return CarNavigationData(
            isNavigating = true,
            instruction = instruction,
            distanceToManeuver = distance,
            maneuver = maneuver,
            destination = if (
                title.isNotBlank() &&
                title != instruction
            ) title else "",
            nextInstruction = "",
            nextDistance = "",
            remainingDistance = "",
            remainingTime = "",
            arrivalTime = ""
        )
    }
}