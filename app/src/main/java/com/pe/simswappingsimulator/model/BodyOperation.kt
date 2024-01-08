package com.pe.simswappingsimulator.model

data class BodyOperation (
    val id_operation: Int,
    val id_usuario: Int,
    val monto: Double,
    val cuenta_destino: String,
    val cuenta_origen: String,
    val latitud: Double,
    val longitud: Double

)
