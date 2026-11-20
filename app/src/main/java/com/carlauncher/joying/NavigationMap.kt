package com.carlauncher.joying

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import java.io.File

/**
 * MapView informativo: no consume toques, de modo que el panel central sigue
 * respondiendo a la pulsación (selector de widgets) igual que el resto.
 */
private class FollowMapView(context: Context) : MapView(context) {
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean = false
}

private fun configureOsmdroid(context: Context) {
    val config = Configuration.getInstance()

    config.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    // OpenStreetMap exige identificar la app. Caché en el almacenamiento interno
    // (no hace falta permiso de almacenamiento).
    config.userAgentValue = "CarLauncher/" + context.packageName

    val base = File(context.cacheDir, "osmdroid")
    config.osmdroidBasePath = base
    config.osmdroidTileCache = File(base, "tiles")
}

/** Flecha de posición: halo azul con una punta de flecha blanca-azul. */
private fun positionIcon(context: Context): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val size = (46 * density).toInt().coerceAtLeast(40)
    val center = size / 2f

    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.argb(70, 33, 150, 243)
    canvas.drawCircle(center, center, size * 0.48f, paint)

    val arrow = Path().apply {
        moveTo(center, size * 0.14f)
        lineTo(size * 0.80f, size * 0.82f)
        lineTo(center, size * 0.66f)
        lineTo(size * 0.20f, size * 0.82f)
        close()
    }

    paint.color = android.graphics.Color.rgb(33, 150, 243)
    canvas.drawPath(arrow, paint)

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2.5f * density
    paint.strokeJoin = Paint.Join.ROUND
    paint.color = android.graphics.Color.WHITE
    canvas.drawPath(arrow, paint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Mapa que sigue la posición GPS: se orienta con el rumbo (el rumbo siempre
 * arriba) y ajusta el zoom según la velocidad. Colores invertidos para que
 * encaje con el tema oscuro. Tiles de OpenStreetMap (necesita Internet).
 */
@Composable
fun NavigationMap(
    gps: CarGpsData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = (context as? LifecycleOwner)?.lifecycle

    val mapView = remember {
        configureOsmdroid(context)

        FollowMapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(false)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
            controller.setZoom(16.5)
        }
    }

    val marker = remember {
        Marker(mapView).apply {
            icon = positionIcon(context)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            mapView.overlays.add(this)
        }
    }

    // Última posición conocida (la guarda la meteorología) para no empezar en el océano
    val fallback = remember {
        val prefs = context.getSharedPreferences("weather_settings", Context.MODE_PRIVATE)
        val lat = prefs.getString("lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("lon", null)?.toDoubleOrNull()

        if (lat != null && lon != null) GeoPoint(lat, lon) else null
    }

    // Orientación suavizada (se conserva entre actualizaciones)
    val orientation = remember { floatArrayOf(0f) }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }

        lifecycle?.addObserver(observer)

        onDispose {
            lifecycle?.removeObserver(observer)
            mapView.onDetach()
        }
    }

    Box(modifier = modifier) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                val lat = gps.latitude
                val lon = gps.longitude

                val point = if (lat != null && lon != null) GeoPoint(lat, lon) else fallback

                if (point != null) {
                    marker.position = point
                    view.controller.setCenter(point)
                }

                // Más zoom despacio, más alejado rápido
                val zoom = when {
                    gps.speedKmh < 25f -> 17.5
                    gps.speedKmh < 60f -> 16.5
                    gps.speedKmh < 100f -> 15.5
                    else -> 14.5
                }
                view.controller.setZoom(zoom)

                // Rumbo arriba, con giro suave por el camino más corto
                gps.bearing?.let { bearing ->
                    val diff = ((bearing - orientation[0] + 540f) % 360f) - 180f
                    orientation[0] = (orientation[0] + diff * 0.35f + 360f) % 360f
                }

                view.mapOrientation = -orientation[0]
                view.invalidate()
            }
        )

        if (gps.latitude == null && fallback == null) {
            Text(
                text = "ESPERANDO POSICIÓN GPS",
                color = Color(0xFF8FA6BF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
