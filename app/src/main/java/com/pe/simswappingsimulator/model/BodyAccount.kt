package com.pe.simswappingsimulator.model

data class BodyAccount(
    val dni: String,
    val nombre: String,
    val apellido: String,
    val cc: String,
    val latitude: String,
    val longitude: String,
    val pin: String,
    val telefono: String?
)