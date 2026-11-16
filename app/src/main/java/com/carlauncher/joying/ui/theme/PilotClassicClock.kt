package com.carlauncher.joying.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint as NativePaint
import android.graphics.BlurMaskFilter

private const val CLOCK_PILOT = 0
private const val CLOCK_SPEEDMASTER = 1
private const val CLOCK_XWIND = 2

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PilotClassicClock() {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var selectedClock by rememberSaveable { mutableIntStateOf(CLOCK_PILOT) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    selectedClock = (selectedClock + 1) % 3
                }
            )
    ) {
        when (selectedClock) {
            CLOCK_PILOT -> drawIwcPilot(currentTime)
            CLOCK_SPEEDMASTER -> drawSpeedmasterMoonwatch(currentTime)
            CLOCK_XWIND -> drawKhakiXWind(currentTime)
        }
    }
}

private fun DrawScope.drawIwcPilot(currentTime: LocalDateTime) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = minOf(size.width, size.height) * 0.46f
    val canvas = drawContext.canvas.nativeCanvas

    // 1. Textura de la Esfera Principal: Degradado radial que simula la curvatura del cristal y el satinado mate
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1E2124), Color(0xFF111214), Color(0xFF090A0B)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Bisel metálico externo sutil
    drawCircle(
        color = Color(0xFF3A3F44),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Configuración del texto numérico grueso de aviación militar con sombra integrada
    val numberPaint = NativePaint().apply {
        color = Color(0xFFFFFFFF).toArgb()
        textSize = radius * 0.15f
        textAlign = NativePaint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(3f, 1f, 2f, Color(0x99000000).toArgb())
    }

    // Triángulo instrumental superior clásico
    val triangleTopY = center.y - radius + 18.dp.toPx()
    val trianglePath = android.graphics.Path().apply {
        moveTo(center.x, triangleTopY)
        lineTo(center.x - 10.dp.toPx(), triangleTopY + 16.dp.toPx())
        lineTo(center.x + 10.dp.toPx(), triangleTopY + 16.dp.toPx())
        close()
    }
    canvas.drawPath(trianglePath, NativePaint().apply {
        color = Color(0xFFFFFFFF).toArgb()
        style = NativePaint.Style.FILL
        isAntiAlias = true
        setShadowLayer(4f, 0f, 2f, Color(0x66000000).toArgb())
    })
    drawCircle(Color.White, 3.dp.toPx(), Offset(center.x - 18.dp.toPx(), triangleTopY + 8.dp.toPx()))
    drawCircle(Color.White, 3.dp.toPx(), Offset(center.x + 18.dp.toPx(), triangleTopY + 8.dp.toPx()))

    // Marcas perimetrales ferroviarias
    for (i in 0 until 60) {
        val angle = (i * 6.0 - 90.0) * PI / 180.0
        val isMajor = i % 5 == 0
        val outer = radius - 4.dp.toPx()
        val inner = if (isMajor) radius - 20.dp.toPx() else radius - 10.dp.toPx()

        drawLine(
            color = if (isMajor) Color.White else Color(0xB3FFFFFF),
            start = Offset(center.x + cos(angle).toFloat() * inner, center.y + sin(angle).toFloat() * inner),
            end = Offset(center.x + cos(angle).toFloat() * outer, center.y + sin(angle).toFloat() * outer),
            strokeWidth = if (isMajor) 3.dp.toPx() else 1.2.dp.toPx(),
            cap = StrokeCap.Square
        )
    }

    // Distribución de numerales del IWC
    for (h in 1..11) {
        if (h == 3 || h == 6) continue
        val angle = (h * 30.0 - 90.0) * PI / 180.0
        val numberRadius = radius - 42.dp.toPx()
        val x = center.x + cos(angle).toFloat() * numberRadius
        val y = center.y + sin(angle).toFloat() * numberRadius + numberPaint.textSize * 0.35f
        canvas.drawText(h.toString(), x, y, numberPaint)
    }

    // Subesfera de reserva de marcha (Efecto Guilloché / Círculos concéntricos)
    val powerCenter = Offset(center.x + radius * 0.42f, center.y)
    val powerRadius = radius * 0.22f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF141517), Color(0xFF090A0B)),
            center = powerCenter,
            radius = powerRadius
        ),
        radius = powerRadius,
        center = powerCenter
    )
    drawCircle(Color(0x4DFFFFFF), powerRadius, powerCenter, style = Stroke(width = 1.dp.toPx()))

    for (p in 0..7) {
        val a = (p * 25.0 + 100.0) * PI / 180.0
        val color = if (p <= 2) Color(0xFFE53935) else Color.White
        drawLine(
            color,
            Offset(powerCenter.x + cos(a).toFloat() * powerRadius * 0.8f, powerCenter.y + sin(a).toFloat() * powerRadius * 0.8f),
            Offset(powerCenter.x + cos(a).toFloat() * powerRadius * 0.95f, powerCenter.y + sin(a).toFloat() * powerRadius * 0.95f),
            1.5.dp.toPx()
        )
    }

    // Ventana de fecha rectangular rehundida a las 6 en punto
    val dateRectCenter = Offset(center.x, center.y + radius * 0.58f)
    val datePaint = NativePaint().apply {
        color = Color.White.toArgb()
        textSize = radius * 0.09f
        textAlign = NativePaint.Align.CENTER
        typeface = android.graphics.Typeface.SANS_SERIF
        isAntiAlias = true
    }
    canvas.drawText(currentTime.format(DateTimeFormatter.ofPattern("25")), dateRectCenter.x, dateRectCenter.y, datePaint)

    // 5. Manecillas Flieger 3D: Degradados lineales metálicos partidos para simular relieve/ángulo mecánico real
    val hourAngle = (currentTime.hour % 12 * 30.0 + currentTime.minute * 0.5 - 90.0) * PI / 180.0
    val minuteAngle = (currentTime.minute * 6.0 - 90.0) * PI / 180.0
    val secondAngle = (currentTime.second * 6.0 - 90.0) * PI / 180.0

    // Sombras proyectadas de las manecillas sobre el dial
    drawFliegerHandShadow(center, hourAngle, radius * 0.50f, 9.dp.toPx())
    drawFliegerHandShadow(center, minuteAngle, radius * 0.76f, 6.5.dp.toPx())

    // Manecillas metálicas reales
    drawFliegerHand3D(center, hourAngle, radius * 0.50f, 9.dp.toPx())
    drawFliegerHand3D(center, minuteAngle, radius * 0.76f, 6.5.dp.toPx())

    // Segundero con contrapeso circular trasero estilizado
    val secondStart = center - Offset(cos(secondAngle).toFloat() * radius * 0.15f, sin(secondAngle).toFloat() * radius * 0.15f)
    val secondEnd = center + Offset(cos(secondAngle).toFloat() * radius * 0.85f, sin(secondAngle).toFloat() * radius * 0.85f)
    drawLine(Color(0xFFE5E5E5), secondStart, secondEnd, 1.5.dp.toPx(), StrokeCap.Round)

    // Capuchón central tridimensional
    drawCircle(Color(0xFF0A0B0C), 6.dp.toPx(), center)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFF7A7E85), Color(0xFF2C2D30)),
            center = center,
            radius = 3.dp.toPx()
        ),
        radius = 3.dp.toPx(),
        center = center
    )
}

private fun DrawScope.drawSpeedmasterMoonwatch(currentTime: LocalDateTime) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = minOf(size.width, size.height) * 0.46f
    val canvas = drawContext.canvas.nativeCanvas

    // Fondo texturizado asfalto / negro poroso aterciopelado clásico de Omega
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF191B1E), Color(0xFF0F1012)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Doble punto característico Super-LumiNova envejecido a las 12
    val topMarkerY = center.y - radius + 22.dp.toPx()
    val tritiumColor = Color(0xFFE2DAC2)
    drawCircle(tritiumColor, 2.2.dp.toPx(), Offset(center.x - 6.dp.toPx(), topMarkerY))
    drawCircle(tritiumColor, 2.2.dp.toPx(), Offset(center.x + 6.dp.toPx(), topMarkerY))
    drawLine(tritiumColor, Offset(center.x, topMarkerY - 4.dp.toPx()), Offset(center.x, topMarkerY + 10.dp.toPx()), 3.dp.toPx())

    // Índices de precisión escalonados
    for (i in 0 until 180) {
        val a = (i * 2.0 - 90.0) * PI / 180.0
        val isFiveMinutes = i % 15 == 0
        val isMinute = i % 3 == 0

        val outer = radius - 2.dp.toPx()
        val inner = when {
            isFiveMinutes -> radius - 22.dp.toPx()
            isMinute -> radius - 15.dp.toPx()
            else -> radius - 8.dp.toPx()
        }
        drawLine(
            if (isFiveMinutes) tritiumColor else Color(0x80FFFFFF),
            Offset(center.x + cos(a).toFloat() * inner, center.y + sin(a).toFloat() * inner),
            Offset(center.x + cos(a).toFloat() * outer, center.y + sin(a).toFloat() * outer),
            if (isFiveMinutes) 2.5.dp.toPx() else 0.8.dp.toPx(), StrokeCap.Square
        )
    }
// Tres Subesferas rehundidas con bordes en relieve sombreados
    val subRadius = radius * 0.22f
    val subPositions = listOf(
        Offset(center.x - radius * 0.38f, center.y),
        Offset(center.x + radius * 0.38f, center.y),
        Offset(center.x, center.y + radius * 0.36f)
    )
    subPositions.forEachIndexed { index, p ->
// Fondo rehundido oscuro uniforme
        drawCircle(Color(0xFF0A0B0C), subRadius, p)
// Sombra perimetral interior suave para dar volumen de hundimiento
        drawCircle(Color(0x33000000), subRadius - 1.dp.toPx(), p, style = Stroke(width = 2.dp.toPx()))
        drawCircle(Color(0x26FFFFFF), subRadius, p, style = Stroke(width = 1.dp.toPx()))
        for (tick in 0 until 4) {
            val a = (tick * 90.0 - 90.0) * PI / 180.0
            drawLine(
                Color.White,
                Offset(p.x + cos(a).toFloat() * subRadius * 0.72f, p.y + sin(a).toFloat() * subRadius * 0.72f),
                Offset(p.x + cos(a).toFloat() * subRadius * 0.95f, p.y + sin(a).toFloat() * subRadius * 0.95f),
                1.5.dp.toPx()
            )
        }
        val angle = (currentTime.second * 6.0 + index * 120.0 - 90.0) * PI / 180.0
        drawLine(Color.White, p, p + Offset(cos(angle).toFloat() * subRadius * 0.75f, sin(angle).toFloat() * subRadius * 0.75f), 1.8.dp.toPx(), StrokeCap.Round)
    }
// Manecillas de bastón lacadas con volumen volumétrico
    val hourAngle = (currentTime.hour % 12 * 30.0 + currentTime.minute * 0.5 - 90.0) * PI / 180.0
    val minuteAngle = (currentTime.minute * 6.0 - 90.0) * PI / 180.0
    val secondAngle = (currentTime.second * 6.0 - 90.0) * PI / 180.0
// Sombras arrojadas de bastones
    drawLine(Color(0x80000000), center + Offset(2f, 2f), center + Offset(cos(hourAngle).toFloat() * radius * 0.52f + 2f, sin(hourAngle).toFloat() * radius * 0.52f + 2f), 4.5.dp.toPx(), StrokeCap.Square)
    drawLine(Color(0x80000000), center + Offset(2f, 2f), center + Offset(cos(minuteAngle).toFloat() * radius * 0.78f + 2f, sin(minuteAngle).toFloat() * radius * 0.78f + 2f), 3.2.dp.toPx(), StrokeCap.Square)
// Agujas volumétricas (Efecto brillo 3D lineal interno)
    drawBatonHand3D(center, hourAngle, radius * 0.52f, 4.5.dp.toPx())
    drawBatonHand3D(center, minuteAngle, radius * 0.78f, 3.2.dp.toPx())
// Aguja de cronógrafo blanca extremadamente fina
    drawLine(Color.White, center - Offset(cos(secondAngle).toFloat() * radius * 0.18f, sin(secondAngle).toFloat() * radius * 0.18f), center + Offset(cos(secondAngle).toFloat() * radius * 0.84f, sin(secondAngle).toFloat() * radius * 0.84f), 1.2.dp.toPx(), StrokeCap.Round)
    drawCircle(Color.White, 3.5.dp.toPx(), center)
}
private fun DrawScope.drawKhakiXWind(currentTime: LocalDateTime) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = minOf(size.width, size.height) * 0.46f
    val canvas = drawContext.canvas.nativeCanvas
// Fondo fibra de carbono / Patrón técnico oscuro cepillado
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1A1D20), Color(0xFF0A0C0E)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
// Flecha de rumbo técnica naranja en relieve superior
    val arrowTopY = center.y - radius + 4.dp.toPx()
    val arrowPath = android.graphics.Path().apply {
        moveTo(center.x, arrowTopY + 12.dp.toPx())
        lineTo(center.x - 8.dp.toPx(), arrowTopY)
        lineTo(center.x + 8.dp.toPx(), arrowTopY)
        close()
    }
    canvas.drawPath(arrowPath, NativePaint().apply {
        color = Color(0xFFFF6B00).toArgb()
        style = NativePaint.Style.FILL
        isAntiAlias = true
        setShadowLayer(4f, 0f, 2f, Color(0xCC000000).toArgb())
    })
// Bisel técnico tridimensional exterior
    for (i in 0 until 120) {
        val a = (i * 3.0 - 90.0) * PI / 180.0
        val isMajor = i % 10 == 0
        val outer = radius - 15.dp.toPx()
        val inner = if (isMajor) radius - 26.dp.toPx() else radius - 20.dp.toPx()
        drawLine(
            if (isMajor) Color(0xFFFF6B00) else Color(0x55C4C7CC),
            Offset(center.x + cos(a).toFloat() * inner, center.y + sin(a).toFloat() * inner),
            Offset(center.x + cos(a).toFloat() * outer, center.y + sin(a).toFloat() * outer),
            if (isMajor) 2.dp.toPx() else 0.8.dp.toPx()
        )
    }
    val numberPaint = NativePaint().apply {
        color = Color(0xFFC4C7CC).toArgb()
        textSize = radius * 0.12f
        textAlign = NativePaint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
        isAntiAlias = true
        setShadowLayer(2f, 1f, 1f, Color(0xFF000000).toArgb())
    }
    val largeNumberPaint = NativePaint(numberPaint).apply { textSize = radius * 0.16f; typeface = android.graphics.Typeface.DEFAULT_BOLD }
    val hoursToDraw = listOf(1, 2, 4, 5, 7, 8, 10, 11)
    hoursToDraw.forEach { h ->
        val a = (h * 30.0 - 90.0) * PI / 180.0
        val nr = radius - 48.dp.toPx()
        canvas.drawText(h.toString(), center.x + cos(a).toFloat() * nr, center.y + sin(a).toFloat() * nr + numberPaint.textSize * 0.35f, numberPaint)
    }
    canvas.drawText("12", center.x, center.y - radius * 0.58f, largeNumberPaint)
// Doble complicación concéntrica rehundida vertical
    val subRadius = radius * 0.19f
    val subs = listOf(
        Offset(center.x, center.y - radius * 0.28f),
        Offset(center.x, center.y + radius * 0.32f)
    )
    subs.forEach { p ->
        drawCircle(
            brush = Brush.radialGradient(colors = listOf(Color(0xFF101214), Color(0xFF050607)), center = p, radius = subRadius),
            radius = subRadius,
            center = p
        )
        drawCircle(Color(0x26FFFFFF), subRadius, p, style = Stroke(width = 1.dp.toPx()))
    }
// Ventana de fecha rehundida texturizada
    val boxX = center.x + radius * 0.55f
    val boxY = center.y - radius * 0.02f
// Sombra interna del marco de la fecha
    drawRect(Color(0xFF222528), Offset(boxX - 15.dp.toPx(), boxY - 11.dp.toPx()), size = androidx.compose.ui.geometry.Size(30.dp.toPx(), 22.dp.toPx()))
    drawRect(Color.White, Offset(boxX - 14.dp.toPx(), boxY - 10.dp.toPx()), size = androidx.compose.ui.geometry.Size(28.dp.toPx(), 20.dp.toPx()))
    canvas.drawText("8", boxX, boxY + 6.dp.toPx(), NativePaint().apply {
        color = Color.Black.toArgb()
        textSize = radius * 0.08f
        textAlign = NativePaint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        isAntiAlias = true
    })
// Agujas con volumen y punta luminosa de color aviador naranja
    val hourAngle = (currentTime.hour % 12 * 30.0 + currentTime.minute * 0.5 - 90.0) * PI / 180.0
    val minuteAngle = (currentTime.minute * 6.0 - 90.0) * PI / 180.0
    val secondAngle = (currentTime.second * 6.0 - 90.0) * PI / 180.0
// Sombras
    drawLine(Color(0x99000000), center + Offset(2f, 2f), center + Offset(cos(hourAngle).toFloat() * radius * 0.48f + 2f, sin(hourAngle).toFloat() * radius * 0.48f + 2f), 6.dp.toPx(), StrokeCap.Round)
    drawLine(Color(0x99000000), center + Offset(2f, 2f), center + Offset(cos(minuteAngle).toFloat() * radius * 0.72f + 2f, sin(minuteAngle).toFloat() * radius * 0.72f + 2f), 4.2.dp.toPx(), StrokeCap.Round)
    drawBatonHand3D(center, hourAngle, radius * 0.48f, 6.dp.toPx())
    drawBatonHand3D(center, minuteAngle, radius * 0.72f, 4.2.dp.toPx())
// Segundero técnico naranja brillante
    drawLine(Color(0xFFFF6B00), center, center + Offset(cos(secondAngle).toFloat() * radius * 0.82f, sin(secondAngle).toFloat() * radius * 0.82f), 1.6.dp.toPx(), StrokeCap.Round)
    drawCircle(Color(0xFFFF6B00), 3.5.dp.toPx(), center)
}
// ==================== RENDERS MAESTROS AUXILIARES 3D / VOLUMETRÍA ====================
private fun DrawScope.drawFliegerHand3D(center: Offset, angle: Double, length: Float, baseWidth: Float) {
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val perpX = -sinA
    val perpY = cosA
    val midX = center.x + cosA * length * 0.25f
    val midY = center.y + sinA * length * 0.25f
    val tipX = center.x + cosA * length
    val tipY = center.y + sinA * length
// Mitad izquierda de la aguja: Tono metal brillante iluminado
    val pathLeft = Path().apply {
        moveTo(center.x, center.y)
        lineTo(midX - perpX * baseWidth, midY - perpY * baseWidth)
        lineTo(center.x + cosA * length * 0.85f - perpX * (baseWidth * 0.2f), center.y + sinA * length * 0.85f - perpY * (baseWidth * 0.2f))
        lineTo(tipX, tipY)
        close()
    }
// Mitad derecha de la aguja: Tono metal oscurecido por sombra de faceta
    val pathRight = Path().apply {
        moveTo(center.x, center.y)
        lineTo(midX + perpX * baseWidth, midY + perpY * baseWidth)
        lineTo(center.x + cosA * length * 0.85f + perpX * (baseWidth * 0.2f), center.y + sinA * length * 0.85f + perpY * (baseWidth * 0.2f))
        lineTo(tipX, tipY)
        close()
    }
    drawPath(pathLeft, color = Color(0xFFF0F0F2))
    drawPath(pathRight, color = Color(0xFFB0B3B8))
}
private fun DrawScope.drawFliegerHandShadow(center: Offset, angle: Double, length: Float, baseWidth: Float) {
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val perpX = -sinA
    val perpY = cosA
    val shadowOffset = 3.dp.toPx()
    val path = Path().apply {
        moveTo(center.x + shadowOffset, center.y + shadowOffset)
        lineTo(center.x + cosA * length * 0.25f - perpX * baseWidth + shadowOffset, center.y + sinA * length * 0.25f - perpY * baseWidth + shadowOffset)
        lineTo(center.x + cosA * length + shadowOffset, center.y + sinA * length + shadowOffset)
        lineTo(center.x + cosA * length * 0.25f + perpX * baseWidth + shadowOffset, center.y + sinA * length * 0.25f + perpY * baseWidth + shadowOffset)
        close()
    }
    drawPath(path, color = Color(0x66000000))
}
private fun DrawScope.drawBatonHand3D(center: Offset, angle: Double, length: Float, width: Float) {
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val perpX = -sinA
    val perpY = cosA
    val endX = center.x + cosA * length
    val endY = center.y + sinA * length
// Cara iluminada de la varilla bastón
    val pathLeft = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x - perpX * (width / 2f), center.y - perpY * (width / 2f))
        lineTo(endX - perpX * (width / 2f), endY - perpY * (width / 2f))
        lineTo(endX, endY)
        close()
    }
// Cara ensombrecida de la varilla bastón
    val pathRight = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x + perpX * (width / 2f), center.y + perpY * (width / 2f))
        lineTo(endX + perpX * (width / 2f), endY + perpY * (width / 2f))
        lineTo(endX, endY)
        close()
    }
    drawPath(pathLeft, color = Color(0xFFFFFFFF))
    drawPath(pathRight, color = Color(0xFFCCCCCC))
}