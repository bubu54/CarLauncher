package com.carlauncher.joying

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.carlauncher.joying.ui.Blue
import com.carlauncher.joying.ui.ButtonBackground
import com.carlauncher.joying.ui.Secondary
import com.carlauncher.joying.ui.White
import kotlinx.coroutines.delay

private val HudBackground = Color(0xD00B1320)

private fun hasNotificationAccess(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context)
        .contains(context.packageName)

private fun openNotificationAccessSettings(context: Context) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) {
    }
}

private fun maneuverArrow(maneuver: String): String = when (maneuver) {
    "LEFT" -> "↰"
    "RIGHT" -> "↱"
    "UTURN" -> "↶"
    "ROUNDABOUT" -> "⟳"
    else -> "↑"
}

/** Estado "sin guía activa": qué falta y cómo resolverlo. */
private data class NavStatus(
    val icon: String,
    val title: String,
    val subtitle: String,
    val buttonText: String,
    val onButton: () -> Unit,
    val diagnostic: String? = null
)

@Composable
fun NavigationWidget(
    navigation: CarNavigationData = CarNavigationData(),
    gps: CarGpsData = CarGpsData()
) {
    val context = LocalContext.current
    val navApp = LocalLauncherSettings.current.data.navApp
    val showMap = LocalLauncherSettings.current.data.navShowMap

    val listenerConnected by CarNavigationRepository.listenerConnected.collectAsState()
    val seen by CarNavigationRepository.seen.collectAsState()

    // Se vuelve a comprobar el permiso (el usuario lo concede en otra pantalla)
    var accessGranted by remember { mutableStateOf(hasNotificationAccess(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            accessGranted = hasNotificationAccess(context)
            delay(1500)
        }
    }

    val status: NavStatus? = when {

        navigation.isNavigating -> null

        !accessGranted -> NavStatus(
            icon = "🔒",
            title = "ACCESO A NOTIFICACIONES",
            subtitle = "Para leer las indicaciones de Maps, Waze o Sygic el launcher " +
                    "necesita acceso a las notificaciones.",
            buttonText = "CONCEDER ACCESO",
            onButton = { openNotificationAccessSettings(context) }
        )

        !listenerConnected -> NavStatus(
            icon = "⏳",
            title = "ACTIVANDO SERVICIO",
            subtitle = "El permiso está concedido pero Android aún no ha enlazado el " +
                    "servicio. Si no cambia en unos segundos, desactiva y vuelve a " +
                    "activar el acceso del launcher.",
            buttonText = "AJUSTES DE ACCESO",
            onButton = { openNotificationAccessSettings(context) }
        )

        else -> NavStatus(
            icon = "➤",
            title = "ESPERANDO NAVEGACIÓN",
            subtitle = "Inicia una ruta en tu app de navegación. El widget se activa " +
                    "cuando empieza la guía (abrir la app no basta).",
            buttonText = "ABRIR NAVEGACIÓN",
            onButton = { CarNavigationLauncher.openPreferred(context, navApp) },
            diagnostic = if (seen.isBlank()) {
                "Servicio activo · sin notificaciones de navegación"
            } else {
                "Servicio activo · detectado:\n$seen"
            }
        )
    }

    if (showMap) {

        // Mapa de fondo con las indicaciones superpuestas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            NavigationMap(
                gps = gps,
                modifier = Modifier.fillMaxSize()
            )

            if (status == null) {
                MapGuidanceHud(navigation)
            } else {
                MapStatusBanner(status)
            }
        }

    } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp)
        ) {
            if (status == null) {
                ActiveNavigation(navigation)
            } else {
                NavigationMessage(
                    icon = status.icon,
                    title = status.title,
                    subtitle = status.subtitle,
                    buttonText = status.buttonText,
                    onButton = status.onButton,
                    diagnostic = status.diagnostic
                )
            }
        }
    }
}

// ───────────────────────── Superposición sobre el mapa ─────────────────────────

@Composable
private fun MapGuidanceHud(navigation: CarNavigationData) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {

        // Maniobra actual (arriba a la izquierda)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .widthIn(max = 400.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(HudBackground)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(92.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = maneuverArrow(navigation.maneuver),
                    color = Blue,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = navigation.distanceToManeuver,
                    color = White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                if (navigation.source.isNotBlank()) {
                    Text(
                        text = navigation.source.uppercase(),
                        color = Secondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = navigation.instruction,
                    color = White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (navigation.destination.isNotBlank()) {
                    Text(
                        text = navigation.destination,
                        color = Blue,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Datos del trayecto (abajo)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudBackground)
                .padding(vertical = 10.dp, horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            NavigationInfoItem("DISTANCIA", navigation.remainingDistance)
            NavigationInfoItem("TIEMPO", navigation.remainingTime)
            NavigationInfoItem("LLEGADA", navigation.arrivalTime)
        }
    }
}

@Composable
private fun MapStatusBanner(status: NavStatus) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(HudBackground)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.title,
                    color = White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = status.subtitle,
                    color = Secondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (status.diagnostic != null) {
                    Text(
                        text = status.diagnostic,
                        color = Secondary,
                        fontSize = 9.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Button(
                onClick = status.onButton,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue,
                    contentColor = White
                )
            ) {
                Text(status.buttonText)
            }
        }
    }
}

// ───────────────────────── Vista sin mapa ─────────────────────────

@Composable
private fun NavigationMessage(
    icon: String,
    title: String,
    subtitle: String,
    buttonText: String,
    onButton: () -> Unit,
    diagnostic: String? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                color = Blue,
                fontSize = 60.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                color = Secondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onButton,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Blue,
                    contentColor = White
                )
            ) {
                Text(buttonText)
            }

            if (diagnostic != null) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = diagnostic,
                    color = Secondary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ActiveNavigation(navigation: CarNavigationData) {

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
                text = maneuverArrow(navigation.maneuver),
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
            if (navigation.source.isNotBlank()) {
                Text(
                    text = navigation.source.uppercase(),
                    color = Secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            Text(
                text = navigation.instruction,
                color = White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (navigation.destination.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = navigation.destination,
                    color = Blue,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (navigation.nextInstruction.isNotBlank()) {
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
        NavigationInfoItem("DISTANCIA", navigation.remainingDistance)
        NavigationInfoItem("TIEMPO", navigation.remainingTime)
        NavigationInfoItem("LLEGADA", navigation.arrivalTime)
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
