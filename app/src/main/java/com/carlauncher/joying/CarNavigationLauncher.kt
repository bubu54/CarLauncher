package com.carlauncher.joying

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object CarNavigationLauncher {

    fun openGoogleMaps(context: Context) {
        openApp(
            context,
            "com.google.android.apps.maps",
            "Google Maps"
        )
    }

    fun openWaze(context: Context) {
        openApp(
            context,
            "com.waze",
            "Waze"
        )
    }

    fun openSygic(context: Context) {
        openApp(
            context,
            "com.sygic.aura",
            "Sygic"
        )
    }

    fun openApp(
        context: Context,
        packageName: String,
        appName: String
    ) {
        try {
            val launchIntent =
                context.packageManager.getLaunchIntentForPackage(packageName)

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                Toast.makeText(
                    context,
                    "$appName no está instalado",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "No se ha podido abrir $appName",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun openNavigation(
        context: Context,
        latitude: Double,
        longitude: Double
    ) {
        val uri = Uri.parse("google.navigation:q=$latitude,$longitude")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openGoogleMaps(context)
        }
    }

    /** Abre la primera app de navegación instalada (Waze > Google Maps > Sygic). */
    fun openFirstAvailable(context: Context) {
        val candidates = listOf(
            "com.waze" to "Waze",
            "com.google.android.apps.maps" to "Google Maps",
            "com.sygic.aura" to "Sygic"
        )

        val installed = candidates.firstOrNull { (pkg, _) ->
            context.packageManager.getLaunchIntentForPackage(pkg) != null
        }

        if (installed != null) {
            openApp(context, installed.first, installed.second)
        } else {
            Toast.makeText(
                context,
                "No hay ninguna app de navegación instalada",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun openDialer(context: Context) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            Toast.makeText(context, "No se ha podido abrir el teléfono", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAndroidAuto(context: Context) {
        openApp(context, "com.google.android.projection.gearhead", "Android Auto")
    }

    /** Abre la app indicada; si está vacía o no existe, la primera disponible. */
    fun openPreferred(context: Context, packageName: String) {
        if (packageName.isNotBlank()) {
            val pm = context.packageManager

            if (pm.getLaunchIntentForPackage(packageName) != null) {
                val label = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
                } catch (_: Exception) {
                    packageName
                }

                openApp(context, packageName, label)
                return
            }
        }

        openFirstAvailable(context)
    }
}
