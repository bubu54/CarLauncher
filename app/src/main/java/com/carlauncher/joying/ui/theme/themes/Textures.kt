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
     * Mosaico de fibra de carbono (tejido 2x2): celdas alternas con la fibra en
     * horizontal y en vertical, cada una con un brillo central.
     * [cell] = lado de cada celda en píxeles.
     */
    fun carbonTile(cell: Int): Bitmap = carbonCache.getOrPut(cell) {

        val size = cell * 2
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFF0C0D0E.toInt())

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val colors = intArrayOf(0xFF08090A.toInt(), 0xFF2B2E32.toInt(), 0xFF0D0E10.toInt())
        val stops = floatArrayOf(0f, 0.5f, 1f)

        for (cy in 0..1) {
            for (cx in 0..1) {

                val x0 = (cx * cell).toFloat()
                val y0 = (cy * cell).toFloat()
                val horizontalStrand = (cx + cy) % 2 == 0

                paint.shader = if (horizontalStrand) {
                    LinearGradient(0f, y0, 0f, y0 + cell, colors, stops, Shader.TileMode.CLAMP)
                } else {
                    LinearGradient(x0, 0f, x0 + cell, 0f, colors, stops, Shader.TileMode.CLAMP)
                }

                canvas.drawRect(x0, y0, x0 + cell, y0 + cell, paint)
            }
        }

        // Rendija oscura entre hebras para dar definición
        paint.shader = null
        paint.color = 0x88000000.toInt()
        paint.strokeWidth = 1f

        canvas.drawLine(0f, 0f, size.toFloat(), 0f, paint)
        canvas.drawLine(0f, cell.toFloat(), size.toFloat(), cell.toFloat(), paint)
        canvas.drawLine(0f, 0f, 0f, size.toFloat(), paint)
        canvas.drawLine(cell.toFloat(), 0f, cell.toFloat(), size.toFloat(), paint)

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

        // Una estría por fila, con intensidad aleatoria
        for (y in 0 until h) {
            val alpha = random.nextInt(30)
            paint.color = if (random.nextBoolean()) {
                android.graphics.Color.argb(alpha, 255, 255, 255)
            } else {
                android.graphics.Color.argb(alpha, 0, 0, 0)
            }
            canvas.drawRect(0f, y.toFloat(), w.toFloat(), y + 1f, paint)
        }

        // Segmentos más largos y brillantes (con continuidad al repetir el mosaico)
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
