package com.carlauncher.joying.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarMediaData
import kotlinx.coroutines.delay

private const val FADE_IN_MS = 450
private const val HOLD_MS = 2000L
private const val FADE_OUT_MS = 900

/**
 * Aviso de "canción nueva": cuando cambia la canción y no se está viendo el widget
 * de música, aparece con un fundido, se mantiene un par de segundos y se desvanece.
 * No intercepta toques.
 */
@Composable
fun NowPlayingToast(
    media: CarMediaData,
    enabled: Boolean
) {
    val fade = remember { Animatable(0f) }
    var lastKey by remember { mutableStateOf<String?>(null) }

    val key = if (media.title.isBlank()) {
        null
    } else {
        media.packageName + "|" + media.title + "|" + media.artist
    }

    LaunchedEffect(key, enabled) {

        // Deshabilitado (p. ej. widget de música a la vista): ocultar y no avisar luego
        if (!enabled) {
            fade.snapTo(0f)
            if (key != null) lastKey = key
            return@LaunchedEffect
        }

        val previous = lastKey
        if (key != null) lastKey = key

        // Solo se avisa en un cambio real de canción (no al abrir la app)
        val isNewTrack = key != null && previous != null && previous != key

        if (isNewTrack) {
            delay(250) // margen para que llegue la carátula
            fade.animateTo(1f, tween(FADE_IN_MS))
            delay(HOLD_MS)
        }

        // Si hay algo visible (también si se interrumpió un aviso), se desvanece
        if (fade.value > 0f) {
            fade.animateTo(0f, tween(FADE_OUT_MS))
        }
    }

    if (fade.value > 0.01f) {

        val shape = RoundedCornerShape(18.dp)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .graphicsLayer { this.alpha = fade.value }
                    .width(440.dp)
                    .clip(shape)
                    .background(Panel)
                    .border(1.dp, Blue, shape)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Portada
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(12.dp))
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
                            fontSize = 38.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AHORA SUENA",
                        color = Secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = media.title,
                        color = White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (media.artist.isNotBlank()) {
                        Text(
                            text = media.artist,
                            color = Blue,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
