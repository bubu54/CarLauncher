
package com.carlauncher.joying.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.carlauncher.joying.CarMediaData
import com.carlauncher.joying.CarMediaController

@Composable
fun CarMediaBar(
    media: CarMediaData,
    controller: CarMediaController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                Panel,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Text(
            text = "⏮",
            color = if (media.hasSession) Blue else Secondary,
            fontSize = 27.sp,
            modifier = Modifier
                .clickable(enabled = media.hasSession) {
                    controller.previous(media)
                }
                .padding(horizontal = 12.dp)
        )

        Text(
            text = if (media.isPlaying) "Ⅱ" else "▶",
            color = if (media.hasSession) Blue else Secondary,
            fontSize = 27.sp,
            modifier = Modifier
                .clickable(enabled = media.hasSession) {
                    controller.playPause(media)
                }
                .padding(horizontal = 12.dp)
        )

        Text(
            text = "⏭",
            color = if (media.hasSession) Blue else Secondary,
            fontSize = 27.sp,
            modifier = Modifier
                .clickable(enabled = media.hasSession) {
                    controller.next(media)
                }
                .padding(horizontal = 12.dp)
        )
    }
}
