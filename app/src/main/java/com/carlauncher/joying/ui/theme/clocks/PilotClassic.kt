package com.carlauncher.joying.ui.theme.clocks

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
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint as NativePaint
import java.time.format.DateTimeFormatter

@Composable
fun PilotClassicClockView() {
    var currentTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimestamp = System.currentTimeMillis()
            delay(16) // Mantiene el refresco fluido a ~60fps para movimiento automático continuo
        }
    }

    val instant = Instant.ofEpochMilli(currentTimestamp)
    val time = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {
        drawPilotClassicEngine(time)
    }
}

private fun DrawScope.drawPilotClassicEngine(time: ZonedDateTime) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = minOf(size.width, size.height) * 0.46f
    val canvas = drawContext.canvas.nativeCanvas

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1E2124), Color(0xFF111214), Color(0xFF090A0B)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    drawCircle(
        color = Color(0xFF3A3F44),
        radius = radius,
        center = center,
        style = Stroke(width = 1.5.dp.toPx())
    )

    val numberPaint = NativePaint().apply {
        color = Color(0xFFFFFFFF).toArgb()
        textSize = radius * 0.15f
        textAlign = NativePaint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(3f, 1f, 2f, Color(0x99000000).toArgb())
    }

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

    for (h in 1..11) {
        if (h == 6) continue
        val angle = (h * 30.0 - 90.0) * PI / 180.0
        val numberRadius = radius - 42.dp.toPx()
        val x = center.x + cos(angle).toFloat() * numberRadius
        val y = center.y + sin(angle).toFloat() * numberRadius + numberPaint.textSize * 0.35f
        canvas.drawText(h.toString(), x, y, numberPaint)
    }

    val monthCenter = Offset(center.x + radius * 0.42f, center.y)
    val monthRadius = radius * 0.24f

    drawCircle(
        brush = Brush.radialGradient(colors = listOf(Color(0xFF141517), Color(0xFF050607)), center = monthCenter, radius = monthRadius),
        radius = monthRadius,
        center = monthCenter
    )
    drawCircle(Color(0x33FFFFFF), monthRadius, monthCenter, style = Stroke(width = 1.dp.toPx()))

    val subNumberPaint = NativePaint().apply {
        color = Color(0xCCFFFFFF).toArgb()
        textSize = monthRadius * 0.23f
        textAlign = NativePaint.Align.CENTER
        typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
        isAntiAlias = true
    }

    for (m in 1..12) {
        val angle = (m * 30.0 - 90.0) * PI / 180.0
        val outer = monthRadius - 2.dp.toPx()
        val inner = monthRadius - 6.dp.toPx()

        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(monthCenter.x + cos(angle).toFloat() * inner, monthCenter.y + sin(angle).toFloat() * inner),
            end = Offset(monthCenter.x + cos(angle).toFloat() * outer, monthCenter.y + sin(angle).toFloat() * outer),
            strokeWidth = 1.dp.toPx()
        )

        if (m % 3 == 0) {
            val nr = monthRadius - 13.dp.toPx()
            val mx = monthCenter.x + cos(angle).toFloat() * nr
            val my = monthCenter.y + sin(angle).toFloat() * nr + subNumberPaint.textSize * 0.35f
            canvas.drawText(m.toString(), mx, my, subNumberPaint)
        }
    }

    val currentMonth = time.monthValue
    val monthAngle = (currentMonth * 30.0 - 90.0) * PI / 180.0
    drawLine(
        color = Color(0xFFDCDCDC),
        start = monthCenter,
        end = Offset(monthCenter.x + cos(monthAngle).toFloat() * monthRadius * 0.7f, monthCenter.y + sin(monthAngle).toFloat() * monthRadius * 0.7f),
        strokeWidth = 1.8.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(Color(0xFF1E2124), 2.5.dp.toPx(), monthCenter)

    val dateRectCenter = Offset(center.x, center.y + radius * 0.52f)

    drawRect(
        color = Color(0xFF1D2023),
        topLeft = Offset(dateRectCenter.x - 17.dp.toPx(), dateRectCenter.y - 12.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(34.dp.toPx(), 24.dp.toPx())
    )
    drawRect(
        color = Color.White,
        topLeft = Offset(dateRectCenter.x - 15.dp.toPx(), dateRectCenter.y - 10.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(30.dp.toPx(), 20.dp.toPx())
    )
    val dateTextPaint = NativePaint().apply {
        color = Color.Black.toArgb()
        textSize = radius * 0.085f
        textAlign = NativePaint.Align.CENTER
        typeface = android.graphics.Typeface.create("mono", android.graphics.Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText(
        time.format(DateTimeFormatter.ofPattern("d")),
        dateRectCenter.x,
        dateRectCenter.y + 6.dp.toPx(),
        dateTextPaint
    )

    val millisecond = time.nano / 1000000.0
    val secondContinuous = time.second + (millisecond / 1000.0)
    val minuteContinuous = time.minute + (secondContinuous / 60.0)
    val hourContinuous = (time.hour % 12) + (minuteContinuous / 60.0)

    val hourAngle = (hourContinuous * 30.0 - 90.0) * PI / 180.0
    val minuteAngle = (minuteContinuous * 6.0 - 90.0) * PI / 180.0
    val secondAngle = (secondContinuous * 6.0 - 90.0) * PI / 180.0

    drawFliegerHandShadow(center, hourAngle, radius * 0.50f, 9.dp.toPx())
    drawFliegerHandShadow(center, minuteAngle, radius * 0.76f, 6.5.dp.toPx())

    drawFliegerHand3D(center, hourAngle, radius * 0.50f, 9.dp.toPx())
    drawFliegerHand3D(center, minuteAngle, radius * 0.76f, 6.5.dp.toPx())

    val secondStart = center - Offset(cos(secondAngle).toFloat() * radius * 0.15f, sin(secondAngle).toFloat() * radius * 0.15f)
    val secondEnd = center + Offset(cos(secondAngle).toFloat() * radius * 0.85f, sin(secondAngle).toFloat() * radius * 0.85f)

    drawLine(Color(0x4D000000), secondStart + Offset(2f, 2f), secondEnd + Offset(2f, 2f), 1.5.dp.toPx(), StrokeCap.Round)
    drawLine(Color(0xFFECECEC), secondStart, secondEnd, 1.5.dp.toPx(), StrokeCap.Round)

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

private fun DrawScope.drawFliegerHand3D(center: Offset, angle: Double, length: Float, baseWidth: Float) {
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val perpX = -sinA
    val perpY = cosA

    val midX = center.x + cosA * length * 0.25f
    val midY = center.y + sinA * length * 0.25f
    val tipX = center.x + cosA * length
    val tipY = center.y + sinA * length

    val pathLeft = Path().apply {
        moveTo(center.x, center.y)
        lineTo(midX - perpX * baseWidth, midY - perpY * baseWidth)
        lineTo(center.x + cosA * length * 0.85f - perpX * (baseWidth * 0.2f), center.y + sinA * length * 0.85f - perpY * (baseWidth * 0.2f))
        lineTo(tipX, tipY)
        close()
    }
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