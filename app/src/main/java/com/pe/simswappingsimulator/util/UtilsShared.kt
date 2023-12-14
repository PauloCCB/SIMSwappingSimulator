package com.pe.simswappingsimulator.util

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.pe.simswappingsimulator.activity.FingerprintDialogFragment
import java.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

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
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            advertisingId = adInfo.id.toString()
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


    public fun checkFingerprintCompatibility(context: Context, fingerprintManager:FingerprintManager,  keyguardManager: KeyguardManager): Boolean {
        // Verificar la disponibilidad del hardware y si hay huellas dactilares registradas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!fingerprintManager.isHardwareDetected || !fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(context, "No hay huellas dactilares registradas", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            Toast.makeText(context, "La autenticación de huellas dactilares no es compatible", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verificar la seguridad del bloqueo de pantalla
        if (!keyguardManager.isKeyguardSecure) {
            Toast.makeText(context, "Asegura tu pantalla de bloqueo en la configuración", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    public fun getKeyGenerator(): KeyGenerator {
        // Configurar el generador de claves
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "your_key_alias",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()

        return keyGenerator
    }

    public fun getCipher(keyGenerator: KeyGenerator): Cipher {
        // Configurar el cifrado
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val key = keyStore.getKey("your_key_alias", null) as SecretKey

        val cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
                KeyProperties.BLOCK_MODE_CBC + "/" +
                KeyProperties.ENCRYPTION_PADDING_PKCS7)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        return cipher
    }

}