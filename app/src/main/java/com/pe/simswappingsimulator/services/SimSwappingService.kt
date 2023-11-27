package com.pe.simswappingsimulator.services

import com.pe.simswappingsimulator.model.BodyAccount
import com.pe.simswappingsimulator.model.BodyLogin
import retrofit2.Call;
import retrofit2.http.Body
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;



interface SimSwappingService {
    @POST("registerAccount")
    fun registerAccount(@Body bodyAccount: BodyAccount): Call<Int>

    @POST("validateLogin")
    fun validateLogin(@Body bodyLogin: BodyLogin): Call<BodyLogin?>?
    //fun validateLogin(@Path("cc") cc: String,@Path("pin") passcode:String,): Call<Integer?>?
}