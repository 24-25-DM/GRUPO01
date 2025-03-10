package com.example.myapplication.dto

import android.util.Log
import com.example.myapplication.modelo.Vehiculo
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
    val usuarioId: Int,
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

    // Intentar parsear la fecha con varios formatos posibles
    val fecha = try {
        when {
            // Si la fecha está en formato API (Wed Jan 01 00:00:00 GMT-05:00 2020)
            fechaFabricacion.matches(Regex("^\\w{3}\\s\\w{3}\\s\\d{2}.*")) -> {
                try {
                    @Suppress("DEPRECATION")
                    Date(fechaFabricacion)
                } catch (e: Exception) {
                    null
                }
            }
            // Si solo es año
            fechaFabricacion.matches(Regex("^\\d{4}$")) -> {
                SimpleDateFormat("yyyy", Locale.getDefault()).parse(fechaFabricacion)
            }
            // Si es formato completo dd/MM/yyyy
            fechaFabricacion.matches(Regex("^\\d{2}/\\d{2}/\\d{4}$")) -> {
                dateFormat.parse(fechaFabricacion)
            }
            // Si está vacío
            fechaFabricacion.isEmpty() -> null
            else -> null
        }
    } catch (e: Exception) {
        Log.e("VehiculoDTO", "Error parseando fecha: $fechaFabricacion", e)
        null
    }

    return Vehiculo(
        id = this.id,
        placa = this.placa,
        marca = this.marca,
        fechaFabricacion = fecha,
        color = this.color,
        precio = this.precio,
        disponible = this.disponible,
        imageResource = this.imageResource,
        usuarioId = this.usuarioId
    )
}