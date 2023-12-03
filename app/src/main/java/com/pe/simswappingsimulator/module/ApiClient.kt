package com.pe.simswappingsimulator.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pe.simswappingsimulator.services.SimSwappingService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private var retrofit: Retrofit? = null
    private const val BASE_URL = "http://ec2-3-22-236-186.us-east-2.compute.amazonaws.com:8080/WSSimSwapping-1.0/simswapping/"
    private val BA_USER = "admin"
    private val BA_PASS = "12345678"

    private val loggingInterceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    //private const val BASE_URL = "http://localhost:8080/" //PRD
    val gson: Gson = GsonBuilder()
        .setLenient() // Esto permite JSON no estricto
        .create()

    fun getClient(username: String, password: String): Retrofit {
        if (retrofit == null) {
            val httpClient = OkHttpClient.Builder()
            httpClient
                .addInterceptor(loggingInterceptor)
                .addInterceptor(BasicAuthInterceptor(username, password))

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()
        }
        return retrofit!!
    }/*

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
*/
    val simSwappingService: SimSwappingService = getClient(BA_USER, BA_PASS).create(SimSwappingService::class.java)
}