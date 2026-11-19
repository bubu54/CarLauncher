package com.carlauncher.joying.ui.theme.themes

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import java.util.Random

/** Generadores de texturas (se crean una vez y se reutilizan). */
internal object Textures {

    private val carbonCache = HashMap<Int, Bitmap>()
    private var brushedCache: Bitmap? = null

    /**
     * Mosaico de fibra de carbono en sarga diagonal 2x2 (Twill 45°) con trama amplia:
     * Genera el patrón escalonado en relieve de bloques rectangulares inclinados
     * con iluminación direccional y repetición continua sin costuras.
     * [cell] = tamaño base de la celda en píxeles.
     */
    fun carbonTile(cell: Int): Bitmap = carbonCache.getOrPut(cell) {
        // Aumentamos el multiplicador a 2.8f para duplicar el tamaño de los bloques
        val step = (cell * 3.8f).coerceAtLeast(16f)
        val tileSize = (step * 4f).toInt()
        val bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFF161719.toInt())

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val sub = step / 2f

        // Degradado en relieve para hebras iluminadas desde arriba-izquierda
        val lightColors = intArrayOf(
            0xFF45474C.toInt(),
            0xFF34363A.toInt(),
            0xFF212225.toInt(),
            0xFF141517.toInt()
        )
        // Degradado más profundo para hebras en sombra perpendicular
        val darkColors = intArrayOf(
            0xFF323438.toInt(),
            0xFF232528.toInt(),
            0xFF17181B.toInt(),
            0xFF0F1012.toInt()
        )
        val stops = floatArrayOf(0f, 0.25f, 0.75f, 1f)

        // Rotamos el lienzo -45° alrededor del centro del mosaico
        canvas.save()
        canvas.rotate(90f, tileSize / 2f, tileSize / 2f)

        // Dibujamos con margen extra (-4..11) para cubrir las esquinas tras la rotación
        for (row in -4 until 12) {
            for (col in -4 until 12) {
                val phase = (col + row).mod(4)

                if (phase == 0) {
                    val x0 = col * sub
                    val y0 = row * sub
                    val w = sub * 2f
                    val h = sub

                    paint.shader = LinearGradient(
                        x0, y0, x0, y0 + h,
                        lightColors, stops, Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(x0 + 1f, y0 + 1f, x0 + w - 1f, y0 + h - 1f, paint)
                } else if (phase == 2) {
                    val x0 = col * sub
                    val y0 = row * sub
                    val w = sub
                    val h = sub * 2f

                    paint.shader = LinearGradient(
                        x0, y0, x0 + w, y0,
                        darkColors, stops, Shader.TileMode.CLAMP
                    )
                    canvas.drawRect(x0 + 1f, y0 + 1f, x0 + w - 1f, y0 + h - 1f, paint)
                }
            }
        }

        canvas.restore()
        bitmap
    }

    /**
     * Estrías horizontales finas del aluminio cepillado (sin color: solo blanco y
     * negro translúcidos, para superponer sobre el degradado metálico).
     * El mosaico es continuo en horizontal.
     */
    fun brushedTile(): Bitmap {
        brushedCache?.let { return it }

        val w = 192
        val h = 96
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val random = Random(42)

        for (y in 0 until h) {
            val alpha = random.nextInt(30)
            paint.color = if (random.nextBoolean()) {
                android.graphics.Color.argb(alpha, 255, 255, 255)
            } else {
                android.graphics.Color.argb(alpha, 0, 0, 0)
            }
            canvas.drawRect(0f, y.toFloat(), w.toFloat(), y + 1f, paint)
        }

        for (i in 0 until 140) {
            val y = random.nextInt(h).toFloat()
            val x = random.nextInt(w).toFloat()
            val length = (random.nextInt(w / 2) + 20).toFloat()
            val alpha = 14 + random.nextInt(34)

            paint.color = if (random.nextInt(3) == 0) {
                android.graphics.Color.argb(alpha, 0, 0, 0)
            } else {
                android.graphics.Color.argb(alpha, 255, 255, 255)
            }

            canvas.drawRect(x, y, x + length, y + 1f, paint)
            if (x + length > w) {
                canvas.drawRect(x - w, y, x + length - w, y + 1f, paint)
            }
        }

        brushedCache = bitmap
        return bitmap
    }
}
