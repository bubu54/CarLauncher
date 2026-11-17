package com.carlauncher.joying

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

/** Acciones del sistema Android: abrir apps, ajustes y elegir launcher. */
object SystemActions {

    private fun startSafely(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    fun launchApp(context: Context, packageName: String, activityName: String) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(packageName, activityName))
            .addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

        if (!startSafely(context, intent)) {
            Toast.makeText(context, "No se ha podido abrir la aplicación", Toast.LENGTH_SHORT).show()
        }
    }

    /** Ajustes generales de Android. */
    fun openSystemSettings(context: Context) {
        if (!startSafely(context, Intent(Settings.ACTION_SETTINGS))) {
            Toast.makeText(context, "No se han podido abrir los ajustes del sistema", Toast.LENGTH_SHORT).show()
        }
    }

    /** Información de una app (permisos, forzar detención, desinstalar...). */
    fun openAppInfo(context: Context, packageName: String) {
        startSafely(
            context,
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
        )
    }

    /**
     * Abre la pantalla de Android para elegir el launcher predeterminado.
     * Si el dispositivo no la ofrece, prueba las apps predeterminadas y por último
     * los ajustes generales.
     */
    fun openDefaultLauncherSettings(context: Context) {
        val candidates = listOf(
            Settings.ACTION_HOME_SETTINGS,
            Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS,
            Settings.ACTION_SETTINGS
        )

        for (action in candidates) {
            if (startSafely(context, Intent(action))) return
        }

        Toast.makeText(context, "No se han podido abrir los ajustes del sistema", Toast.LENGTH_SHORT).show()
    }

    private fun resolveHome(context: Context) =
        context.packageManager.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.MATCH_DEFAULT_ONLY
        )

    /** true si este launcher es el launcher predeterminado actual. */
    fun isDefaultLauncher(context: Context): Boolean {
        return try {
            resolveHome(context)?.activityInfo?.packageName == context.packageName
        } catch (e: Exception) {
            false
        }
    }

    /** Nombre del launcher predeterminado actual (si el sistema lo expone). */
    fun defaultLauncherLabel(context: Context): String? {
        return try {
            val info = resolveHome(context) ?: return null
            val pkg = info.activityInfo.packageName

            // "android" = selector de launcher: no hay ninguno predeterminado
            if (pkg == "android") null else info.loadLabel(context.packageManager).toString()
        } catch (e: Exception) {
            null
        }
    }
}
