package com.pe.simswappingsimulator.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        //btnTransferir

    }
}