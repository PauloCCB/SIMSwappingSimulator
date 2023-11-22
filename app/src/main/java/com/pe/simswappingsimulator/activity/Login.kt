package com.pe.simswappingsimulator.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
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
import com.pe.simswappingsimulator.services.SimSwappingService
import com.pe.simswappingsimulator.util.Utils
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        val retrofit = Retrofit.Builder()
            .baseUrl(Utils.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        simSwappingService = retrofit.create(SimSwappingService::class.java)


        val view = binding.root
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            //requestLocation()
        } else {
            requestPermission()
        }
        setContentView(view)

        imei = getDeviceId(this)
        if (!imei.isNotEmpty()) {
            // Manejar el caso en el que no se pueda obtener el IMEI
            imei = getAdvertisingId(this)
        }
        setOnClickListener()

    }

    private fun setOnClickListener() {
        binding.tvCreateAccount.setOnClickListener {
            val intent = Intent(this@Login, RegisterAccount::class.java)
            startActivity(intent)

            // Agrega la animación de transición
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.btnLogin.setOnClickListener {

            val bodyLogin = BodyLogin(
                binding.etCreditCard.text.toString(),
                binding.etPassword.text.toString(),
                latitude.toString(),
                longitude.toString(),
                imei
            )

            val call = simSwappingService.validateLogin(bodyLogin)

            call!!.enqueue(object : Callback<Integer?> {
                override fun onResponse(call: Call<Integer?>, response: Response<Integer?>) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        startAccountPanelActivity(result)
                    } else {
                        Toast.makeText(applicationContext,"Error",Toast.LENGTH_SHORT).show()
                        // Manejar el error
                    }
                }

                override fun onFailure(call: Call<Integer?>, t: Throwable) {
                    // Manejar el fallo en la comunicación
                    Toast.makeText(applicationContext,"Error",Toast.LENGTH_SHORT).show()
                }
            })





        }
    }

    private fun startAccountPanelActivity(result: Integer?) {
        /*val intent = Intent(this@Login, AccountPanel::class.java)
        val bundle = Bundle()

        bundle.putString("accountType", it.tipoCuenta)
        bundle.putString("accountId", it.id.toString())

        intent.putExtras(bundle)*/
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
        val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return telephonyManager.imei ?: ""
        } else {
            // Antes de Android Oreo, puedes obtener el IMEI de la siguiente manera
            return telephonyManager.deviceId ?: ""
        }

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
}