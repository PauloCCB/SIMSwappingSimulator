package com.pe.simswappingsimulator.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pe.simswappingsimulator.databinding.ActivityHomeBinding


class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private var generalExtras = Bundle()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        initView()

        setOnClickListeners()
    }

    private fun initView() {
        val intent: Intent = intent
        val extras: Bundle? = intent.extras

        if (extras != null) {

            with(extras){

                binding.tvSaldo.text = "S/. ${getDouble("saldo")}"
                val acc = getString("cc")
                val primeraParte = "********".substring(0, minOf(acc!!.length, 8))
                // Mantener el resto de la cadena sin cambios
                val resto = acc!!.substring(minOf(acc!!.length, 8))

                // Concatenar las dos partes
                binding.tvNroCuenta.text  = "$primeraParte$resto"
            }
            generalExtras = extras
        }
    }

    private fun setOnClickListeners() {
        binding.btnTransferir.setOnClickListener {
            val intent = Intent(this@Home, TransferActivity::class.java)
            intent.putExtras(generalExtras)
            startActivity(intent)
        }

    }
}