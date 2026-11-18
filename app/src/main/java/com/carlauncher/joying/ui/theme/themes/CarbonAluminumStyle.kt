package com.carlauncher.joying.ui.theme.themes

import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Fondo de fibra de carbono, botones de aluminio cepillado con bisel y relieve,
 * y panel central oscuro con brillo de cristal.
 */
object CarbonAluminumStyle : LauncherStyle {

    override val id = "carbon_aluminum"
    override val name = "Fibra de carbono y aluminio"

    override val buttonContentColor = Color(0xFF1B2127)
    override val buttonDisabledContentColor = Color(0xFF727A82)

    // Texto "grabado": una sombra clara 1 px por debajo
    override val buttonTextShadow: Shadow? =
        Shadow(color = Color(0xAAFFFFFF), offset = Offset(0f, 1.5f), blurRadius = 0f)

    // ─────────────────────────── Fondo ───────────────────────────

    override fun screenBackground(modifier: Modifier): Modifier =
        modifier.drawWithCache {

            val cell = 5.dp.toPx().roundToInt().coerceAtLeast(4)

            val weave = ShaderBrush(
                BitmapShader(
                    Textures.carbonTile(cell),
                    Shader.TileMode.REPEAT,
                    Shader.TileMode.REPEAT
                )
            )

            // Reflejo diagonal suave
            val sheen = Brush.linearGradient(
                colors = listOf(Color(0x1CFFFFFF), Color(0x00FFFFFF), Color(0x10FFFFFF)),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )

            // Esquinas más oscuras
            val vignette = Brush.radialGradient(
                colors = listOf(Color(0x00000000), Color(0xB8000000)),
                center = Offset(size.width / 2f, size.height / 2f),
                radius = maxOf(size.width, size.height) * 0.8f
            )

            onDrawBehind {
                drawRect(weave)
                drawRect(sheen)
                drawRect(vignette)
            }
        }

    // ─────────────────────────── Botones ───────────────────────────

    override fun buttonSurface(modifier: Modifier, shape: Shape, pressed: Boolean): Modifier =
        modifier
            .shadow(
                elevation = if (pressed) 2.dp else 7.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(shape)
            .drawWithCache {

                val outline = shape.createOutline(size, layoutDirection, this)

                val brushed = ShaderBrush(
                    BitmapShader(
                        Textures.brushedTile(),
                        Shader.TileMode.REPEAT,
                        Shader.TileMode.REPEAT
                    )
                )

                // Metal: más claro arriba, banda oscura central y rebote de luz abajo.
                // Pulsado: más oscuro y plano (el botón "se hunde").
                val base = Brush.verticalGradient(
                    colors = if (pressed) {
                        listOf(Color(0xFFA2A9B0), Color(0xFF939AA2), Color(0xFF878F97), Color(0xFF959DA4))
                    } else {
                        listOf(Color(0xFFE6EAEE), Color(0xFFBBC2C9), Color(0xFF9CA4AC), Color(0xFFB6BDC4))
                    }
                )

                // Destello especular diagonal
                val specular = Brush.linearGradient(
                    colors = listOf(
                        Color(0x00FFFFFF),
                        Color(if (pressed) 0x22FFFFFFL else 0x66FFFFFFL),
                        Color(0x00FFFFFF),
                        Color(0x26FFFFFF)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )

                // Bisel: luz en el borde superior, sombra en el inferior
                val bevel = Brush.verticalGradient(
                    colors = listOf(
                        Color(if (pressed) 0x66000000L else 0xEEFFFFFFL),
                        Color(0x00FFFFFF),
                        Color(0x00000000),
                        Color(if (pressed) 0x55FFFFFFL else 0xA6000000L)
                    )
                )

                // Sombra interior en la parte baja para dar volumen
                val bottomShade = Brush.verticalGradient(
                    colors = listOf(Color(0x00000000), Color(0x30000000)),
                    startY = size.height * 0.55f,
                    endY = size.height
                )

                val bevelWidth = 3.dp.toPx() // la mitad exterior queda recortada

                onDrawBehind {
                    drawRect(base)
                    drawRect(brushed)
                    drawRect(specular)
                    drawRect(bottomShade)
                    drawOutline(outline, brush = bevel, style = Stroke(width = bevelWidth))
                }
            }

    // ─────────────────────────── Paneles ───────────────────────────

    override fun panelSurface(modifier: Modifier, shape: Shape): Modifier =
        modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(shape)
            .drawWithCache {

                val outline = shape.createOutline(size, layoutDirection, this)

                val base = Brush.verticalGradient(
                    colors = listOf(Color(0xFF17263D), Color(0xFF0E1A2B), Color(0xFF0A1422))
                )

                // Reflejo de cristal en la parte alta
                val gloss = Brush.verticalGradient(
                    colors = listOf(Color(0x16FFFFFF), Color(0x00FFFFFF)),
                    startY = 0f,
                    endY = size.height * 0.4f
                )

                val edge = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3A86C2), Color(0xFF174C79), Color(0xFF0A2B49))
                )

                val edgeWidth = 3.dp.toPx()

                onDrawBehind {
                    drawRect(base)
                    drawRect(gloss)
                    drawOutline(outline, brush = edge, style = Stroke(width = edgeWidth))
                }
            }
}
