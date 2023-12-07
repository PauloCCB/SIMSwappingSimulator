package com.pe.simswappingsimulator.activity

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.widget.Toast

class MyAuthenticationCallback(private val context: Context) : FingerprintManager.AuthenticationCallback() {

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
        // La autenticación mediante huella digital ha tenido éxito
        Toast.makeText(context, "Autenticación exitosa", Toast.LENGTH_SHORT).show()
        // Puedes realizar acciones adicionales aquí según tus necesidades
    }

    override fun onAuthenticationFailed() {
        // La autenticación mediante huella digital ha fallado
        Toast.makeText(context, "Autenticación fallida", Toast.LENGTH_SHORT).show()
        // Puedes realizar acciones adicionales aquí según tus necesidades
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
        // Se proporciona información adicional para ayudar al usuario durante la autenticación
        Toast.makeText(context, "Ayuda: $helpString", Toast.LENGTH_SHORT).show()
        // Puedes realizar acciones adicionales aquí según tus necesidades
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
        // Se notifica un error durante la autenticación
        Toast.makeText(context, "Error: $errString", Toast.LENGTH_SHORT).show()
        // Puedes realizar acciones adicionales aquí según tus necesidades
    }
}