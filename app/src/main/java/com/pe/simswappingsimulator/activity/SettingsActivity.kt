package com.pe.simswappingsimulator.activity

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityHomeBinding
import com.pe.simswappingsimulator.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private var generalExtras = Bundle()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            obtenerUbicacionActual()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        //googleMapsSettings()
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {
        // Obtener la última ubicación conocida
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Utilizar la ubicación actual como latitudInicial y longitudInicial
                    val latitudInicial = location.latitude
                    val longitudInicial = location.longitude

                    // Inicializar el mapa y configurarlo con la ubicación actual
                    val mapFragment =
                        supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

                    mapFragment.getMapAsync { googleMap ->
                        val initialLocation = LatLng(latitudInicial, longitudInicial)
                        googleMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(initialLocation, 15f)
                        )

                        val radioMetros = 100.0
                        val circleOptions = CircleOptions()
                            .center(marcador)
                            .radius(radioMetros)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.argb(70, 0, 0, 255))

                        googleMap.addCircle(circleOptions)

                        googleMap.addMarker(MarkerOptions().position(initialLocation).title("Marcador Inicial"))
                        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

                        // Habilita los controles de zoom
                        googleMap.uiSettings.isZoomControlsEnabled = true
                        googleMap.setOnMarkerClickListener { marker ->
                            // Lógica para manejar clics en marcadores
                            true
                        }
                    }
                }
            }
    }
   /* private fun googleMapsSettings() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            // Configura el mapa aquí
            val initialLocation = LatLng(latitudInicial, longitudInicial)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f))

            // Añade un marcador en la ubicación inicial
            googleMap.addMarker(MarkerOptions().position(initialLocation).title("Marcador Inicial"))

            // Personaliza la apariencia del mapa, por ejemplo, cambiar el tipo de mapa a satélite
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

            // Habilita los controles de zoom
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Configura un listener para manejar eventos de clic en marcadores
            googleMap.setOnMarkerClickListener { marker ->
                // Lógica para manejar clics en marcadores
                true
            }
        }
    }*/
}