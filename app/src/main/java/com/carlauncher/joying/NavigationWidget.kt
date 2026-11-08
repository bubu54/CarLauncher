package com.carlauncher.joying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.ui.Blue
import com.carlauncher.joying.ui.White
import com.carlauncher.joying.ui.Secondary
import com.carlauncher.joying.ui.ButtonBackground

@Composable
fun NavigationWidget(
    navigation: CarNavigationData = CarNavigationData()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
    ) {

        if (!navigation.isNavigating) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "➤",
                        color = Blue,
                        fontSize = 75.sp
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "ESPERANDO NAVEGACIÓN",
                        color = White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Abre tu navegador para comenzar",
                        color = Secondary,
                        fontSize = 15.sp
                    )
                }
            }

        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Próxima maniobra
                Column(
                    modifier = Modifier.width(170.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (navigation.maneuver) {
                            "LEFT" -> "↰"
                            "RIGHT" -> "↱"
                            "UTURN" -> "↶"
                            "ROUNDABOUT" -> "⟳"
                            else -> "↑"
                        },
                        color = Blue,
                        fontSize = 100.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = navigation.distanceToManeuver,
                        color = White,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = navigation.instruction,
                        color = White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = navigation.destination,
                        color = Blue,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "SIGUIENTE MANIOBRA",
                        color = Secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "${navigation.nextDistance}  ${navigation.nextInstruction}",
                        color = White,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Información del trayecto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        ButtonBackground,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavigationInfoItem(
                    "DISTANCIA",
                    navigation.remainingDistance
                )

                NavigationInfoItem(
                    "TIEMPO",
                    navigation.remainingTime
                )

                NavigationInfoItem(
                    "LLEGADA",
                    navigation.arrivalTime
                )
            }
        }
    }
}

@Composable
private fun NavigationInfoItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            color = Secondary,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value.ifBlank { "--" },
            color = White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}