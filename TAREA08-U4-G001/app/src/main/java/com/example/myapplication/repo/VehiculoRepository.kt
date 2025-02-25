package com.example.myapplication.repo

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.Vehiculo
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VehiculoRepository(private val dbHelper: DatabaseHelper) {

    // Método para insertar un vehículo
    fun insertVehiculo(vehiculo: Vehiculo, usuarioId: Int): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            // No incluimos ID porque debería ser autoincremental
            put(DatabaseHelper.COLUMN_VEHICULO_PLACA, vehiculo.placa)
            put(DatabaseHelper.COLUMN_VEHICULO_MARCA, vehiculo.marca)
            put(DatabaseHelper.COLUMN_VEHICULO_FECHA_FABRICACION, formatDate(vehiculo.fechaFabricacion))
            put(DatabaseHelper.COLUMN_VEHICULO_COLOR, vehiculo.color)
            put(DatabaseHelper.COLUMN_VEHICULO_PRECIO, vehiculo.precio)
            put(DatabaseHelper.COLUMN_VEHICULO_DISPONIBLE, if (vehiculo.disponible) 1 else 0)
            put(DatabaseHelper.COLUMN_VEHICULO_IMAGEN, vehiculo.imageResource)
            put(DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID, usuarioId)
        }
        return db.insert(DatabaseHelper.TABLE_VEHICULO, null, values)
    }

    // Método para actualizar un vehículo
    fun updateVehicle(vehiculo: Vehiculo, usuarioId: Int): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_VEHICULO_PLACA, vehiculo.placa)
            put(DatabaseHelper.COLUMN_VEHICULO_MARCA, vehiculo.marca)
            put(DatabaseHelper.COLUMN_VEHICULO_FECHA_FABRICACION, formatDate(vehiculo.fechaFabricacion))
            put(DatabaseHelper.COLUMN_VEHICULO_COLOR, vehiculo.color)
            put(DatabaseHelper.COLUMN_VEHICULO_PRECIO, vehiculo.precio)
            put(DatabaseHelper.COLUMN_VEHICULO_DISPONIBLE, if (vehiculo.disponible) 1 else 0)
            put(DatabaseHelper.COLUMN_VEHICULO_IMAGEN, vehiculo.imageResource)
        }
        return db.update(
            DatabaseHelper.TABLE_VEHICULO,
            values,
            "${DatabaseHelper.COLUMN_VEHICULO_ID} = ? AND ${DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID} = ?",
            arrayOf(vehiculo.id.toString(), usuarioId.toString())
        )
    }

    // Método para eliminar un vehículo
    fun deleteVehicle(vehiculoId: String, usuarioId: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_VEHICULO,
            "${DatabaseHelper.COLUMN_VEHICULO_ID} = ? AND ${DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID} = ?",
            arrayOf(vehiculoId, usuarioId.toString())
        )
    }

    // Método para obtener un vehículo por ID
    fun getVehiculoById(vehiculoId: String): Vehiculo? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_VEHICULO,
            null,
            "${DatabaseHelper.COLUMN_VEHICULO_ID} = ?",
            arrayOf(vehiculoId),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                cursorToVehiculo(it)
            } else {
                null
            }
        }
    }

    // Método para obtener todos los vehículos de un usuario
    fun getVehiculosByUsuarioId(usuarioId: Int): List<Vehiculo> {
        val vehiculos = mutableListOf<Vehiculo>()
        val db = dbHelper.readableDatabase

        try {
            val cursor = db.query(
                DatabaseHelper.TABLE_VEHICULO,
                null,
                "${DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID} = ?",
                arrayOf(usuarioId.toString()),
                null, null, null
            )

            cursor.use {
                while (it.moveToNext()) {
                    val vehiculo = cursorToVehiculo(it)
                    vehiculo?.let { v -> vehiculos.add(v) }
                }
            }
        } catch (e: Exception) {
            Log.e("VehiculoRepository", "Error al obtener vehículos: ${e.message}")
        }

        return vehiculos
    }

    // Método auxiliar para convertir un cursor en un objeto Vehiculo
    private fun cursorToVehiculo(cursor: Cursor): Vehiculo? {
        try {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_ID))
            val placa = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_PLACA))
            val marca = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_MARCA))
            val fechaStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_FECHA_FABRICACION))
            val color = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_COLOR))
            val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_PRECIO))
            val disponible = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_DISPONIBLE)) == 1
            val imagen = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_IMAGEN))
            val usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID))

            // Usar el método parseDate para manejar múltiples formatos de fecha
            val fechaFabricacion = parseDate(fechaStr)

            return Vehiculo(
                id = id,
                placa = placa,
                marca = marca,
                fechaFabricacion = fechaFabricacion,
                color = color,
                precio = precio,
                disponible = disponible,
                imageResource = imagen,
                usuarioId = usuarioId
            )
        } catch (e: Exception) {
            Log.e("VehiculoRepository", "Error al convertir cursor a vehículo: ${e.message}")
            return null
        }
    }

    // Método para formatear fechas al guardar
    private fun formatDate(date: Date?): String {
        if (date == null) return ""
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        } catch (e: Exception) {
            Log.e("VehiculoRepository", "Error al formatear fecha: ${e.message}")
            ""
        }
    }

    // Método para analizar fechas con múltiples formatos
    private fun parseDate(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) return null

        // Lista de posibles formatos de fecha para probar
        val formatPatterns = arrayOf(
            "dd/MM/yyyy",                       // Tu formato esperado
            "EEE MMM dd HH:mm:ss z yyyy",       // Formato completo con zona horaria (predeterminado de Java)
            "EEE MMM dd HH:mm:ss 'GMT'Z yyyy"   // Formato visto en tu error
        )

        for (pattern in formatPatterns) {
            try {
                val format = SimpleDateFormat(pattern, Locale.US)
                return format.parse(dateString)
            } catch (e: ParseException) {
                // Intentar con el siguiente patrón
            }
        }

        // Si llegamos aquí, ninguno de los formatos funcionó
        Log.e("VehiculoRepository", "No se pudo analizar la fecha: $dateString")
        return null
    }
}