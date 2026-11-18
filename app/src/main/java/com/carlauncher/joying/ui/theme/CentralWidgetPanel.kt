
package com.carlauncher.joying.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarMediaData
import com.carlauncher.joying.CarMediaController
import com.carlauncher.joying.CarNavigationData
import com.carlauncher.joying.CarGpsData
import com.carlauncher.joying.CarGpsController
import com.carlauncher.joying.ui.GpsSpeedometer
import com.carlauncher.joying.ui.CompassWidget
import com.carlauncher.joying.NavigationWidget
import com.carlauncher.joying.AutoWidget
import com.carlauncher.joying.LocalLauncherSettings
import com.carlauncher.joying.ui.theme.themes.launcherPanel
import com.carlauncher.joying.ui.theme.clocks.ClockRegistry

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CentralWidgetPanel(
    widget: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    mediaData: CarMediaData,
    mediaController: CarMediaController,
    navigationData: CarNavigationData,
    gpsData: CarGpsData,
    gpsController: CarGpsController,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .launcherPanel(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (widget) {

                "INICIO" -> {
                    Text(
                        "CAR LAUNCHER",
                        color = Blue,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "TOCA PARA ELEGIR WIDGET",
                        color = Secondary,
                        fontSize = 12.sp
                    )
                }

                "RELOJ" -> {
                    val clock = ClockRegistry.byId(LocalLauncherSettings.current.data.clockId)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = clock.name,
                            color = Blue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        clock.content()
                        Text(
                            text = clock.caption,
                            color = Secondary,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
                "VELOCÍMETRO" -> GpsSpeedometer(
                    gpsData = gpsData,
                    gpsController = gpsController
                )

                "BRÚJULA" -> {
                    CompassWidget()
                }

                "NAVEGACIÓN" -> {
                    NavigationWidget(
                        navigation = navigationData
                    )
                }

                "MÚSICA" -> {
                    MusicInfoWidget(
                        media = mediaData,
                        controller = mediaController
                    )
                }

                "AUTO" -> AutoWidget(
                    navigationData = navigationData,
                    gpsController = gpsController,
                    mediaData = mediaData,
                    mediaController = mediaController
                )
            }
        }
    }
}
