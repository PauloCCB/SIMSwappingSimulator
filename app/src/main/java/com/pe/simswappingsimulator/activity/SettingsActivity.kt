package com.pe.simswappingsimulator.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityHomeBinding
import com.pe.simswappingsimulator.databinding.ActivitySettingsBinding
import com.pe.simswappingsimulator.model.ResponseAccount
import com.pe.simswappingsimulator.model.ResponseUbicaciones
import com.pe.simswappingsimulator.model.Ubicaciones
import com.pe.simswappingsimulator.module.ApiClient
import com.pe.simswappingsimulator.util.UtilsShared
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SettingsActivity : AppCompatActivity(), GoogleMap.OnMapClickListener,GoogleMap.OnMarkerClickListener {

    private lateinit var binding: ActivitySettingsBinding

    private var generalExtras = Bundle()

    private lateinit var googleMap: GoogleMap
    private var markerCount = 0
    private var markers = ArrayList<LatLng>()
    private var polyline: Polyline? = null

    private lateinit var lstUbicaciones: List<Ubicaciones>

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ActionBarTheme)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Elige tus zonas de uso"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val intent: Intent = intent
        val extras: Bundle? = intent.extras

        if (extras != null) {
            generalExtras = extras
        }

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

        //invocamos a la lista de ubicaciones segun id usuario
        /*val objUbicacion = Ubicaciones(
            id_ubicacion = 0,
            id_usuario = generalExtras.getInt("idUsuario"),
            latitud = "",
            longitud = "",
            estado = ""
        )*/
        val call = ApiClient.simSwappingService.getLocations(generalExtras.getInt("idUsuario"))

        call!!.enqueue(object : Callback<ResponseUbicaciones> {

            override fun onResponse(call: Call<ResponseUbicaciones>, response: Response<ResponseUbicaciones>) {
                try {
                    if (response.isSuccessful ) {
                        lstUbicaciones = response.body()?.lstUbicaciones!!
                        //Por cada ubicacion registrada se debe pintar en el mapa
                        for (objUbicacion in lstUbicaciones){
                            setMarkerIntoMap(objUbicacion.latitud!!.toDouble(), objUbicacion.longitud!!.toDouble())
                        }

                    }else {
                        Log.d("error","Error: ${response.message()}")
                        Toast.makeText(applicationContext,"Error: ${response}",Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseUbicaciones>, t: Throwable) {
                Log.d("error","Failure: ${t.printStackTrace()}")
                Toast.makeText(applicationContext,"Failure: ${t.message}",Toast.LENGTH_SHORT).show()
            }
        })
                //googleMapsSettings()
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionActual() {
        // Obtener la última ubicación conocida
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    // Utilizar la ubicación actual como latitudInicial y longitudInicial
                   setMarkerIntoMap(location.latitude,location.longitude)


                }
            }
    }

    private fun setMarkerIntoMap(latitudInicial: Double, longitudInicial: Double) {

        // Inicializar el mapa y configurarlo con la ubicación actual
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync { map ->
            googleMap = map

            val initialLocation = LatLng(latitudInicial, longitudInicial)
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(initialLocation, 16f)
            )

            //Logica que permite agregar más marcadores y tomar su evento
            googleMap.setOnMapClickListener(this)
            googleMap.setOnMarkerClickListener(this)

            //Marcador Inicial por ubicación actual
            setMarketWithOptions(googleMap, latitudInicial, longitudInicial)

            googleMap.addMarker(
                MarkerOptions()
                    .position(initialLocation)
                    .title("Marcador Inicial")

            )
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

            // Habilita los controles de zoom
            googleMap.uiSettings.isZoomControlsEnabled = true
        }
    }


    private fun setMarketWithOptions(googleMap: GoogleMap, latitudInicial: Double, longitudInicial: Double) {
        val marcador = LatLng(latitudInicial, longitudInicial)
        val radioMetros = UtilsShared.RADIOUS_METER
        val circleOptions = CircleOptions()
            .center(marcador)
            .radius(radioMetros)
            .strokeColor(R.color.color95AFD2)
            .fillColor(Color.argb(70, 211, 225, 247))

        googleMap.addCircle(circleOptions)
    }

    override fun onMapClick(point: LatLng) {
        if (markerCount < 2) {
            // Añadir marcador
            googleMap.addMarker(MarkerOptions().position(point).title("Marcador ${markerCount + 1}"))

            // Agregar el punto al conjunto de marcadores
            markers.add(point)

            val latitud = point.latitude
            val longitud = point.longitude


            //Registramos ubicación
            val objUbicacion = Ubicaciones(
                id_ubicacion = 0,
                id_usuario = generalExtras.getInt("idUsuario"),
                latitud = latitud,
                longitud = longitud,
                estado = ""
            )
            val call = ApiClient.simSwappingService.registerLocation(objUbicacion)
            call!!.enqueue(object : Callback<ResponseUbicaciones> {

                override fun onResponse(call: Call<ResponseUbicaciones>, response: Response<ResponseUbicaciones>) {
                    if (response.isSuccessful ) {
                        setMarketWithOptions(googleMap, latitud,longitud)
                    }else {
                        Log.d("error","Error: ${response.message()}")
                        Toast.makeText(applicationContext,"Error: ${response}",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ResponseUbicaciones>, t: Throwable) {
                    Log.d("error", "Failure: ${t.printStackTrace()}")
                }
            })

            // Incrementar el contador de marcadores
            markerCount++

            // Si hay dos marcadores, dibujar una línea entre ellos
            if (markerCount == 2) {
                drawPolyline()
            }
        } else {
            Toast.makeText(this, "Ya has agregado los dos marcadores", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onMarkerClick(marker: Marker): Boolean {
        //Al realizar click sobre el marcador, podremos eliminar el punto
        //PENDIENTE DE REALIZACIÓN
        marker.remove()
        Toast.makeText(this, "Punto removido", Toast.LENGTH_SHORT).show()
        /*val position = marker.position
        val latitud = position.latitude
        val longitud = position.longitude*/

        // Mostrar la información en un Toast
        //Toast.makeText(this, "Marcador seleccionado\nLatitud: $latitud\nLongitud: $longitud", Toast.LENGTH_SHORT).show()

        return true
    }

    private fun drawPolyline() {
        // Dibujar una línea entre los dos marcadores
        polyline?.remove()
        polyline = googleMap.addPolyline(
            PolylineOptions()
                .addAll(markers)
                .color(android.R.color.holo_red_dark)
                .width(5f)
        )

        // Ajustar la cámara para que todos los marcadores y la línea sean visibles
        val bounds = LatLngBounds.builder().include(markers[0]).include(markers[1]).build()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50))
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
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}