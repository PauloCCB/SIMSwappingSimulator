package com.pe.simswappingsimulator.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityLoginBinding


class Login : AppCompatActivity(){

    private lateinit var binding: ActivityLoginBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        val view = binding.root
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            requestLocation()
        } else {
            requestPermission()
        }
        setContentView(view)
        setOnClickListener()


    }

    private fun setOnClickListener() {
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this@Login, RegisterAccount::class.java)
            startActivity(intent)

            // Agrega la animación de transición
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
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
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Aquí puedes utilizar las variables latitude y longitude como desees
                    Toast.makeText(
                        this,
                        "Latitud: $latitude, Longitud: $longitude",
                        Toast.LENGTH_SHORT
                    ).show()
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

}