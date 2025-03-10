package com.example.myapplication.dto

import com.example.myapplication.modelo.Usuario

data class UsuarioDTO(
    val id: Int,
    val nombreUsuario: String,
    val contrasenia: String,
    val ultimoIngreso: String? = null,
    val vehiculos: List<VehiculoDTO>? = null
)

fun Usuario.toDTO(): UsuarioDTO {
    return UsuarioDTO(
        id = this.id,
        nombreUsuario = this.nombreUsuario,
        contrasenia = this.contrasenia,
        ultimoIngreso = this.ultimoIngreso,
        vehiculos = this.vehiculos.map { it.toDTO() }
    )
}

fun UsuarioDTO.toEntity(): Usuario {
    return Usuario(
        id = this.id,
        nombreUsuario = this.nombreUsuario,
        contrasenia = this.contrasenia,
        ultimoIngreso = this.ultimoIngreso,
        vehiculos = this.vehiculos?.map { it.toEntity() } ?: emptyList()
    )
}