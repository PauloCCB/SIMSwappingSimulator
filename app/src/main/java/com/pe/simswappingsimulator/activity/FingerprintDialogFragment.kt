package com.pe.simswappingsimulator.activity

import android.app.AlertDialog
import android.app.Dialog
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.os.CancellationSignal
import androidx.fragment.app.DialogFragment
import com.pe.simswappingsimulator.R
import javax.crypto.Cipher

class FingerprintDialogFragment : DialogFragment() {

    /*private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var callback: FingerprintManager.AuthenticationCallback
    */
    private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var callback: MyAuthenticationCallback
    private lateinit var authenticationResultListener: AuthenticationResultListener
    private lateinit var cancellationSignal: CancellationSignal
    fun startAuthentication(
        cipher: Cipher,
        fingerprintManager: FingerprintManager
    ) {
        cancellationSignal = CancellationSignal()
        val cryptoObject = FingerprintManager.CryptoObject(cipher)
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, callback, null)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity?.layoutInflater
        val view = inflater?.inflate(R.layout.fragment_fingerprint_dialog, null)

        builder.setView(view)
            .setNegativeButton("Cancelar") { dialog, _ ->
                // Manejar la cancelación de la autenticación
                dialog.dismiss()
            }

        return builder.create()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationSignal.cancel()
    }

    companion object {
        const val TAG = "FingerprintDialogFragment"

        fun newInstance(
            cryptoObject: FingerprintManager.CryptoObject,
            callback: MyAuthenticationCallback,
            authenticationResultListener: AuthenticationResultListener
        ): FingerprintDialogFragment {
            val fragment = FingerprintDialogFragment()
            fragment.cryptoObject = cryptoObject
            fragment.callback = callback
            fragment.authenticationResultListener = authenticationResultListener

            return fragment
        }
    }
}