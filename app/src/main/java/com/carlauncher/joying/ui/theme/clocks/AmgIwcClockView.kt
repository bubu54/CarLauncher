package com.carlauncher.joying.ui.theme.clocks

// ─────────────────────────────────────────────────────────────────────────────
// NOTA: el fichero original llegó cortado (empezaba a mitad de una instrucción).
// La PRIMERA PARTE de este archivo (imports, composable, caja, bisel, esfera,
// números aplicados 12-3-6-9 y marcadores de hora) está RECONSTRUIDA.
// Desde el comentario "// 8. CÁLCULO CONTINUO" hasta el final, el código es el
// original sin modificar. Si tienes el archivo completo, sustitúyelo sin más:
// el nombre del composable (AmgIwcClockView) y su registro se mantienen.
// ─────────────────────────────────────────────────────────────────────────────

import android.graphics.Paint as NativePaint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmgIwcClockView() {
    var currentTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimestamp = System.currentTimeMillis()
            delay(33)
        }
    }

    val instant = Instant.ofEpochMilli(currentTimestamp)
    val time = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

    Canvas(
        modifier = Modifier
        .fillMaxWidth()
        .height(340.dp)
    ) {
        drawAmgIwcEngine(time)
    }
}

private fun DrawScope.drawAmgIwcEngine(time: ZonedDateTime) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerCaseRadius = minOf(size.width, size.height) * 0.48f
    val bezelRadius = outerCaseRadius * 0.94f
    val dialRadius = bezelRadius * 0.90f
    val canvas = drawContext.canvas.nativeCanvas

    // 1. Caja de acero con sombra
    drawCircle(Color(0x99000000), outerCaseRadius, Offset(center.x, center.y + 4.dp.toPx()))
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFF9AA0A6),
                Color(0xFFE6E9ED),
                Color(0xFF6B7075),
                Color(0xFFD9DDE2),
                Color(0xFF5C6065),
                Color(0xFFE6E9ED),
                Color(0xFF9AA0A6)
            ),
            center = center
        ),
        radius = outerCaseRadius,
        center = center
    )

    // 2. Bisel de grafito con marcas de minuto (la del 12 en rojo)
    drawCircle(Color(0xFF16191D), bezelRadius, center)

    for (i in 0 until 60) {
        val a = Math.toRadians(i * 6.0)
        val major = i % 5 == 0
        val r1 = bezelRadius * 0.97f
        val r2 = bezelRadius * (if (major) 0.87f else 0.92f)

        drawLine(
            color = if (i == 0) Color(0xFFE02424) else Color(0xFFE5E7EB),
            start = Offset(
                center.x + (r1 * sin(a)).toFloat(),
                center.y - (r1 * cos(a)).toFloat()
            ),
            end = Offset(
                center.x + (r2 * sin(a)).toFloat(),
                center.y - (r2 * cos(a)).toFloat()
            ),
            strokeWidth = if (major) 2.4.dp.toPx() else 1.2.dp.toPx(),
            cap = StrokeCap.Butt
        )
    }

    // 3. Esfera antracita con degradado radial
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF31353B), Color(0xFF1C1F24), Color(0xFF0D0F12)),
            center = center,
            radius = dialRadius
        ),
        radius = dialRadius,
        center = center
    )
    drawCircle(
        color = Color(0x33FFFFFF),
        radius = dialRadius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // 4. Texto de esfera
    val brandPaint = NativePaint().apply {
        color = Color(0xFFE5E7EB).toArgb()
        textSize = dialRadius * 0.10f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        isAntiAlias = true
        letterSpacing = 0.18f
    }
    canvas.drawText("AMG", center.x, center.y - dialRadius * 0.30f, brandPaint)

    val subPaint = NativePaint().apply {
        color = Color(0xFF9CA3AF).toArgb()
        textSize = dialRadius * 0.065f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
        isAntiAlias = true
        letterSpacing = 0.2f
    }
    canvas.drawText("AUTOMATIC", center.x, center.y + dialRadius * 0.40f, subPaint)

    // 5. Números aplicados metálicos en 12, 3, 6 y 9
    val numeralSize = dialRadius * 0.30f
    val numeralRadius = dialRadius * 0.68f

    listOf("12" to 0.0, "3" to 90.0, "6" to 180.0, "9" to 270.0).forEach { (label, deg) ->
        val a = Math.toRadians(deg)
        drawMetallicAppliedNumber(
            canvas = canvas,
            text = label,
            x = center.x + (numeralRadius * sin(a)).toFloat(),
            y = center.y - (numeralRadius * cos(a)).toFloat() + numeralSize * 0.36f,
            sizePx = numeralSize
        )
    }

    // 7. Marcadores de hora aplicados (los de 12, 3, 6 y 9 los ocupan los números)
    for (i in 0 until 12) {
        if (i % 3 == 0) continue

        rotate(degrees = i * 30f, pivot = center) {
            val indexWidth = 9.dp.toPx()
            val indexHeight = dialRadius * 0.17f
            val indexTop = center.y - dialRadius * 0.93f

            drawRoundRect(
                color = Color(0xFFD1D5DB),
                topLeft = Offset(center.x - indexWidth / 2f, indexTop),
                size = Size(indexWidth, indexHeight),
                cornerRadius = CornerRadius(1.5.dp.toPx())
            )
            drawRect(
                color = Color(0xFFF8FAFC),
                topLeft = Offset(center.x - indexWidth * 0.24f, indexTop + 3.dp.toPx()),
                size = Size(indexWidth * 0.48f, indexHeight - 6.dp.toPx())
            )
        }
    }
    // 8. CÁLCULO CONTINUO DE HORAS, MINUTOS Y SEGUNDOS
    val millisecond = time.nano / 1_000_000.0
    val secondContinuous = time.second + (millisecond / 1000.0)
    val minuteContinuous = time.minute + (secondContinuous / 60.0)
    val hourContinuous = (time.hour % 12) + (minuteContinuous / 60.0)
    val hourAngleDeg = (hourContinuous * 30.0).toFloat()
    val minuteAngleDeg = (minuteContinuous * 6.0).toFloat()
    val secondAngleDeg = (secondContinuous * 6.0).toFloat()
    // Aguja de Horas
    drawAmgIngenieurHand(
        center = center,
        angleDeg = hourAngleDeg,
        length = dialRadius * 0.56f,
        tailLength = dialRadius * 0.14f,
        width = 11.dp.toPx()
    )
    // Aguja de Minutos
    drawAmgIngenieurHand(
        center = center,
        angleDeg = minuteAngleDeg,
        length = dialRadius * 0.82f,
        tailLength = dialRadius * 0.16f,
        width = 9.dp.toPx()
    )
    // Eje intermedio metálico (bajo el segundero)
    drawCircle(Color(0x99000000), 11.dp.toPx(), Offset(center.x, center.y + 2.dp.toPx()))
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE5E7EB), Color(0xFF6B7280)),
            start = Offset(center.x - 10.dp.toPx(), center.y - 10.dp.toPx()),
            end = Offset(center.x + 10.dp.toPx(), center.y + 10.dp.toPx())
        ),
        radius = 10.dp.toPx(),
        center = center
    )
    // 9. SEGUNDERO DE MOVIMIENTO CONTINUO CON PUNTA ROJA AMG
    drawAmgSecondHand(
        center = center,
        angleDeg = secondAngleDeg,
        length = dialRadius * 0.88f,
        tailLength = dialRadius * 0.24f
    )
}
private fun DrawScope.drawAmgSecondHand(
    center: Offset,
    angleDeg: Float,
    length: Float,
    tailLength: Float
) {
    rotate(degrees = angleDeg, pivot = center) {
        val tipY = center.y - length
        val tailY = center.y + tailLength
        val redTipLength = length * 0.16f
        // 1. Sombra proyectada del segundero
        drawLine(
            color = Color(0x88000000),
            start = Offset(center.x + 2.dp.toPx(), tailY + 4.dp.toPx()),
            end = Offset(center.x + 2.dp.toPx(), tipY + 4.dp.toPx()),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0x88000000),
            radius = 4.dp.toPx(),
            center = Offset(center.x + 2.dp.toPx(), center.y + tailLength * 0.65f + 4.dp.toPx())
        )
        // 2. Cuerpo metálico principal del segundero
        drawLine(
            color = Color(0xFFE5E7EB),
            start = Offset(center.x, tailY),
            end = Offset(center.x, tipY + redTipLength),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Butt
        )
        // 3. Punta roja deportiva AMG
        drawLine(
            color = Color(0xFFE02424),
            start = Offset(center.x, tipY + redTipLength),
            end = Offset(center.x, tipY),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
        // 4. Contrapeso circular trasero
        drawCircle(
            color = Color(0xFFE5E7EB),
            radius = 4.dp.toPx(),
            center = Offset(center.x, center.y + tailLength * 0.65f)
        )
        drawCircle(
            color = Color(0xFF1F2226),
            radius = 1.6.dp.toPx(),
            center = Offset(center.x, center.y + tailLength * 0.65f)
        )
        // 5. Remache central superior del segundero
        drawCircle(Color(0xFF1A1D21), 5.5.dp.toPx(), center)
        drawCircle(Color(0xFFD1D5DB), 4.dp.toPx(), center)
        drawCircle(Color(0xFFE02424), 1.6.dp.toPx(), center)
    }
}
private fun drawMetallicAppliedNumber(
    canvas: android.graphics.Canvas,
    text: String,
    x: Float,
    y: Float,
    sizePx: Float
) {
    val typefaceBold = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    val shadowPaint = NativePaint().apply {
        color = Color(0xCC000000).toArgb()
        textSize = sizePx
        textAlign = NativePaint.Align.CENTER
        typeface = typefaceBold
        isAntiAlias = true
    }
    canvas.drawText(text, x + 2f, y + 4f, shadowPaint)
    val strokePaint = NativePaint().apply {
        color = Color(0xFFE5E7EB).toArgb()
        textSize = sizePx
        textAlign = NativePaint.Align.CENTER
        typeface = typefaceBold
        style = NativePaint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }
    canvas.drawText(text, x, y, strokePaint)
    val fillPaint = NativePaint().apply {
        color = Color(0xFF32363B).toArgb()
        textSize = sizePx
        textAlign = NativePaint.Align.CENTER
        typeface = typefaceBold
        isAntiAlias = true
    }
    canvas.drawText(text, x, y, fillPaint)
}
private fun DrawScope.drawAmgIngenieurHand(
    center: Offset,
    angleDeg: Float,
    length: Float,
    tailLength: Float,
    width: Float
) {
    rotate(degrees = angleDeg, pivot = center) {
        val totalHeight = length + tailLength
        val topY = center.y - length
        drawRoundRect(
            color = Color(0x88000000),
            topLeft = Offset(center.x - width / 2f + 2.dp.toPx(), topY + 4.dp.toPx()),
            size = Size(width, totalHeight),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF9CA3AF), Color(0xFFFFFFFF), Color(0xFF6B7280)),
                startX = center.x - width / 2f,
                endX = center.x + width / 2f
            ),
            topLeft = Offset(center.x - width / 2f, topY),
            size = Size(width, totalHeight),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFF272A2E),
            topLeft = Offset(center.x - width / 2f, topY),
            size = Size(width, totalHeight),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
        val lumeWidth = width * 0.46f
        val lumeHeight = length * 0.76f
        drawRoundRect(
            color = Color(0xFFF8FAFC),
            topLeft = Offset(center.x - lumeWidth / 2f, topY + 5.dp.toPx()),
            size = Size(lumeWidth, lumeHeight),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
    }
}
