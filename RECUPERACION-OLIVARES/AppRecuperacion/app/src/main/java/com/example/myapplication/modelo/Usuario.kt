package com.example.myapplication.modelo

import java.io.Serializable

data class Usuario(
    val id: Int,
    val nombreUsuario: String,
    val contrasenia: String,
    val ultimoIngreso: String? = null,
    val vehiculos: List<Vehiculo> = listOf()
) : Serializable