package com.pe.simswappingsimulator.model

data class ResponseUbicaciones (
    val lstUbicaciones: List<Ubicaciones>,
    val success: Boolean,
    val message: String
)