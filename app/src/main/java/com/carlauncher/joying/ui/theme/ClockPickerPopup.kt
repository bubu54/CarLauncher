package com.carlauncher.joying.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.ui.theme.clocks.ClockDefinition
import com.carlauncher.joying.ui.theme.clocks.ClockRegistry

private val PreviewSize = 170.dp
private const val ClockNativeSize = 330f   // los relojes se dibujan en 330 dp de alto

/** Popup con los relojes disponibles en un scroll horizontal. */
@Composable
fun ClockPickerPopup(
    selectedId: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val clocks = ClockRegistry.all
    val selectedIndex = clocks.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selectedIndex - 1).coerceAtLeast(0)
    )

    // Toque en el fondo = cerrar
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Panel)
                .border(1.dp, BlueBorder, RoundedCornerShape(20.dp))
                // El panel absorbe los toques para que no cierren el popup
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ELEGIR RELOJ",
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Desliza para ver más relojes",
                color = Secondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                state = listState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(clocks, key = { it.id }) { clock ->
                    ClockCard(
                        clock = clock,
                        selected = clock.id == selectedId,
                        onClick = { onSelect(clock.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBackground,
                    contentColor = White
                )
            ) {
                Text("CERRAR")
            }
        }
    }
}

@Composable
private fun ClockCard(
    clock: ClockDefinition,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = Modifier
            .width(PreviewSize + 24.dp)
            .clip(shape)
            .background(ButtonBackground)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Blue else BlueBorder,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Miniatura: el reloj real a su tamaño nativo (330 dp), reducido
        Box(
            modifier = Modifier
                .size(PreviewSize)
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            val scale = PreviewSize.value / ClockNativeSize

            Box(
                modifier = Modifier
                    .requiredSize(ClockNativeSize.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            ) {
                clock.content()
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = clock.name,
            color = if (selected) Blue else White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Text(
            text = if (selected) "✓  EN USO" else "TOCA PARA USAR",
            color = Secondary,
            fontSize = 10.sp
        )
    }
}
