package com.pe.simswappingsimulator.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pe.simswappingsimulator.databinding.ActivityHomeBinding
import com.pe.simswappingsimulator.util.UtilsShared


class Home : AppCompatActivity(),AuthenticationResultListener {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var fingerprintManager: FingerprintManager
    private lateinit var keyguardManager: KeyguardManager

    lateinit var fingerprintDialog: FingerprintDialogFragment

    private var generalExtras = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        initView()

        setOnClickListeners()
    }

    private fun initView() {
        val intent: Intent = intent
        val extras: Bundle? = intent.extras

        if (extras != null) {

            with(extras){

                binding.tvSaldo.text = "S/. ${getDouble("saldo")}"
                val acc = getString("cc")
                val primeraParte = "********".substring(0, minOf(acc!!.length, 8))
                // Mantener el resto de la cadena sin cambios
                val resto = acc!!.substring(minOf(acc!!.length, 8))

                // Concatenar las dos partes
                binding.tvNroCuenta.text  = "$primeraParte$resto"
            }
            generalExtras = extras
        }
    }

    private fun setOnClickListeners() {
        binding.btnTransferir.setOnClickListener {
            val intent = Intent(this@Home, TransferActivity::class.java)
            intent.putExtras(generalExtras)
            startActivity(intent)
        }

        binding.btnSetting.setOnClickListener {

            with(UtilsShared) {
                if (checkFingerprintCompatibility(
                        this@Home,
                        fingerprintManager,
                        keyguardManager
                    )
                ) {
                    val keyGenerator = getKeyGenerator()
                    val cipher = getCipher(keyGenerator)
                    val cryptoObject = FingerprintManager.CryptoObject(cipher)
                    val authenticationCallback =
                        MyAuthenticationCallback(this@Home, this@Home)

                    fingerprintDialog = FingerprintDialogFragment.newInstance(
                        cryptoObject,
                        authenticationCallback,
                        this@Home
                    )
                    fingerprintDialog.show(
                        supportFragmentManager,
                        FingerprintDialogFragment.TAG
                    )

                    fingerprintDialog.startAuthentication(cipher, fingerprintManager)
                } else {

                    CustomConfirmationDialog(this@Home).showConfirmationDialog(
                        UtilsShared.CONFIRMATION_TITLE,
                        "Es necesario que cuente con un sensor de huella digital.",
                        "Ok"
                    ) {
                        binding.btnSetting.isEnabled = true
                    }
                    // El dispositivo no tiene un sensor de huella digital o no hay huellas registradas
                    //Toast.makeText(this, "No hay un sensor de huella digital o no hay huellas registradas", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    override fun onAuthenticationSuccess() {
        Toast.makeText(this,"Autenticación exitosa.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this@Home, SettingsActivity::class.java)
        intent.putExtras(generalExtras)
        startActivity(intent)
        fingerprintDialog.dismiss()
    }

    override fun onAuthenticationError(errorMessage: String) {
        binding.btnSetting.isEnabled = true
        Toast.makeText(this,"${errorMessage}", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationHelp(helpMessage: String) {
        Toast.makeText(this,"onAuthenticationHelp", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationFailed() {
        runOnUiThread {
            binding.btnSetting.isEnabled = true
            Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()
        }
    }


}