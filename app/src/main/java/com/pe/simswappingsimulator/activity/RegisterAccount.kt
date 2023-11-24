package com.pe.simswappingsimulator.activity

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityRegisterAccountBinding
import com.pe.simswappingsimulator.model.BodyAccount
import com.pe.simswappingsimulator.module.ApiClient

class RegisterAccount : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterAccountBinding

    //private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitud: Double = 0.0
    private var longitud: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ActionBarTheme)
        setContentView(R.layout.activity_register_account)
        binding = ActivityRegisterAccountBinding.inflate(layoutInflater)


        setLatitudeLongitude()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        supportActionBar?.apply {
            title = "Registrar cuenta"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }


        onClicksEvents()
    }

    private fun setLatitudeLongitude (){
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
                    latitud = location.latitude
                    longitud = location.longitude

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

    private fun onClicksEvents() {
        binding.btnRegistrar.setOnClickListener {

            with(binding) {

                val objBodyAccount = BodyAccount(
                    txtDNI.text.toString(),
                    txtNombre.text.toString(),
                    txtApellido.text.toString(),
                    txtCC.text.toString(),
                    latitud.toString(),
                    longitud.toString(),
                    txtPIN.text.toString(),
                    null
                )
                ApiClient.simSwappingService.registerAccount(objBodyAccount)

            }


        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    /*override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            // Solicitar permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }*/

    /*private fun enableMyLocation() {
        mMap.isMyLocationEnabled = true

        // Obtener la ubicación actual
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                }
            }

        // Configurar actualizaciones de ubicación en tiempo real
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)
            .setFastestInterval(5000)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (locationResult.lastLocation != null) {
                    val updatedLocation = LatLng(
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude
                    )
                    // Hacer algo con la ubicación actualizada, si es necesario
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }*/

    /*override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }*/
}