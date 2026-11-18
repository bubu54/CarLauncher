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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PilotClassicClockView() {
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
        drawIwcBigPilotEngine(time)
    }
}

private fun DrawScope.drawIwcBigPilotEngine(time: ZonedDateTime) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val outerCaseRadius = minOf(size.width, size.height) * 0.48f
    val bezelRadius = outerCaseRadius * 0.94f
    val dialRadius = bezelRadius * 0.92f
    val canvas = drawContext.canvas.nativeCanvas

    // 1. Caja y bisel metálico de acero
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                Color(0xFF8E9399),
                Color(0xFFE3E7EC),
                Color(0xFF6B7075),
                Color(0xFFD5D9DF),
                Color(0xFF5A5E63),
                Color(0xFFE3E7EC),
                Color(0xFF8E9399)
            ),
            center = center
        ),
        radius = outerCaseRadius,
        center = center
    )

    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFF4A4E54), Color(0xFFD0D4DA)),
            start = Offset(center.x - bezelRadius, center.y - bezelRadius),
            end = Offset(center.x + bezelRadius, center.y + bezelRadius)
        ),
        radius = bezelRadius,
        center = center
    )

    // 2. Esfera negra mate con viñeta radial
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF1F2124),
                Color(0xFF141517),
                Color(0xFF0A0B0C)
            ),
            center = center,
            radius = dialRadius
        ),
        radius = dialRadius,
        center = center
    )

    // Anillo interior (Rehaut)
    drawCircle(
        color = Color(0x55000000),
        radius = dialRadius - 2.dp.toPx(),
        center = center,
        style = Stroke(width = 4.dp.toPx())
    )
    drawCircle(
        color = Color(0x33FFFFFF),
        radius = dialRadius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // 3. Índices de minutos y horas
    for (i in 0 until 60) {
        if (i == 0) continue
        val angle = (i * 6.0 - 90.0) * PI / 180.0
        val isMajor = i % 5 == 0
        val outer = dialRadius - 5.dp.toPx()
        val inner = if (isMajor) dialRadius - 18.dp.toPx() else dialRadius - 11.dp.toPx()

        drawLine(
            color = Color(0xFFF5F6F7),
            start = Offset(center.x + cos(angle).toFloat() * inner, center.y + sin(angle).toFloat() * inner),
            end = Offset(center.x + cos(angle).toFloat() * outer, center.y + sin(angle).toFloat() * outer),
            strokeWidth = if (isMajor) 3.4.dp.toPx() else 1.3.dp.toPx(),
            cap = StrokeCap.Butt
        )
    }

    // Triángulo clásico de aviador a las 12
    val triangleTopY = center.y - dialRadius + 6.dp.toPx()
    val triangleHeight = 14.dp.toPx()
    val triangleHalfWidth = 9.dp.toPx()
    val trianglePath = Path().apply {
        moveTo(center.x, triangleTopY)
        lineTo(center.x - triangleHalfWidth, triangleTopY + triangleHeight)
        lineTo(center.x + triangleHalfWidth, triangleTopY + triangleHeight)
        close()
    }
    drawPath(path = trianglePath, color = Color(0xFFF5F6F7))
    drawCircle(Color(0xFFF5F6F7), 2.4.dp.toPx(), Offset(center.x - 14.dp.toPx(), triangleTopY + 7.dp.toPx()))
    drawCircle(Color(0xFFF5F6F7), 2.4.dp.toPx(), Offset(center.x + 14.dp.toPx(), triangleTopY + 7.dp.toPx()))

    // 4. Textos de la esfera
    val iwcLogoPaint = NativePaint().apply {
        color = Color(0xFFF2F2F2).toArgb()
        textSize = dialRadius * 0.145f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        letterSpacing = 0.08f
        isAntiAlias = true
    }
    canvas.drawText("IWC", center.x, center.y - dialRadius * 0.30f, iwcLogoPaint)

    val schaffhausenPaint = NativePaint().apply {
        color = Color(0xFFDCDCDC).toArgb()
        textSize = dialRadius * 0.043f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        letterSpacing = 0.22f
        isAntiAlias = true
    }
    canvas.drawText("SCHAFFHAUSEN", center.x, center.y - dialRadius * 0.22f, schaffhausenPaint)

    val automaticPaint = NativePaint().apply {
        color = Color(0xFFE0E0E0).toArgb()
        textSize = dialRadius * 0.052f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        letterSpacing = 0.15f
        isAntiAlias = true
    }
    canvas.drawText("AUTOMATIC", center.x, center.y + dialRadius * 0.34f, automaticPaint)

    val swissPaint = NativePaint().apply {
        color = Color(0xFFB0B3B8).toArgb()
        textSize = dialRadius * 0.036f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        letterSpacing = 0.12f
        isAntiAlias = true
    }
    canvas.drawText("SWISS", center.x - dialRadius * 0.15f, center.y + dialRadius * 0.87f, swissPaint)
    canvas.drawText("MADE", center.x + dialRadius * 0.15f, center.y + dialRadius * 0.87f, swissPaint)

    // 5. Subesfera de reserva de marcha 7 DAYS
    val subCenter = Offset(center.x + dialRadius * 0.43f, center.y)
    val subRadius = dialRadius * 0.26f

    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF050506), Color(0xFF3A3D42)),
            start = Offset(subCenter.x - subRadius, subCenter.y - subRadius),
            end = Offset(subCenter.x + subRadius, subCenter.y + subRadius)
        ),
        radius = subRadius + 1.5.dp.toPx(),
        center = subCenter
    )
    drawCircle(
        color = Color(0xFF111214),
        radius = subRadius,
        center = subCenter
    )

    for (r in 1..6) {
        drawCircle(
            color = Color(0x14FFFFFF),
            radius = subRadius * (r / 7f),
            center = subCenter,
            style = Stroke(width = 0.6.dp.toPx())
        )
    }

    val daysLabelPaint = NativePaint().apply {
        color = Color(0xFFE0E0E0).toArgb()
        textSize = subRadius * 0.18f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        letterSpacing = 0.12f
        isAntiAlias = true
    }
    canvas.drawText("DAYS", subCenter.x, subCenter.y - subRadius * 0.56f, daysLabelPaint)

    val subNumPaint = NativePaint().apply {
        color = Color(0xFFF0F0F0).toArgb()
        textSize = subRadius * 0.21f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
    }
    val powerAngles = listOf(225.0, 185.0, 140.0, 90.0, 40.0, 355.0, 315.0)
    for (day in 1..7) {
        val rad = powerAngles[day - 1] * PI / 180.0
        val numR = subRadius * 0.72f
        val nx = subCenter.x + cos(rad).toFloat() * numR
        val ny = subCenter.y + sin(rad).toFloat() * numR + subNumPaint.textSize * 0.35f

        if (day == 1) {
            subNumPaint.color = Color(0xFFD92B2B).toArgb()
            canvas.drawText("1", nx, ny, subNumPaint)
            subNumPaint.color = Color(0xFFF0F0F0).toArgb()
        } else {
            canvas.drawText(day.toString(), nx, ny, subNumPaint)
        }
    }

    val powerAngle = 335.0 * PI / 180.0
    drawLine(
        color = Color(0xFF000000),
        start = subCenter,
        end = Offset(
            subCenter.x + cos(powerAngle).toFloat() * subRadius * 0.62f,
            subCenter.y + sin(powerAngle).toFloat() * subRadius * 0.62f + 2.dp.toPx()
        ),
        strokeWidth = 2.4.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFF5F5F5),
        start = subCenter,
        end = Offset(
            subCenter.x + cos(powerAngle).toFloat() * subRadius * 0.62f,
            subCenter.y + sin(powerAngle).toFloat() * subRadius * 0.62f
        ),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(Color(0xFF888C91), 3.dp.toPx(), subCenter)

    // 6. Números arábigos principales
    val numberPaint = NativePaint().apply {
        color = Color(0xFFF7F8FA).toArgb()
        textSize = dialRadius * 0.165f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        isAntiAlias = true
        setShadowLayer(4f, 0f, 3f, Color(0xAA000000).toArgb())
    }
    for (h in 1..11) {
        if (h == 3 || h == 6) continue
        val angle = (h * 30.0 - 90.0) * PI / 180.0
        val numberRadius = dialRadius - 35.dp.toPx()
        val x = center.x + cos(angle).toFloat() * numberRadius
        val y = center.y + sin(angle).toFloat() * numberRadius + numberPaint.textSize * 0.36f
        canvas.drawText(h.toString(), x, y, numberPaint)
    }
// 7. Ventana de fecha a las 6
    val dateCenter = Offset(center.x, center.y + dialRadius * 0.62f)
    val dateW = 24.dp.toPx()
    val dateH = 28.dp.toPx()
    drawRoundRect(
        color = Color(0xFF3E4247),
        topLeft = Offset(dateCenter.x - dateW / 2f - 1.5.dp.toPx(), dateCenter.y - dateH / 2f - 1.5.dp.toPx()),
        size = Size(dateW + 3.dp.toPx(), dateH + 3.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFF0B0C0E),
        topLeft = Offset(dateCenter.x - dateW / 2f, dateCenter.y - dateH / 2f),
        size = Size(dateW, dateH),
        cornerRadius = CornerRadius(1.5.dp.toPx())
    )
    val dateTextPaint = NativePaint().apply {
        color = Color(0xFFF5F5F5).toArgb()
        textSize = dialRadius * 0.105f
        textAlign = NativePaint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        isAntiAlias = true
    }
    canvas.drawText(
        time.format(DateTimeFormatter.ofPattern("d")),
        dateCenter.x,
        dateCenter.y + dateTextPaint.textSize * 0.36f,
        dateTextPaint
    )
// 8. Agujas
    val millisecond = time.nano / 1_000_000.0
    val secondContinuous = time.second + (millisecond / 1000.0)
    val minuteContinuous = time.minute + (secondContinuous / 60.0)
    val hourContinuous = (time.hour % 12) + (minuteContinuous / 60.0)
    val hourAngle = (hourContinuous * 30.0 - 90.0) * PI / 180.0
    val minuteAngle = (minuteContinuous * 6.0 - 90.0) * PI / 180.0
    val secondAngle = (secondContinuous * 6.0 - 90.0) * PI / 180.0
    drawIwcFliegerHand(
        center = center,
        angle = hourAngle,
        length = dialRadius * 0.54f,
        tailLength = dialRadius * 0.12f,
        width = 11.dp.toPx()
    )
    drawIwcFliegerHand(
        center = center,
        angle = minuteAngle,
        length = dialRadius * 0.84f,
        tailLength = dialRadius * 0.15f,
        width = 9.dp.toPx()
    )
    val secTip = Offset(
        center.x + cos(secondAngle).toFloat() * (dialRadius * 0.88f),
        center.y + sin(secondAngle).toFloat() * (dialRadius * 0.88f)
    )
    val secTail = Offset(
        center.x - cos(secondAngle).toFloat() * (dialRadius * 0.22f),
        center.y - sin(secondAngle).toFloat() * (dialRadius * 0.22f)
    )
    drawLine(
        color = Color(0x77000000),
        start = secTail.copy(y = secTail.y + 4.dp.toPx()),
        end = secTip.copy(y = secTip.y + 4.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFFF5F6F7),
        start = secTail,
        end = secTip,
        strokeWidth = 1.8.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawCircle(Color(0xFF1A1C1E), 6.5.dp.toPx(), center)
    drawCircle(Color(0xFFE5E7EB), 5.dp.toPx(), center)
    drawCircle(Color(0xFF2B2E33), 2.dp.toPx(), center)
}
private fun DrawScope.drawIwcFliegerHand(
    center: Offset,
    angle: Double,
    length: Float,
    tailLength: Float,
    width: Float
) {
    val shadowOffset = 5.dp.toPx()
    val shadowPath = createFliegerHandPath(
        center = Offset(center.x, center.y + shadowOffset),
        angle = angle,
        length = length,
        tailLength = tailLength,
        width = width
    )
    drawPath(
        path = shadowPath,
        color = Color(0x88000000)
    )
    val mainPath = createFliegerHandPath(
        center = center,
        angle = angle,
        length = length,
        tailLength = tailLength,
        width = width
    )
    drawPath(
        path = mainPath,
        color = Color(0xFFFAFBFD)
    )
    drawPath(
        path = mainPath,
        color = Color(0xFF2D3136),
        style = Stroke(width = 1.4.dp.toPx())
    )
}
private fun createFliegerHandPath(
    center: Offset,
    angle: Double,
    length: Float,
    tailLength: Float,
    width: Float
): Path {
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val perpCos = cos(angle + PI / 2.0).toFloat()
    val perpSin = sin(angle + PI / 2.0).toFloat()
    val tail = Offset(center.x - cosA * tailLength, center.y - sinA * tailLength)
    val shoulderDist = length * 0.25f
    val tipShoulderDist = length * 0.78f
    val leftShoulder = Offset(
        center.x + cosA * shoulderDist + perpCos * (width * 0.42f),
        center.y + sinA * shoulderDist + perpSin * (width * 0.42f)
    )
    val rightShoulder = Offset(
        center.x + cosA * shoulderDist - perpCos * (width * 0.42f),
        center.y + sinA * shoulderDist - perpSin * (width * 0.42f)
    )
    val leftWide = Offset(
        center.x + cosA * tipShoulderDist + perpCos * (width * 0.5f),
        center.y + sinA * tipShoulderDist + perpSin * (width * 0.5f)
    )
    val rightWide = Offset(
        center.x + cosA * tipShoulderDist - perpCos * (width * 0.5f),
        center.y + sinA * tipShoulderDist - perpSin * (width * 0.5f)
    )
    val tip = Offset(center.x + cosA * length, center.y + sinA * length)
    return Path().apply {
        moveTo(tail.x, tail.y)
        lineTo(leftShoulder.x, leftShoulder.y)
        lineTo(leftWide.x, leftWide.y)
        lineTo(tip.x, tip.y)
        lineTo(rightWide.x, rightWide.y)
        lineTo(rightShoulder.x, rightShoulder.y)
        close()
    }
}
