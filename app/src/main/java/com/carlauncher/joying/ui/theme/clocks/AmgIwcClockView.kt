package com.carlauncher.joying.ui.theme.clocks

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmgIwcClockView() {
    var currentTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimestamp = System.currentTimeMillis()
            delay(33) // Refresco fluido a ~30-60fps para barrido continuo
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
    val chromeBezelRadius = outerCaseRadius * 0.93f
    val dialRadius = chromeBezelRadius * 0.91f
    val innerPatternRadius = dialRadius * 0.48f
    val canvas = drawContext.canvas.nativeCanvas

    // 1. CARCASA EXTERIOR NEGRA Y BISEL CROMADO PULIDO
    drawCircle(
        color = Color(0xFF0E1012),
        radius = outerCaseRadius,
        center = center
    )

    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFF7D8288),
                Color(0xFFFFFFFF),
                Color(0xFF4C5056),
                Color(0xFFE8ECEF),
                Color(0xFF3E4247),
                Color(0xFFF5F7FA),
                Color(0xFF7D8288)
            ),
            center = center
        ),
        radius = chromeBezelRadius,
        center = center
    )

    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF2B2E33), Color(0xFF090A0B)),
            start = Offset(center.x - dialRadius, center.y - dialRadius),
            end = Offset(center.x + dialRadius, center.y + dialRadius)
        ),
        radius = dialRadius + 2.dp.toPx(),
        center = center
    )

    // 2. ESFERA NEGRA PROFUNDA
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF1A1C1F),
                Color(0xFF101113),
                Color(0xFF070809)
            ),
            center = center,
            radius = dialRadius
        ),
        radius = dialRadius,
        center = center
    )

    // 3. DISCO CENTRAL CON TEXTURA ENTRELAZADA AMG / IWC INGENIEUR
    drawCircle(
        color = Color(0xFF0A0B0D),
        radius = innerPatternRadius + 1.5.dp.toPx(),
        center = center
    )

    val innerClip = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                center = center,
                radius = innerPatternRadius
            )
        )
    }

    clipPath(innerClip) {
        drawCircle(
            color = Color(0xFF131518),
            radius = innerPatternRadius,
            center = center
        )

        val step = 9.dp.toPx()
        val startX = center.x - innerPatternRadius
        val endX = center.x + innerPatternRadius
        val startY = center.y - innerPatternRadius
        val endY = center.y + innerPatternRadius

        var row = 0
        var y = startY
        while (y < endY) {
            var col = 0
            var x = startX
            while (x < endX) {
                val isHorizontal = (row + col) % 2 == 0
                if (isHorizontal) {
                    drawRect(
                        color = Color(0xFF23262B),
                        topLeft = Offset(x + 1f, y + 1.5f),
                        size = Size(step - 2f, step * 0.42f)
                    )
                    drawRect(
                        color = Color(0xFF1D2024),
                        topLeft = Offset(x + 1f, y + step * 0.55f),
                        size = Size(step - 2f, step * 0.42f)
                    )
                } else {
                    drawRect(
                        color = Color(0xFF23262B),
                        topLeft = Offset(x + 1.5f, y + 1f),
                        size = Size(step * 0.42f, step - 2f)
                    )
                    drawRect(
                        color = Color(0xFF1D2024),
                        topLeft = Offset(x + step * 0.55f, y + 1f),
                        size = Size(step * 0.42f, step - 2f)
                    )
                }
                x += step
                col++
            }
            y += step
            row++
        }
    }

    drawCircle(
        color = Color(0x44FFFFFF),
        radius = innerPatternRadius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // 4. LOGOTIPO IWC SCHAFFHAUSEN
    val iwcLogoPaint = NativePaint().apply {
        color = Color(0xFFF0F2F5).toArgb()
        textSize = dialRadius * 0.135f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        letterSpacing = 0.08f
        isAntiAlias = true
        setShadowLayer(4f, 0f, 2f, Color(0xDD000000).toArgb())
    }
    canvas.drawText("IWC", center.x, center.y - dialRadius * 0.21f, iwcLogoPaint)

    val schaffhausenPaint = NativePaint().apply {
        color = Color(0xFFD5D8DD).toArgb()
        textSize = dialRadius * 0.042f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        letterSpacing = 0.20f
        isAntiAlias = true
        setShadowLayer(3f, 0f, 2f, Color(0xDD000000).toArgb())
    }
    canvas.drawText("SCHAFFHAUSEN", center.x, center.y - dialRadius * 0.13f, schaffhausenPaint)

    // 5. ESCALA PERIFÉRICA DE MINUTOS Y NÚMEROS ROJOS AMG (60, 10, 20, 30, 40, 50)
    val redScalePaint = NativePaint().apply {
        color = Color(0xFFE02424).toArgb()
        textSize = dialRadius * 0.062f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
    }

    for (i in 0 until 60) {
        val angleDeg = i * 6f
        val angleRad = (angleDeg - 90.0) * PI / 180.0
        val isTenMark = i % 10 == 0
        val isFiveMark = i % 5 == 0

        if (isTenMark) {
            val label = if (i == 0) "60" else i.toString()
            val textRadius = dialRadius - 11.dp.toPx()
            val tx = center.x + cos(angleRad).toFloat() * textRadius
            val ty = center.y + sin(angleRad).toFloat() * textRadius

            canvas.save()
            val rotationAngle = if (i in 20..40) angleDeg + 180f else angleDeg
            canvas.rotate(rotationAngle, tx, ty)
            canvas.drawText(label, tx, ty + redScalePaint.textSize * 0.35f, redScalePaint)
            canvas.restore()
        } else {
            val outer = dialRadius - 5.dp.toPx()
            val inner = if (isFiveMark) dialRadius - 15.dp.toPx() else dialRadius - 13.dp.toPx()
            drawLine(
                color = if (isFiveMark) Color(0xFFE5E7EB) else Color(0x99CBD0D6),
                start = Offset(center.x + cos(angleRad).toFloat() * inner, center.y + sin(angleRad).toFloat() * inner),
                end = Offset(center.x + cos(angleRad).toFloat() * outer, center.y + sin(angleRad).toFloat() * outer),
                strokeWidth = if (isFiveMark) 2.5.dp.toPx() else 1.2.dp.toPx(),
                cap = StrokeCap.Butt
            )
        }
    }

    // 6. NÚMEROS METÁLICOS APLICADOS "12" Y "6"
    drawMetallicAppliedNumber(canvas, "12", center.x, center.y - dialRadius * 0.62f, dialRadius * 0.21f)
    drawMetallicAppliedNumber(canvas, "6", center.x, center.y + dialRadius * 0.77f, dialRadius * 0.21f)

    // 7. ÍNDICES HORARIOS METÁLICOS EN RELIEVE
    for (h in 1..11) {
        if (h == 6) continue
        val angleDeg = h * 30f
        rotate(degrees = angleDeg, pivot = center) {
            val indexTop = center.y - dialRadius + 22.dp.toPx()
            val indexWidth = 7.dp.toPx()
            val indexHeight = 22.dp.toPx()

            drawRoundRect(
                color = Color(0xAA000000),
                topLeft = Offset(center.x - indexWidth / 2f + 1.5.dp.toPx(), indexTop + 2.dp.toPx()),
                size = Size(indexWidth, indexHeight),
                cornerRadius = CornerRadius(1.5.dp.toPx())
            )

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF8A8F96), Color(0xFFFFFFFF), Color(0xFF6B7075)),
                    startX = center.x - indexWidth / 2f,
                    endX = center.x + indexWidth / 2f
                ),
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