
package com.carlauncher.joying.ui

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
import androidx.compose.ui.graphics.StrokeCap
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

@Composable
fun PilotClassicClock() {

    var currentTime by remember {
        mutableStateOf(LocalDateTime.now())
    }

    // Actualización del reloj cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    val navy = androidx.compose.ui.graphics.Color(0xFF071426)
    val dialBlue = androidx.compose.ui.graphics.Color(0xFF102C49)
    val luminous = androidx.compose.ui.graphics.Color(0xFFBFE8FF)
    val accent = androidx.compose.ui.graphics.Color(0xFF36AFFF)
    val muted = androidx.compose.ui.graphics.Color(0xFF6688A7)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
    ) {

        val center = Offset(
            x = size.width / 2f,
            y = size.height / 2f
        )

        val radius = minOf(size.width, size.height) * 0.45f

        // ESFERA
        drawCircle(
            color = navy,
            radius = radius,
            center = center
        )

        drawCircle(
            color = accent.copy(alpha = 0.7f),
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        drawCircle(
            color = muted.copy(alpha = 0.5f),
            radius = radius - 8.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // ÍNDICES Y NUMERACIÓN
        val nativeCanvas = drawContext.canvas.nativeCanvas

        val numberPaint = NativePaint().apply {
            color = luminous.toArgb()
            textSize = radius * 0.14f
            textAlign = NativePaint.Align.CENTER
            typeface = android.graphics.Typeface.create(
                "sans-serif",
                android.graphics.Typeface.BOLD
            )
            isAntiAlias = true
        }

        val trianglePath = android.graphics.Path().apply {
            moveTo(center.x, center.y - radius + 12.dp.toPx())
            lineTo(center.x - 10.dp.toPx(), center.y - radius + 30.dp.toPx())
            lineTo(center.x + 10.dp.toPx(), center.y - radius + 30.dp.toPx())
            close()
        }

        nativeCanvas.drawPath(
            trianglePath,
            NativePaint().apply {
                color = accent.toArgb()
                style = NativePaint.Style.FILL
                isAntiAlias = true
            }
        )

        for (i in 0 until 60) {

            val angle = (i * 6.0 - 90.0) * PI / 180.0

            val isMajor = i % 5 == 0

            val outer = radius - 16.dp.toPx()
            val inner = if (isMajor) {
                radius - 36.dp.toPx()
            } else {
                radius - 24.dp.toPx()
            }

            val start = Offset(
                center.x + cos(angle).toFloat() * inner,
                center.y + sin(angle).toFloat() * inner
            )

            val end = Offset(
                center.x + cos(angle).toFloat() * outer,
                center.y + sin(angle).toFloat() * outer
            )

            drawLine(
                color = if (isMajor) luminous else muted,
                start = start,
                end = end,
                strokeWidth = if (isMajor) 3.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )

            if (isMajor) {

                val number = if (i == 0) 12 else i / 5

                val numberRadius = radius - 59.dp.toPx()

                val x = center.x +
                        cos(angle).toFloat() * numberRadius

                val y = center.y +
                        sin(angle).toFloat() * numberRadius +
                        numberPaint.textSize * 0.35f

                nativeCanvas.drawText(
                    number.toString(),
                    x,
                    y,
                    numberPaint
                )
            }
        }

        // SUBESFERA INFERIOR
        val subCenter = Offset(
            center.x,
            center.y + radius * 0.55f
        )

        drawCircle(
            color = dialBlue,
            radius = radius * 0.16f,
            center = subCenter,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // AGUJAS
        val hour = currentTime.hour % 12
        val minute = currentTime.minute
        val second = currentTime.second

        val hourAngle = (hour * 30.0 + minute * 0.5 - 90.0) * PI / 180.0
        val minuteAngle = (minute * 6.0 - 90.0) * PI / 180.0
        val secondAngle = (second * 6.0 - 90.0) * PI / 180.0

        fun handEnd(angle: Double, length: Float): Offset {
            return Offset(
                center.x + cos(angle).toFloat() * length,
                center.y + sin(angle).toFloat() * length
            )
        }

        // Aguja horaria
        drawLine(
            color = luminous.copy(alpha = 0.3f),
            start = center,
            end = handEnd(hourAngle, radius * 0.47f),
            strokeWidth = 11.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = luminous,
            start = center,
            end = handEnd(hourAngle, radius * 0.47f),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Aguja minutera
        drawLine(
            color = luminous.copy(alpha = 0.3f),
            start = center,
            end = handEnd(minuteAngle, radius * 0.68f),
            strokeWidth = 9.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawLine(
            color = luminous,
            start = center,
            end = handEnd(minuteAngle, radius * 0.68f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Segundero
        drawLine(
            color = accent,
            start = handEnd(secondAngle + PI, radius * 0.18f),
            end = handEnd(secondAngle, radius * 0.78f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Eje central
        drawCircle(
            color = accent,
            radius = 6.dp.toPx(),
            center = center
        )

        drawCircle(
            color = luminous,
            radius = 2.5.dp.toPx(),
            center = center
        )

        // FECHA
        val datePaint = NativePaint().apply {
            color = luminous.toArgb()
            textSize = radius * 0.09f
            textAlign = NativePaint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        nativeCanvas.drawText(
            currentTime.format(DateTimeFormatter.ofPattern("dd")),
            center.x + radius * 0.48f,
            center.y + radius * 0.48f,
            datePaint
        )
    }
}
