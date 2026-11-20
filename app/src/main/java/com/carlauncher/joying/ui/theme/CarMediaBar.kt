package com.carlauncher.joying.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarMediaController
import com.carlauncher.joying.CarMediaData
import com.carlauncher.joying.ui.theme.themes.LocalLauncherStyle
import com.carlauncher.joying.ui.theme.themes.launcherPanel

@Composable
fun CarMediaBar(
    media: CarMediaData,
    controller: CarMediaController
) {
    val style = LocalLauncherStyle.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .launcherPanel(RoundedCornerShape(12.dp))
    ) {

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 9.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Carátula del disco (a la izquierda del todo)
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ButtonBackground),
            contentAlignment = Alignment.Center
        ) {
            if (media.artwork != null) {
                Image(
                    bitmap = media.artwork.asImageBitmap(),
                    contentDescription = "Carátula",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = "♪",
                    color = Blue,
                    fontSize = 24.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Título y artista
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

        // Tiempo transcurrido / duración de la pista
        if (media.hasSession) {
            TrackTime(position = media.position, duration = media.duration)

            Spacer(modifier = Modifier.width(16.dp))
        }

        // Controles
        val contentColor = if (media.hasSession) {
            style.buttonContentColor
        } else {
            style.buttonDisabledContentColor
        }

        MediaBarButton("⏮", contentColor, media.hasSession) {
            controller.previous(media)
        }

        Spacer(modifier = Modifier.width(8.dp))

        MediaBarButton(
            symbol = if (media.isPlaying) "Ⅱ" else "▶",
            contentColor = contentColor,
            enabled = media.hasSession
        ) {
            controller.playPause(media)
        }

        Spacer(modifier = Modifier.width(8.dp))

        MediaBarButton("⏭", contentColor, media.hasSession) {
            controller.next(media)
        }
    }

    // Barra de progreso fina en el borde inferior
    if (media.hasSession && media.duration > 0L) {
        val fraction = (media.position.toFloat() / media.duration.toFloat()).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0x33FFFFFF))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(fraction)
                .height(3.dp)
                .background(Blue)
        )
    }
    }
}

/** "1:23 / 4:05" (o solo "1:23" si la duración es desconocida, como en una emisora). */
@Composable
private fun TrackTime(position: Long, duration: Long) {
    val digits = TextStyle(fontFeatureSettings = "tnum")

    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = formatTrackTime(position),
            color = White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            style = digits
        )

        if (duration > 0L) {
            Text(
                text = " / " + formatTrackTime(duration),
                color = Secondary,
                fontSize = 14.sp,
                style = digits
            )
        }
    }
}

private fun formatTrackTime(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0L) / 1000L).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format(java.util.Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
    }
}

@Composable
private fun MediaBarButton(
    symbol: String,
    contentColor: androidx.compose.ui.graphics.Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val style = LocalLauncherStyle.current

    StyledButton(
        modifier = Modifier
            .width(54.dp)
            .height(44.dp),
        shape = RoundedCornerShape(10.dp),
        enabled = enabled,
        onClick = onClick
    ) {
        Text(
            text = symbol,
            color = contentColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(shadow = style.buttonTextShadow)
        )
    }
}
