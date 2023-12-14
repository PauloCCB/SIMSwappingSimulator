package com.pe.simswappingsimulator.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityTransferBinding
import com.pe.simswappingsimulator.model.BodyOperation
import com.pe.simswappingsimulator.module.ApiClient

class TransferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferBinding
    private var generalExtras = Bundle()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ActionBarTheme)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        /*setContentView(R.layout.activity_transfer)*/
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

                    /*Toast.makeText(
                        this,
                        "Latitud: $latitude, Longitud: $longitude",
                        Toast.LENGTH_SHORT
                    ).show()*/
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
                // Concatenar las dos partes
                binding.txtNroCuentaOrigen.text  = "$primeraParte$resto"
            }
            generalExtras = extras
        }
    }

    private fun setOnClickListener() {
        binding.btnRegistrar.setOnClickListener{
            binding.txtCuentaDestino.text
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
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}