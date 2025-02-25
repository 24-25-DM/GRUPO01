@file:Suppress("DEPRECATION")

package com.example.myapplication.dto

import com.example.myapplication.Vehiculo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VehiculoDTO(
    val id: Int,
    val placa: String,
    val marca: String,
    val fechaFabricacion: String,
    val color: String,
    val precio: Double,
    val disponible: Boolean,
    val imageResource: String,
    val usuarioId: Int
)

fun Vehiculo.toDTO(): VehiculoDTO {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return VehiculoDTO(
        id = this.id,
        placa = this.placa,
        marca = this.marca,
        fechaFabricacion = if (this.fechaFabricacion != null) dateFormat.format(this.fechaFabricacion) else "",
        color = this.color,
        precio = this.precio,
        disponible = this.disponible,
        imageResource = this.imageResource,
        usuarioId = this.usuarioId
    )
}

fun VehiculoDTO.toEntity(): Vehiculo {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    return Vehiculo(
        id = this.id,
        placa = this.placa,
        marca = this.marca,
        fechaFabricacion = try {
            // Intentar parsear diferentes formatos de fecha
            when {
                // Si solo es año
//                fechaFabricacion.matches(Regex("^\\d{4}$")) -> {
//                    SimpleDateFormat("yyyy", Locale.getDefault()).parse(fechaFabricacion)
//                }
                // Si es formato completo dd/MM/yyyy
                fechaFabricacion.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$")) -> {
                    dateFormat.parse(fechaFabricacion)
                }
                // Si está en formato largo de Java
//                fechaFabricacion.contains("GMT") -> {
//                    try {
//                        // Intenta convertir el formato largo de Java
//                        val date = Date(fechaFabricacion)
//                        // Y formatearlo correctamente
//                        dateFormat.format(date)
//                        date
//                    } catch (e: Exception) {
//                        null
//                    }
//                }
                // Si está vacío
                fechaFabricacion.isEmpty() -> null
                else -> null
            }
        } catch (e: Exception) {
            null // Si hay error en el parseo, guardamos null
        },
        color = this.color,
        precio = this.precio,
        disponible = this.disponible,
        imageResource = this.imageResource,
        usuarioId = this.usuarioId
    )
}