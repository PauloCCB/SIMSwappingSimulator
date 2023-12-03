package com.pe.simswappingsimulator.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityLoginBinding
import com.pe.simswappingsimulator.model.BodyLogin
import com.pe.simswappingsimulator.module.ApiClient
import com.pe.simswappingsimulator.services.SimSwappingService
import com.pe.simswappingsimulator.util.Utils
import com.pe.simswappingsimulator.util.UtilsShared
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


class Login : AppCompatActivity(){

    private lateinit var binding: ActivityLoginBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var simSwappingService: SimSwappingService

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    var imei = ""

    val permission = android.Manifest.permission.READ_PHONE_STATE
    val requestCode = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        val view = binding.root
        setContentView(view)
        val retrofit = Retrofit.Builder()
            .baseUrl(Utils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        simSwappingService = retrofit.create(SimSwappingService::class.java)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            //requestLocation()x
        } else {
            requestPermission()
        }


        imei = UtilsShared.getSimulatedImei()
        Log.d("IMEI-AdvertisingId",imei)
        /*imei = getDeviceId(this)
        Log.d("IMEI-DeviceId",imei)

        if (!imei.isNotEmpty()) {
            imei = UtilsShared.getAdvertisingId(this)
            Log.d("IMEI-AdvertisingId",imei)
        }*/

        setOnClickListener()

    }

    private fun setOnClickListener() {
        binding.tvCreateAccount.setOnClickListener {
            val intent = Intent(this@Login, RegisterAccount::class.java)
            startActivity(intent)
            //overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isEnabled = false
            val bodyLogin = BodyLogin(
                null,
                null,
                null,
                null,
                binding.etCreditCard.text.toString(),
                binding.etPassword.text.toString(),
                latitude.toString(),
                longitude.toString(),
                imei,
                null
            )

            val call = ApiClient.simSwappingService.validateLogin(bodyLogin)

            call!!.enqueue(object : Callback<BodyLogin?> {
                override fun onResponse(call: Call<BodyLogin?>, response: Response<BodyLogin?>) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        startHomeActivity(result)
                    } else {
                        Toast.makeText(applicationContext,"Error",Toast.LENGTH_SHORT).show()
                        // Manejar el error
                    }
                    binding.btnLogin.isEnabled = true
                }

                override fun onFailure(call: Call<BodyLogin?>, t: Throwable) {
                    // Manejar el fallo en la comunicación
                    Log.e("Error:",t.message.toString())
                    Toast.makeText(applicationContext,"Error:${t.message}",Toast.LENGTH_LONG).show()
                    binding.btnLogin.isEnabled = true
                }
            })

        }
    }

    private fun startHomeActivity(result: BodyLogin?) {
        val intent = Intent(this@Login,Home::class.java)
        val bundle = Bundle()

        bundle.putInt("idUsuario", result?.id_usuario!!)
        bundle.putString("nombre", result?.nombre)
        bundle.putString("apellido", result?.apellido)
        bundle.putString("dni", result?.dni)
        bundle.putString("cc", result?.cc)
        bundle.putString("telefono", result?.telefono)
        bundle.putString("imei", result?.imei)
        bundle.putString("latitud", result?.latitud)
        bundle.putString("longitud", result?.longitud)


        intent.putExtras(bundle)
    }

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_ACCESS_LOCATION
        )
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    latitude = location.latitude
                    longitude = location.longitude

                    /*Toast.makeText(
                        this,
                        "Latitud: $latitude, Longitud: $longitude",
                        Toast.LENGTH_SHORT
                    ).show()*/
                } ?: run {
                    Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_LOCATION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso concedido, solicitar la ubicación
                    requestLocation()
                } else {
                    // Permiso denegado, manejar según sea necesario
                    Toast.makeText(
                        this,
                        "Permiso de ubicación denegado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_LOCATION = 1
    }

    private fun getDeviceId(context: Context): String{
        if (checkAndRequestPermission(context, android.Manifest.permission.READ_PHONE_STATE,requestCode)) {
            // Si no se tienen los permisos, solicítalos
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE), 1)
            // Puedes manejar la respuesta de permisos en el método onRequestPermissionsResult
            // y llamar a getIMEI nuevamente si los permisos son otorgados
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
            // El permiso no ha sido otorgado, solicitarlo
            ActivityCompat.requestPermissions(
                context as Login,  // Reemplaza YourActivity con el nombre de tu actividad
                arrayOf(permission),
                requestCode
            )
            return false
        }
    }

}