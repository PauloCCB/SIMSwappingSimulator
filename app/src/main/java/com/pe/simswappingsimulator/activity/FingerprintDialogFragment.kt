package com.pe.simswappingsimulator.activity

import android.app.AlertDialog
import android.app.Dialog
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.pe.simswappingsimulator.R

class FingerprintDialogFragment : DialogFragment() {

    private lateinit var cryptoObject: FingerprintManager.CryptoObject
    private lateinit var callback: FingerprintManager.AuthenticationCallback


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

    companion object {
        const val TAG = "FingerprintDialogFragment"

        fun newInstance(cryptoObject: FingerprintManager.CryptoObject, callback: FingerprintManager.AuthenticationCallback): FingerprintDialogFragment {
            val fragment = FingerprintDialogFragment()
            fragment.cryptoObject = cryptoObject
            fragment.callback = callback
            return fragment
        }
    }
}