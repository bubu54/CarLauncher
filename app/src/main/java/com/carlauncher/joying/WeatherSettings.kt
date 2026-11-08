
package com.carlauncher.joying

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun WeatherSettingsDialog(
    controller: WeatherController,
    weatherData: WeatherData,
    onDismiss: () -> Unit
) {

    var apiKey by remember {
        mutableStateOf(controller.apiKey)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Panel,
        title = {
            Text(
                text = "Configuración meteorológica",
                color = White
            )
        },
        text = {
            Column {

                Text(
                    text = "0e6cf2d7c0edd17db6a38201e3d71f56",
                    color = Secondary
                )

                Button(
                    onClick = {
                        controller.testConnection()
                    },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("Probar conexión HTTPS")
                }

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    label = { Text("API Key") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                if (weatherData.error != null) {
                    Text(
                        text = weatherData.error,
                        color = Color(0xFFFF7070),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (weatherData.loading) {
                    Text(
                        text = "Consultando OpenWeatherMap...",
                        color = Secondary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (weatherData.temperature != null) {
                    Text(
                        text = "Temperatura: ${weatherData.temperature.toInt()} °C",
                        color = Color(0xFF65DDA0),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    controller.setApiKey(apiKey)
                },
                enabled = !weatherData.loading
            ) {
                Text("Guardar y probar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
