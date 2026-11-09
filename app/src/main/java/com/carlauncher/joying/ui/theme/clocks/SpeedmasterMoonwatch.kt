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

@Composable
fun SpeedmasterClockView() {
    var currentTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimestamp = System.currentTimeMillis()
            delay(16) // ~60fps para un barrido continuo ultra fluido
        }
    }

    val instant = Instant.ofEpochMilli(currentTimestamp)
    val time = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {
        drawSpeedmasterEngine(time)
    }
}

private fun DrawScope.drawSpeedmasterEngine(time: ZonedDateTime) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = minOf(size.width, size.height) * 0.46f

    // 1. Textura de la Esfera Principal: Negro asfalto mate con degradado perimetral profundo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1C1E22), Color(0xFF111214), Color(0xFF070809)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Doble punto Super-LumiNova envejecido característico a las 12
    val topMarkerY = center.y - radius + 22.dp.toPx()
    val tritiumColor = Color(0xFFE2DAC2)
    drawCircle(tritiumColor, 2.2.dp.toPx(), Offset(center.x - 6.dp.toPx(), topMarkerY))
    drawCircle(tritiumColor, 2.2.dp.toPx(), Offset(center.x + 6.dp.toPx(), topMarkerY))
    drawLine(tritiumColor, Offset(center.x, topMarkerY - 4.dp.toPx()), Offset(center.x, topMarkerY + 10.dp.toPx()), 3.dp.toPx())

    // 2. Marcas de división minuciosas de cronógrafo clásico (3 subdivisiones por minuto)
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
            if (isFiveMinutes) tritiumColor else Color(0x66FFFFFF),
            Offset(center.x + cos(a).toFloat() * inner, center.y + sin(a).toFloat() * inner),
            Offset(center.x + cos(a).toFloat() * outer, center.y + sin(a).toFloat() * outer),
            if (isFiveMinutes) 2.5.dp.toPx() else 0.8.dp.toPx(), StrokeCap.Square
        )
    }

    val subRadius = radius * 0.23f
    val leftSubCenter = Offset(center.x - radius * 0.38f, center.y)  // Posición 9h
    val rightSubCenter = Offset(center.x + radius * 0.38f, center.y) // Posición 3h

    // 3. Subesfera Izquierda (9 en punto): Indicador de 12 Meses
    drawCircle(Color(0xFF090A0B), subRadius, leftSubCenter)
    drawCircle(Color(0x1AFFFFFF), subRadius, leftSubCenter, style = Stroke(width = 1.dp.toPx()))
    drawCircle(Color(0x33000000), subRadius - 1.dp.toPx(), leftSubCenter, style = Stroke(width = 2.dp.toPx()))

    for (m in 1..12) {
        val a = (m * 30.0 - 90.0) * PI / 180.0
        val isQuarter = m % 3 == 0
        val length = if (isQuarter) subRadius * 0.22f else subRadius * 0.12f

        drawLine(
            color = if (isQuarter) Color.White else Color(0x80FFFFFF),
            start = Offset(leftSubCenter.x + cos(a).toFloat() * (subRadius - length), leftSubCenter.y + sin(a).toFloat() * (subRadius - length)),
            end = Offset(leftSubCenter.x + cos(a).toFloat() * (subRadius - 2.dp.toPx()), leftSubCenter.y + sin(a).toFloat() * (subRadius - 2.dp.toPx())),
            strokeWidth = if (isQuarter) 1.5.dp.toPx() else 1.dp.toPx()
        )
    }
    // Aguja del mes actual (Fija apuntando al mes en curso)
    val monthAngle = (time.monthValue * 30.0 - 90.0) * PI / 180.0
    drawLine(Color.White, leftSubCenter, leftSubCenter + Offset(cos(monthAngle).toFloat() * subRadius * 0.72f, sin(monthAngle).toFloat() * subRadius * 0.72f), 1.8.dp.toPx(), StrokeCap.Round)
    drawCircle(Color(0xFF0F1012), 2.dp.toPx(), leftSubCenter)

    // 4. Subesfera Derecha (3 en punto): Indicador de 31 Días del mes
    drawCircle(Color(0xFF090A0B), subRadius, rightSubCenter)
    drawCircle(Color(0x1AFFFFFF), subRadius, rightSubCenter, style = Stroke(width = 1.dp.toPx()))
    drawCircle(Color(0x33000000), subRadius - 1.dp.toPx(), rightSubCenter, style = Stroke(width = 2.dp.toPx()))

    for (d in 1..31) {
        val a = (d * (360.0 / 31.0) - 90.0) * PI / 180.0
        val isMajor = d == 1 || d == 10 || d == 20 || d == 30
        val length = if (isMajor) subRadius * 0.24f else subRadius * 0.12f

        drawLine(
            color = if (isMajor) Color.White else Color(0x66FFFFFF),
            start = Offset(rightSubCenter.x + cos(a).toFloat() * (subRadius - length), rightSubCenter.y + sin(a).toFloat() * (subRadius - length)),
            end = Offset(rightSubCenter.x + cos(a).toFloat() * (subRadius - 2.dp.toPx()), rightSubCenter.y + sin(a).toFloat() * (subRadius - 2.dp.toPx())),
            strokeWidth = if (isMajor) 1.5.dp.toPx() else 1.dp.toPx()
        )
    }
    // Aguja del día actual (Fija apuntando al día de hoy)
    val dayAngle = (time.dayOfMonth * (360.0 / 31.0) - 90.0) * PI / 180.0
    drawLine(Color.White, rightSubCenter, rightSubCenter + Offset(cos(dayAngle).toFloat() * subRadius * 0.72f, sin(dayAngle).toFloat() * subRadius * 0.72f), 1.8.dp.toPx(), StrokeCap.Round)
    drawCircle(Color(0xFF0F1012), 2.dp.toPx(), rightSubCenter)

    // 5. Motor de Tiempo Continuo (Cálculo mecánico fraccionado)
    val millisecond = time.nano / 1000000.0
    val secondContinuous = time.second + (millisecond / 1000.0)
    val minuteContinuous = time.minute + (secondContinuous / 60.0)
    val hourContinuous = (time.hour % 12) + (minuteContinuous / 60.0)

    val hourAngle = (hourContinuous * 30.0 - 90.0) * PI / 180.0
    val minuteAngle = (minuteContinuous * 6.0 - 90.0) * PI / 180.0
    val secondAngle = (secondContinuous * 6.0 - 90.0) * PI / 180.0

    // Sombras proyectadas de las manecillas principales
    drawLine(Color(0x66000000), center + Offset(2f, 2f), center + Offset(cos(hourAngle).toFloat() * radius * 0.52f + 2f, sin(hourAngle).toFloat() * radius * 0.52f + 2f), 4.5.dp.toPx(), StrokeCap.Square)
    drawLine(Color(0x66000000), center + Offset(2f, 2f), center + Offset(cos(minuteAngle).toFloat() * radius * 0.78f + 2f, sin(minuteAngle).toFloat() * radius * 0.78f + 2f), 3.2.dp.toPx(), StrokeCap.Square)

    // Manecillas lacadas tridimensionales tipo Bastón
    drawBatonHand3D(center, hourAngle, radius * 0.52f, 4.5.dp.toPx())
    drawBatonHand3D(center, minuteAngle, radius * 0.78f, 3.2.dp.toPx())

    // Aguja segundera de aguja fina recta ininterrumpida
    val secondStart = center - Offset(cos(secondAngle).toFloat() * radius * 0.18f, sin(secondAngle).toFloat() * radius * 0.18f)
    val secondEnd = center + Offset(cos(secondAngle).toFloat() * radius * 0.84f, sin(secondAngle).toFloat() * radius * 0.84f)
    drawLine(Color(0x4D000000), secondStart + Offset(2f, 1f), secondEnd + Offset(2f, 1f), 1.2.dp.toPx(), StrokeCap.Round)
    drawLine(Color.White, secondStart, secondEnd, 1.2.dp.toPx(), StrokeCap.Round)

    // Remache central metálico
    drawCircle(Color.White, 3.5.dp.toPx(), center)
    drawCircle(Color(0xFF111214), 1.2.dp.toPx(), center)
}

private fun DrawScope.drawBatonHand3D(center: Offset, angle: Double, length: Float, width: Float) {
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val perpX = -sinA
    val perpY = cosA

    val endX = center.x + cosA * length
    val endY = center.y + sinA * length

    val pathLeft = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x - perpX * (width / 2f), center.y - perpY * (width / 2f))
        lineTo(endX - perpX * (width / 2f), endY - perpY * (width / 2f))
        lineTo(endX, endY)
        close()
    }
    val pathRight = Path().apply {
        moveTo(center.x, center.y)
        lineTo(center.x + perpX * (width / 2f), center.y + perpY * (width / 2f))
        lineTo(endX + perpX * (width / 2f), endY + perpY * (width / 2f))
        lineTo(endX, endY)
        close()
    }

    drawPath(pathLeft, color = Color(0xFFFFFFFF))
    drawPath(pathRight, color = Color(0xFFD0D0D5))
}
