package com.example.myapplication

import java.io.Serializable
import java.util.UUID

data class Usuario(
    val id: String = UUID.randomUUID().toString(),
    val nombreUsuario : String,
    val contrasenia : String,
    val vehiculos : List<Vehiculo> = listOf()
) : Serializable