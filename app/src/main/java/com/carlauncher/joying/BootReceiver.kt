package com.carlauncher.joying

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Lanza el launcher tras el arranque. Está deshabilitado en el manifest y solo se
 * activa desde Ajustes > Sistema > "Iniciar launcher al arrancar".
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            try {
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } catch (_: Exception) {
                // Android 10+ puede bloquear el inicio desde segundo plano.
            }
        }
    }
}
