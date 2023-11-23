package com.pe.simswappingsimulator.util

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import java.io.IOException

object UtilsShared {

    fun getSimulatedImei(): String {
        return "123456789012345" // Puedes establecer el valor que desees
    }

    fun getAdvertisingId(context: Context): String {
        var advertisingId = ""
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context.applicationContext)
            advertisingId = adInfo.id
        } catch (e: IOException) {
            e.printStackTrace()
            // Manejar la excepción
        } catch (e: GooglePlayServicesNotAvailableException) {
            // Manejar la excepción
        } catch (e: GooglePlayServicesRepairableException) {
            // Manejar la excepción
        }

        return advertisingId
    }
}