package com.pe.simswappingsimulator.model

data class Ubicaciones (
    val id_ubicacion: Int,
    val id_usuario:Int,
    val latitud: Double,
    val longitud: Double,
    val estado: String?
)