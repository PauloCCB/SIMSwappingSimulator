package com.pe.simswappingsimulator.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityLoginBinding


class Login : AppCompatActivity(){

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)


        val view = binding.root
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

}