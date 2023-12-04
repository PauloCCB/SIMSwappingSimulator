package com.pe.simswappingsimulator.model

data class ResponseAccount (

    val usuario: BodyLogin,
    val success: Boolean,
    val message: String

)