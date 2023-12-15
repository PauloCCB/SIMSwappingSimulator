package com.pe.simswappingsimulator.activity

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityTransferBinding
import com.pe.simswappingsimulator.model.BodyOperation
import com.pe.simswappingsimulator.model.ResponseAccount
import com.pe.simswappingsimulator.model.ResponseOperation
import com.pe.simswappingsimulator.module.ApiClient
import com.pe.simswappingsimulator.util.UtilsShared
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransferActivity : AppCompatActivity(),AuthenticationResultListener {

    private lateinit var binding: ActivityTransferBinding
    private var generalExtras = Bundle()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private lateinit var fingerprintManager: FingerprintManager
    private lateinit var keyguardManager: KeyguardManager
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ActionBarTheme)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initView()
        supportActionBar?.apply {
            title = "Transferir dinero"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = location.latitude
                    longitude = location.longitude

                } ?: run {
                    Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            }
        setOnClickListener()
    }

    private fun initView() {
        val intent: Intent = intent
        val extras: Bundle? = intent.extras

        if (extras != null) {

            with(extras) {
                binding.txtSaldoCuenta.text = "S/. ${getDouble("saldo")}"
                val acc = getString("cc")
                val primeraParte = "********".substring(0, minOf(acc!!.length, 8))
                val resto = acc!!.substring(minOf(acc!!.length, 8))
                binding.txtNroCuentaOrigen.text  = "$primeraParte$resto"
            }
            generalExtras = extras
        }


        setTextWatchers()
    }

    private fun setTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateData()
            }
        }
        binding.txtCuentaDestino.addTextChangedListener(textWatcher)
        binding.txtMonto.addTextChangedListener(textWatcher)
    }
    private fun validateData(): Boolean {

        if (binding.txtCuentaDestino.text.toString().isNullOrEmpty()) {
            return false
        } else if (binding.txtMonto.text.toString().isNullOrEmpty()) {
            return false
        }
        binding.btnRegistrar.isEnabled = true
        return true
    }


        private fun setOnClickListener() {
        binding.btnRegistrar.setOnClickListener {

            //1- Validar huella digital
            with(UtilsShared) {
                if (checkFingerprintCompatibility(
                        this@TransferActivity,
                        fingerprintManager,
                        keyguardManager
                    )
                ) {
                    val keyGenerator = getKeyGenerator()
                    val cipher = getCipher(keyGenerator)
                    val cryptoObject = FingerprintManager.CryptoObject(cipher)
                    val authenticationCallback =
                        MyAuthenticationCallback(this@TransferActivity, this@TransferActivity)

                    val fingerprintDialog = FingerprintDialogFragment.newInstance(
                        cryptoObject,
                        authenticationCallback,
                        this@TransferActivity
                    )
                    fingerprintDialog.show(supportFragmentManager, FingerprintDialogFragment.TAG)

                    fingerprintDialog.startAuthentication(cipher, fingerprintManager)

                } else {

                    CustomConfirmationDialog(this@TransferActivity).showConfirmationDialog(
                        UtilsShared.CONFIRMATION_TITLE,
                        "Es necesario que cuente con un sensor de huella digital.",
                        "Ok"
                    ) {
                        binding.btnRegistrar.isEnabled = true
                    }
                    // El dispositivo no tiene un sensor de huella digital o no hay huellas registradas
                    //Toast.makeText(this, "No hay un sensor de huella digital o no hay huellas registradas", Toast.LENGTH_SHORT).show()
                }
            }
        }
            //2- Solicitamos validación SMS en back

            //3- De ser válida el SMS registramos operacion en back



    }

    private fun doRegisterOperation() {
        binding.txtCuentaDestino.text
        binding.txtMonto.text
        val bodyOperation = BodyOperation(
            id_operation = 0,
            id_usuario = generalExtras.getInt("idUsuario"),
            cuenta_destino = binding.txtCuentaDestino.text.toString(),
            cuenta_origen = generalExtras!!.getString("cc").toString(),
            monto = binding.txtMonto.text.toString().toDouble(),
            latitud = latitude,
            longitud = longitude

        )
        val call = ApiClient.simSwappingService.registerOperation(bodyOperation)
        call!!.enqueue(object : Callback<ResponseOperation> {
            override fun onResponse(call: Call<ResponseOperation>, response: Response<ResponseOperation>) {
                try{

                    if(response.body()!!.success){

                    }

                }catch (e: Exception) {
                    Log.d("ERRvalidateLogin",e.printStackTrace().toString())
                }
            }

            override fun onFailure(call: Call<ResponseOperation>, t: Throwable) {
                Log.e("Error:",t.printStackTrace().toString())
                binding.btnRegistrar.isEnabled = true
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onAuthenticationSuccess() {
        Toast.makeText(this@TransferActivity,"Autenticación exitosa.",Toast.LENGTH_SHORT).show()
        doRegisterOperation()

    }

    override fun onAuthenticationError(errorMessage: String) {
        binding.btnRegistrar.isEnabled = true
        Toast.makeText(this@TransferActivity,"Error de autenticación: ${errorMessage}",Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationHelp(helpMessage: String) {
        Toast.makeText(this@TransferActivity,"onAuthenticationHelp",Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationFailed() {
        runOnUiThread {
            binding.btnRegistrar.isEnabled = true
            Toast.makeText(this, "Autenticación fallida", Toast.LENGTH_SHORT).show()
        }
    }

}