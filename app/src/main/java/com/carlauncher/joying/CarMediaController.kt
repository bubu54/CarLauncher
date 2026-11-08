
package com.carlauncher.joying

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.app.NotificationManager
import android.provider.Settings
import android.os.SystemClock

data class CarMediaData(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val duration: Long = 0L,
    val position: Long = 0L,
    val isPlaying: Boolean = false,
    val artwork: android.graphics.Bitmap? = null,
    val hasSession: Boolean = false,
    val hasAccess: Boolean = false,
    val controller: MediaController? = null,
    val packageName: String = ""
)

class CarMediaController(private val context: Context) {

    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    private val component =
        ComponentName(context, CarNotificationListener::class.java)

    fun hasPermission(): Boolean {
        return notificationManager
            .isNotificationListenerAccessGranted(component)
    }

    fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun readMedia(): CarMediaData {

        if (!hasPermission()) {
            return CarMediaData(hasAccess = false)
        }

        return try {

            val manager =
                context.getSystemService(Context.MEDIA_SESSION_SERVICE)
                        as MediaSessionManager

            val sessions = manager.getActiveSessions(component)

            if (sessions.isEmpty()) {
                return CarMediaData(hasAccess = true)
            }

            // Priorizar la sesión que realmente está reproduciendo.
            val controller =
                sessions.firstOrNull {
                    it.playbackState?.state == PlaybackState.STATE_PLAYING
                }
                // Si ninguna reproduce, priorizar una sesión con metadatos.
                    ?: sessions.firstOrNull {
                        !it.metadata?.getString(
                            MediaMetadata.METADATA_KEY_TITLE
                        ).isNullOrBlank()
                    }
                    ?: sessions.first()

            val metadata = controller.metadata
            val playback = controller.playbackState

            val duration =
                metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)
                    ?: 0L

            // Estimar la posición entre actualizaciones del reproductor.
            var position = playback?.position ?: 0L

            if (playback?.state == PlaybackState.STATE_PLAYING) {
                val updateTime = playback.lastPositionUpdateTime

                if (updateTime > 0L) {
                    position += SystemClock.elapsedRealtime() - updateTime
                }
            }

            position = position.coerceAtLeast(0L)

            if (duration > 0L) {
                position = position.coerceAtMost(duration)
            }

            // Buscar la carátula en los campos habituales.
            val artwork =
                metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                    ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

            CarMediaData(
                title = metadata?.getString(
                    MediaMetadata.METADATA_KEY_TITLE
                ).orEmpty(),

                artist = metadata?.getString(
                    MediaMetadata.METADATA_KEY_ARTIST
                ) ?: metadata?.getString(
                    MediaMetadata.METADATA_KEY_ALBUM_ARTIST
                ).orEmpty(),

                album = metadata?.getString(
                    MediaMetadata.METADATA_KEY_ALBUM
                ).orEmpty(),

                duration = duration,
                position = position,

                isPlaying = playback?.state ==
                        PlaybackState.STATE_PLAYING,

                artwork = artwork,
                hasSession = true,
                hasAccess = true,
                controller = controller,
                packageName = controller.packageName
            )

        } catch (e: SecurityException) {
            CarMediaData(hasAccess = false)
        } catch (e: Exception) {
            CarMediaData(hasAccess = true)
        }
    }

    fun previous(data: CarMediaData) {
        data.controller?.transportControls?.skipToPrevious()
    }

    fun playPause(data: CarMediaData) {
        val controls = data.controller?.transportControls ?: return

        if (data.isPlaying) {
            controls.pause()
        } else {
            controls.play()
        }
    }

    fun next(data: CarMediaData) {
        data.controller?.transportControls?.skipToNext()
    }
}
