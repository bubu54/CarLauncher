package com.carlauncher.joying.ui.theme.clocks

import androidx.compose.runtime.Composable

/** Un reloj disponible en el selector del widget RELOJ. */
data class ClockDefinition(
    val id: String,                       // identificador estable (se guarda en los ajustes)
    val name: String,                     // nombre mostrado
    val caption: String,                  // texto pequeño bajo el reloj
    val nativeHeight: Float = 330f,       // alto (dp) con el que se dibuja el reloj
    val content: @Composable () -> Unit   // el reloj
)

/**
 * REGISTRO DE RELOJES.
 *
 * Para añadir un reloj nuevo:
 *   1. Crea su fichero en esta carpeta con un @Composable (p. ej. MiRelojView()).
 *      Debe dibujarse en 330 dp de alto, como los demás.
 *   2. Añade UNA línea a la lista [all]. Aparecerá solo en el popup de selección
 *      (pulsación larga sobre el reloj).
 *
 * El primero de la lista es el reloj por defecto.
 */
object ClockRegistry {

    val all: List<ClockDefinition> = listOf(
        ClockDefinition(
            id = "speedmaster",
            name = "SPEEDMASTER",
            caption = "AUTOMATIC  ·  LOCAL TIME"
        ) { SpeedmasterClockView() },

        ClockDefinition(
            id = "pilot_classic",
            name = "PILOT CLASSIC",
            caption = "AUTOMATIC  ·  LOCAL TIME",
            nativeHeight = 340f
        ) { PilotClassicClockView() },

        ClockDefinition(
            id = "amg_ingenieur",
            name = "AMG INGENIEUR",
            caption = "AUTOMATIC  ·  LOCAL TIME",
            nativeHeight = 340f
        ) { AmgIwcClockView() },

        ClockDefinition(
            id = "khaki_xwind",
            name = "KHAKI X-WIND",
            caption = "FIELD  ·  LOCAL TIME"
        ) { KhakiXWindClockView() }
    )

    fun byId(id: String): ClockDefinition =
        all.firstOrNull { it.id == id } ?: all.first()
}
