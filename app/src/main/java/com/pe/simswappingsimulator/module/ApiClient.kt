package com.pe.simswappingsimulator.module

import com.pe.simswappingsimulator.services.SimSwappingService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {


    private const val BASE_URL = "http://ec2-3-22-236-186.us-east-2.compute.amazonaws.com:8080/WSSimSwapping-1.0/simswapping"
    //private const val BASE_URL = "http://localhost:8080/" //PRD

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val simSwappingService: SimSwappingService = retrofit.create(SimSwappingService::class.java)
}