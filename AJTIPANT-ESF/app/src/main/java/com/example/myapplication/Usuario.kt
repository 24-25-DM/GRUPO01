package com.example.myapplication

import java.io.Serializable

data class Usuario(
    val id: Int,
    val nombreUsuario: String,
    val contrasenia: String,
    val vehiculos: List<Vehiculo> = listOf()
) : Serializable