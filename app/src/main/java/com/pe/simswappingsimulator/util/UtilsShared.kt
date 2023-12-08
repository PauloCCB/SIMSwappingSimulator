package com.pe.simswappingsimulator.util

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import java.io.IOException

object UtilsShared {

   val CONFIRMATION_TITLE = "Mensaje del sistema"
    fun getSimulatedImei(): String {
        return "123456789012345" // Puedes establecer el valor que desees
    }
    fun getSimulatedPhone(): String {
        return "991691171"
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


    @SuppressLint("MissingPermission")
    fun getPhoneNumber(context:Context): String {
        val telephoneManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if(telephoneManager.line1Number.isNullOrEmpty()){
            return getSimulatedPhone()
        }else {
            return telephoneManager.line1Number
        }
    }
}