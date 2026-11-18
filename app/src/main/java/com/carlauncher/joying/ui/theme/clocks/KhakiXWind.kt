package com.carlauncher.joying.ui.theme.clocks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint as NativePaint

@Composable
fun KhakiXWindClockView() {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(200) // el segundero marca cada segundo; 200 ms es suficiente
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {
        drawKhakiXWind(currentTime)
    }
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
