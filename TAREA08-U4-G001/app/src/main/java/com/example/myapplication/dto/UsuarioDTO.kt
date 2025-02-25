package com.example.myapplication.dto

import com.example.myapplication.Usuario

data class UsuarioDTO(
    val id: Int,
    val nombreUsuario: String,
    val contrasenia: String,
    val vehiculos: List<VehiculoDTO>? = null
)

fun Usuario.toDTO(): UsuarioDTO {
    return UsuarioDTO(
        id = this.id,
        nombreUsuario = this.nombreUsuario,
        contrasenia = this.contrasenia,
        vehiculos = this.vehiculos.map { it.toDTO() }
    )
}

fun UsuarioDTO.toEntity(): Usuario {
    return Usuario(
        id = this.id,
        nombreUsuario = this.nombreUsuario,
        contrasenia = this.contrasenia,
        vehiculos = this.vehiculos?.map { it.toEntity() } ?: emptyList()
    )
}