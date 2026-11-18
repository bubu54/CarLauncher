package com.carlauncher.joying

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager

/** Generación de la red móvil actual: "2G", "3G", "4G" o "5G". */
object CellularInfo {

    fun generation(context: Context, downstreamKbps: Int): String {

        // Con el permiso READ_PHONE_STATE se lee el tipo real de red del módem
        if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val fromModem = fromNetworkType(tm.dataNetworkType)

                if (fromModem != null) return fromModem
            } catch (_: SecurityException) {
            } catch (_: Exception) {
            }
        }

        // Sin permiso (o sin dato): estimación por el ancho de banda que informa Android
        return fromBandwidth(downstreamKbps)
    }

    private fun fromNetworkType(type: Int): String? = when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN,
        TelephonyManager.NETWORK_TYPE_GSM -> "2G"

        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G"

        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_IWLAN,
        19 /* LTE_CA (oculto en el SDK) */ -> "4G"

        TelephonyManager.NETWORK_TYPE_NR -> "5G"

        else -> null
    }

    private fun fromBandwidth(kbps: Int): String = when {
        kbps <= 0 -> "4G"
        kbps <= 400 -> "2G"
        kbps <= 20_000 -> "3G"
        else -> "4G"
    }
}
