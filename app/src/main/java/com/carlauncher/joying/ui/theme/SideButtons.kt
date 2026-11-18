package com.carlauncher.joying.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncher.joying.CarNavigationLauncher
import com.carlauncher.joying.LocalLauncherSettings
import com.carlauncher.joying.ui.theme.themes.LocalLauncherStyle

@Composable
fun SideButtons(
    buttons: List<String>,
    modifier: Modifier = Modifier,
    onAppsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val launcherSettings = LocalLauncherSettings.current
    val style = LocalLauncherStyle.current

    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        buttons.forEach { button ->

            StyledButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(11.dp),
                onClick = {
                    when (button) {
                        "NAVEGACIÓN" -> CarNavigationLauncher.openPreferred(
                            context,
                            launcherSettings.data.navApp
                        )

                        "MÚSICA" -> {
                            val pkg = launcherSettings.data.musicApp

                            if (pkg.isNotBlank()) {
                                CarNavigationLauncher.openApp(context, pkg, "reproductor")
                            } else {
                                Toast.makeText(
                                    context,
                                    "Elige un reproductor en Ajustes › Música",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        "TELÉFONO" -> CarNavigationLauncher.openDialer(context)
                        "APLICACIONES" -> onAppsClick()
                        "ANDROID AUTO" -> CarNavigationLauncher.openAndroidAuto(context)

                        else -> Toast.makeText(
                            context,
                            "$button: función pendiente",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Text(
                    text = button,
                    color = style.buttonContentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(shadow = style.buttonTextShadow)
                )
            }
        }
    }
}
