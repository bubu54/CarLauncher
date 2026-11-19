package com.carlauncher.joying.ui.theme.compass

import androidx.compose.runtime.Composable
import com.carlauncher.joying.CarGpsData

/** Una brújula disponible en el selector del widget BRÚJULA. */
data class CompassDefinition(
    val id: String,                                   // identificador estable (se guarda en ajustes)
    val name: String,                                 // nombre mostrado
    val caption: String,                              // descripción corta
    val preview: @Composable () -> Unit,              // miniatura para el popup (llena su espacio)
    val content: @Composable (gps: CarGpsData) -> Unit // el widget
)

/**
 * REGISTRO DE BRÚJULAS.
 *
 * Para añadir una brújula nueva:
 *   1. Crea su fichero en esta carpeta: un @Composable que llene el panel central
 *      (recibe los datos del GPS compartido) y otro @Composable de miniatura.
 *   2. Añade UNA entrada a la lista [all]. Aparecerá sola en el popup de selección
 *      (pulsación larga sobre la brújula).
 *
 * La primera de la lista es la predeterminada.
 */
object CompassRegistry {

    val all: List<CompassDefinition> = listOf(
        CompassDefinition(
            id = "metallic",
            name = "METÁLICA",
            caption = "ROSA · MAG + GPS",
            preview = { MetallicCompassPreview() }
        ) { MetallicCompassView() },

        CompassDefinition(
            id = "aircraft",
            name = "AVIÓN",
            caption = "PITCH · ROLL · RUMBO",
            preview = { AircraftCompassPreview() }
        ) { gps -> AircraftCompassView(gps) }
    )

    fun byId(id: String): CompassDefinition =
        all.firstOrNull { it.id == id } ?: all.first()
}
