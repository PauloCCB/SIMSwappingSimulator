package com.pe.simswappingsimulator.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.components.GetAdvertisingIdListener
import com.pe.simswappingsimulator.components.GetAdvertisingIdTask
import com.pe.simswappingsimulator.databinding.ActivityRegisterAccountBinding
import com.pe.simswappingsimulator.model.BodyAccount
import com.pe.simswappingsimulator.model.ResponseAccount
import com.pe.simswappingsimulator.module.ApiClient
import com.pe.simswappingsimulator.util.UtilsShared
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterAccount : AppCompatActivity(), GetAdvertisingIdListener {

    private lateinit var binding: ActivityRegisterAccountBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0
    var imei =""
    val requestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ActionBarTheme)
        //setContentView(R.layout.activity_register_account)
        binding = ActivityRegisterAccountBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        imei = getDeviceId(this@RegisterAccount)
        if (imei.isNullOrEmpty()) {
            val obtenerAdvertisingIdTask = GetAdvertisingIdTask(applicationContext,this@RegisterAccount)
            obtenerAdvertisingIdTask.execute()
        }else {
            binding.tvImei.text = imei
        }

        setTextWatchers()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setLatitudeLongitude()

        supportActionBar?.apply {
            title = "Registrar cuenta"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }


        onClicksEvents()
    }

    private fun getDeviceId(context: Context): String{
        if (checkAndRequestPermission(context, android.Manifest.permission.READ_PHONE_STATE,requestCode)) {
            // Si no se tienen los permisos, solicítalos
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE), 1)
            return ""
        }
        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return telephonyManager.imei ?: ""
        } else {
            // Antes de Android Oreo, puedes obtener el IMEI de la siguiente manera
            return telephonyManager.deviceId ?: ""
        }

    }
    fun checkAndRequestPermission(context: Context, permission: String, requestCode: Int): Boolean {
        // Verificar si el permiso ya ha sido otorgado
        val permissionCheck = ContextCompat.checkSelfPermission(context, permission)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // El permiso ya ha sido otorgado
            return true
        } else {
            ActivityCompat.requestPermissions(
                context as Login,  // Reemplaza YourActivity con el nombre de tu actividad
                arrayOf(permission),
                requestCode
            )
            return false
        }
    }
    private fun setTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateData()
            }
        }
        binding.txtDNI.addTextChangedListener(textWatcher)
        binding.txtNombre.addTextChangedListener(textWatcher)
        binding.txtApellido.addTextChangedListener(textWatcher)
        binding.txtCC.addTextChangedListener(textWatcher)
        binding.txtPIN.addTextChangedListener(textWatcher)

    }
    @SuppressLint("MissingPermission")
    private fun setLatitudeLongitude (){

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitud = location.latitude
                    longitud = location.longitude

                } ?: run {
                    Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun onClicksEvents() {
        binding.btnRegistrar.setOnClickListener {

            with(binding) {

                if(validateData()) {
                    val objBodyAccount = BodyAccount(
                        txtDNI.text.toString(),
                        txtNombre.text.toString(),
                        txtApellido.text.toString(),
                        txtCC.text.toString(),
                        latitud.toString(),
                        longitud.toString(),
                        txtPIN.text.toString(),
                        null,
                        imei

                    )
                    val call = ApiClient.simSwappingService.registerAccount(objBodyAccount)

                    call!!.enqueue(object : Callback<ResponseAccount> {

                        override fun onResponse(call: Call<ResponseAccount>, response: Response<ResponseAccount>) {

                            if (response.isSuccessful ) {
                                //Toast.makeText(applicationContext,response.body()!!.message,Toast.LENGTH_SHORT).show()

                                val confirmationDialog = CustomConfirmationDialog(this@RegisterAccount)

                                confirmationDialog.showConfirmationDialog(
                                    UtilsShared.CONFIRMATION_TITLE,
                                    response.body()!!.message,
                                    "Ok"
                                ) {
                                    startLoginActivity()
                                }

                            }else {
                                Log.d("error","Error: ${response.message()}")
                                Toast.makeText(applicationContext,"Error: ${response}",Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ResponseAccount>, t: Throwable) {
                            Log.d("error","Failure: ${t.printStackTrace()}")
                            Toast.makeText(applicationContext,"Failure: ${t.message}",Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }


        }
    }

    private fun startLoginActivity() {
        val intent = Intent(this@RegisterAccount, Login::class.java)
        startActivity(intent)
    }

    private fun validateData() : Boolean {

        if(binding.txtNombre.text.toString().isNullOrEmpty()){
            return false
        } else if(binding.txtApellido.text.toString().isNullOrEmpty()){
            return false
        } else if(binding.txtCC.text.toString().isNullOrEmpty()){
            return false
        } else if(binding.txtNombre.text.toString().isNullOrEmpty()){
            return false
        } else if(binding.txtDNI.text.toString().isNullOrEmpty()){
            return false
        } else if(binding.txtPIN.text.toString().isNullOrEmpty()){
            return false
        }
        binding.btnRegistrar.isEnabled = true
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onGetAdvertisingId(advertisingId: String) {
        Log.d("advertisingId:",advertisingId)
        imei = advertisingId
        binding.tvImei.text = imei
    }


}