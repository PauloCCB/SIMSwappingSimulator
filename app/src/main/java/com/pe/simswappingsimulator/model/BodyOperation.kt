package com.pe.simswappingsimulator.model

data class BodyOperation (
    val id_operation: Integer,
    val id_usuario: Integer,
    val monto: Double,
    val cuenta_destino: String,
    val cuenta_origen: String,

)
