
package com.carlauncher.joying.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

@Composable
fun WidgetSelectorDialog(
    selectedWidget: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(390.dp),
            color = Panel,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, BlueBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "SELECCIONAR WIDGET",
                    color = Blue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                )

                WidgetChoice(
                    name = "RELOJ",
                    description = "Pilot Classic",
                    selected = selectedWidget == "RELOJ",
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                    onClick = { onSelect("RELOJ") }
                )

                WidgetChoice(
                    name = "VELOCÍMETRO",
                    description = "GPS",
                    selected = selectedWidget == "VELOCÍMETRO",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp),
                    onClick = { onSelect("VELOCÍMETRO") }
                )

                WidgetChoice(
                    name = "BRÚJULA",
                    description = "Orientación",
                    selected = selectedWidget == "BRÚJULA",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    onClick = { onSelect("BRÚJULA") }
                )

                WidgetChoice(
                    name = "NAVEGACIÓN",
                    description = "Maniobras",
                    selected = selectedWidget == "NAVEGACIÓN",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 35.dp, bottom = 75.dp),
                    onClick = { onSelect("NAVEGACIÓN") }
                )

                WidgetChoice(
                    name = "MÚSICA",
                    description = "Reproductor",
                    selected = selectedWidget == "MÚSICA",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 35.dp, bottom = 75.dp),
                    onClick = { onSelect("MÚSICA") }
                )

                WidgetChoice(
                    name = "AUTO",
                    description = "Automático",
                    selected = selectedWidget == "AUTO",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 3.dp),
                    onClick = { onSelect("AUTO") }
                )
            }
        }
    }
}


@Composable
fun WidgetChoice(
    name: String,
    description: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val buttonColor = if (selected) {
        Color(0xFF204A70)
    } else {
        ButtonBackground
    }

    Box(
        modifier = modifier
            .width(142.dp)
            .height(65.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(buttonColor)
            .border(
                width = 1.dp,
                color = if (selected) Blue else BlueBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                color = White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                color = Secondary,
                fontSize = 9.sp
            )
        }
    }
}
