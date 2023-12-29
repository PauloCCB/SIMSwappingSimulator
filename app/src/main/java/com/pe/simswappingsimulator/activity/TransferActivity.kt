package com.pe.simswappingsimulator.activity

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.hardware.fingerprint.FingerprintManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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

    private var latitude: String = ""
    private var longitude: String = ""

    private lateinit var fingerprintManager: FingerprintManager
    private lateinit var keyguardManager: KeyguardManager

    private val handler = Handler(Looper.getMainLooper())
    private var progressStatus = 0
    private var isReadyToFinish = false
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

        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = String.format("%.8f", location.latitude)
                    longitude = String.format("%.8f", location.longitude)

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


        @SuppressLint("MissingPermission")
        private fun setOnClickListener() {
        binding.btnRegistrar.setOnClickListener {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        latitude = String.format("%.8f", location.latitude)
                        longitude = String.format("%.8f", location.longitude)

                        binding.txtLocation.text = "Latitud: ${latitude}  Longitud: ${longitude}"

                    } ?: run {
                        Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                    }
                }
            binding.btnRegistrar.isEnabled = false
            if (!isReadyToFinish) {
                binding.pgToken.visibility = View.VISIBLE

                Thread {
                    while (progressStatus < 100) {
                        progressStatus += 1

                        // Actualizar la barra de progreso en el hilo principal
                        handler.post {
                            binding.pgToken.progress = progressStatus
                        }

                        try {
                            // Agregar un retraso para simular una operación en curso
                            Thread.sleep(50)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }

                    if (progressStatus == 100) {
                        runOnUiThread {
                            binding.btnRegistrar.text = "ENVIAR"
                            binding.txtMensajeToken.visibility = View.VISIBLE
                            binding.txtToken.visibility = View.VISIBLE
                            binding.txtExpira.visibility = View.VISIBLE
                            binding.btnRegistrar.isEnabled = true
                            isReadyToFinish = true
                        }
                    }

                }.start()
                binding.txtToken.text = generateTokenDigits(6)
            } else {
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
                        fingerprintDialog.show(
                            supportFragmentManager,
                            FingerprintDialogFragment.TAG
                        )

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
        }
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
                        startHomeActivity()
                    }else {

                        CustomConfirmationDialog(this@TransferActivity).showConfirmationDialog(
                            UtilsShared.CONFIRMATION_TITLE,
                            response.body()!!.message,
                            "Ok"
                        ) {
                            binding.btnRegistrar.isEnabled = true
                        }
                        Toast.makeText(this@TransferActivity,"Ocurrió un error, por favor vuelva a intentarlo",Toast.LENGTH_SHORT).show()
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

    private fun startHomeActivity() {
        val intent = Intent(this@TransferActivity,Home::class.java)
        val bundle = Bundle()

        bundle.putInt("idUsuario", generalExtras.getInt("idUsuario"))
        bundle.putString("nombre", generalExtras.getString("nombre"))
        bundle.putString("apellido", generalExtras.getString("apellido"))
        bundle.putString("dni", generalExtras.getString("dni"))
        bundle.putString("cc", generalExtras.getString("cc").toString())
        bundle.putString("imei", generalExtras.getString("imei").toString())
        bundle.putString("latitud", latitude.toString())
        bundle.putString("longitud", longitude.toString())
        bundle.putInt("idCuenta", generalExtras.getInt("idCuenta"))

        var nuevoSaldo = generalExtras.getDouble("saldo") - binding.txtMonto.text.toString().toDouble()
        bundle.putDouble("saldo", nuevoSaldo)


        /* val objUsuario = generalExtras.usuario
        val objCuenta = result.cuenta
        bundle.putInt("idUsuario", objUsuario.id_usuario!!)
        bundle.putString("nombre", objUsuario.nombre)
        bundle.putString("apellido", objUsuario.apellido)
        bundle.putString("dni", objUsuario.dni)
        bundle.putString("cc", objUsuario.cc)
        bundle.putString("telefono", objUsuario.telefono)
        bundle.putString("imei", objUsuario.imei)
        bundle.putString("latitud", objUsuario.latitud)
        bundle.putString("longitud", objUsuario.longitud)

        bundle.putInt("idCuenta", objCuenta.id_cuenta)
        bundle.putDouble("saldo", objCuenta.saldo)
        */

        intent.putExtras(bundle)
        startActivity(intent)
    }

    fun generateTokenDigits(length: Int): String {
        val random = java.util.Random()
        val stringBuilder = StringBuilder(length)

        repeat(length) {
            val digit = random.nextInt(10)
            stringBuilder.append(digit)
        }

        return stringBuilder.toString()
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
        Toast.makeText(this@TransferActivity,"${errorMessage}",Toast.LENGTH_SHORT).show()
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