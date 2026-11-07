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
    val controller: MediaController? = null
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

            // Priorizar la sesión que está reproduciendo.
            val controller = sessions.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: sessions.first()

            val metadata = controller.metadata
            val playback = controller.playbackState

            val duration =
                metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L

            val position = playback?.position?.coerceAtLeast(0L) ?: 0L

            val artwork =
                metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)

            CarMediaData(
                title = metadata?.getString(
                    MediaMetadata.METADATA_KEY_TITLE
                ) ?: "",
                artist = metadata?.getString(
                    MediaMetadata.METADATA_KEY_ARTIST
                ) ?: metadata?.getString(
                    MediaMetadata.METADATA_KEY_ALBUM_ARTIST
                ) ?: "",
                album = metadata?.getString(
                    MediaMetadata.METADATA_KEY_ALBUM
                ) ?: "",
                duration = duration,
                position = position,
                isPlaying = playback?.state == PlaybackState.STATE_PLAYING,
                artwork = artwork,
                hasSession = true,
                hasAccess = true,
                controller = controller
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