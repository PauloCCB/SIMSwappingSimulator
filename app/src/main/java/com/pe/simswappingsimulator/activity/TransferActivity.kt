package com.pe.simswappingsimulator.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pe.simswappingsimulator.R
import com.pe.simswappingsimulator.databinding.ActivityTransferBinding

class TransferActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransferBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransferBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_transfer)

        supportActionBar?.apply {
            title = "Transferir dinero"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }


}