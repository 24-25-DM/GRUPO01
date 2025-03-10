package com.example.myapplication.modelo

import java.io.Serializable
import java.util.Date

data class Vehiculo(
    val id: Int,
    val placa: String,
    val marca: String,
    val fechaFabricacion: Date?,
    val color: String,
    val precio: Double,
    val disponible: Boolean,
    var imageResource: String,
    val usuarioId: Int
) : Serializable