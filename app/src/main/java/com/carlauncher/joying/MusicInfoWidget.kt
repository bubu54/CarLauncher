
package com.carlauncher.joying

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
@Composable
fun MusicInfoWidget(
    media: CarMediaData,
    controller: CarMediaController
) {

    val progress = if (media.duration > 0L) {
        (media.position.toFloat() / media.duration.toFloat())
            .coerceIn(0f, 1f)
    } else {
        0f
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(25.dp)
    ) {

        // CARÁTULA
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(ButtonBackground),
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
                    fontSize = 85.sp
                )
            }
        }

        // INFORMACIÓN MUSICAL
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "NOW PLAYING",
                color = Blue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = media.title.ifBlank {
                    if (media.hasSession) "Sin título" else "Sin reproducción"
                },
                color = White,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = media.artist.ifBlank { "Artista desconocido" },
                color = Secondary,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = media.album.ifBlank { " " },
                color = Secondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(22.dp))

            // BARRA DE PROGRESO INFORMATIVA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(ButtonBackground)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(Blue)
                )
            }

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMediaTime(media.position),
                    color = Secondary,
                    fontSize = 11.sp
                )

                Text(
                    text = formatMediaTime(media.duration),
                    color = Secondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = when {
                    !media.hasAccess -> "PERMISO MULTIMEDIA NECESARIO"
                    !media.hasSession -> "ESPERANDO REPRODUCTOR"
                    media.isPlaying -> "REPRODUCIENDO"
                    else -> "EN PAUSA"
                },
                color = if (media.isPlaying) {
                    ColorMusicGreen
                } else {
                    Secondary
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            if (!media.hasAccess) {
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        controller.openPermissionSettings()
                    }
                ) {
                    Text("CONCEDER PERMISO")
                }
            }
        }
    }
}
*/

private val ColorMusicGreen =
    androidx.compose.ui.graphics.Color(0xFF45D483)

private fun formatMediaTime(milliseconds: Long): String {

    val totalSeconds = (milliseconds / 1000L).coerceAtLeast(0L)

    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%d:%02d".format(minutes, seconds)
}
