package com.carlauncher.joying.ui.theme.themes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape

/**
 * Estilo visual del launcher (fondo, botones y paneles).
 *
 * Para añadir un tema nuevo:
 *   1. Crea un `object MiTema : LauncherStyle` en esta carpeta.
 *   2. Añádelo a la lista [LauncherStyles.all].
 * Aparecerá solo en Ajustes > Apariencia > Estilo visual.
 */
interface LauncherStyle {
    val id: String                          // identificador estable (se guarda en ajustes)
    val name: String                        // nombre mostrado

    val buttonContentColor: Color           // color del texto/iconos sobre los botones
    val buttonDisabledContentColor: Color   // ... cuando el botón está desactivado
    val buttonTextShadow: Shadow?           // sombra del texto (relieve), opcional

    /** Fondo de toda la pantalla. */
    fun screenBackground(modifier: Modifier): Modifier

    /** Superficie de un botón. [pressed] = está siendo pulsado. */
    fun buttonSurface(modifier: Modifier, shape: Shape, pressed: Boolean): Modifier

    /** Superficie de un panel (panel central, barra inferior). */
    fun panelSurface(modifier: Modifier, shape: Shape): Modifier
}

object LauncherStyles {

    const val DEFAULT_ID = "carbon_aluminum"

    /** Temas disponibles. El primero es el predeterminado. */
    val all: List<LauncherStyle> = listOf(
        CarbonAluminumStyle,
        ClassicFlatStyle
    )

    fun byId(id: String): LauncherStyle =
        all.firstOrNull { it.id == id } ?: all.first()
}

val LocalLauncherStyle = staticCompositionLocalOf<LauncherStyle> { ClassicFlatStyle }

@Composable
fun Modifier.launcherBackground(): Modifier =
    LocalLauncherStyle.current.screenBackground(this)

@Composable
fun Modifier.launcherButton(shape: Shape, pressed: Boolean = false): Modifier =
    LocalLauncherStyle.current.buttonSurface(this, shape, pressed)

@Composable
fun Modifier.launcherPanel(shape: Shape): Modifier =
    LocalLauncherStyle.current.panelSurface(this, shape)
