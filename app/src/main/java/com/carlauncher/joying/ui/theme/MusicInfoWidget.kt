
package com.carlauncher.joying.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarMediaData
import com.carlauncher.joying.CarMediaController

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

                    Button(
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
                        text = media.title.ifBlank {
                            "TÍTULO DESCONOCIDO"
                        },
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = media.artist.ifBlank {
                            "ARTISTA DESCONOCIDO"
                        },
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
