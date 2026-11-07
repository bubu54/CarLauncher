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

    private fun openApp(
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
}