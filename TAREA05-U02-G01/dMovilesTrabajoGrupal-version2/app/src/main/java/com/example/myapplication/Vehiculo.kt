package com.example.myapplication

import java.io.Serializable
import java.util.Date
import java.util.UUID

data class Vehiculo(
    val id: String = UUID.randomUUID().toString(),
    val placa: String,
    val marca: String,
    val fechaFabricacion: Date,
    val color: String,
    val precio: Double,
    val disponible: Boolean,
    val imageResource: Int
) : Serializable