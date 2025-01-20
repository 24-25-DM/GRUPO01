package repo

import android.content.ContentValues
import android.util.Log
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.Vehiculo
import java.text.SimpleDateFormat
import java.util.Date

class VehiculoRepository(private val dbHelper: DatabaseHelper) {

    fun insertVehiculo(vehiculo: Vehiculo, usuarioId: String): Long {
        val db = dbHelper.writableDatabase

        // Generar el siguiente ID para el vehículo
        val nextId = dbHelper.getNextVehicleId(usuarioId)

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_VEHICULO_ID, nextId)
            put(DatabaseHelper.COLUMN_VEHICULO_PLACA, vehiculo.placa)
            put(DatabaseHelper.COLUMN_VEHICULO_MARCA, vehiculo.marca)
            put(DatabaseHelper.COLUMN_VEHICULO_FECHA_FABRICACION, SimpleDateFormat("dd/MM/yyyy").format(vehiculo.fechaFabricacion))
            put(DatabaseHelper.COLUMN_VEHICULO_COLOR, vehiculo.color)
            put(DatabaseHelper.COLUMN_VEHICULO_PRECIO, vehiculo.precio)
            put(DatabaseHelper.COLUMN_VEHICULO_DISPONIBLE, if (vehiculo.disponible) 1 else 0)
            put(DatabaseHelper.COLUMN_VEHICULO_IMAGEN, vehiculo.imageResource)
            put(DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID, usuarioId)
        }
        return db.insert(DatabaseHelper.TABLE_VEHICULO, null, values)
    }

    fun getVehiculosByUsuarioId(usuarioId: String): List<Vehiculo> {
        val db = dbHelper.readableDatabase
        val vehiculos = mutableListOf<Vehiculo>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        val cursor = db.query(
            DatabaseHelper.TABLE_VEHICULO,
            null,
            "${DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID} = ?",
            arrayOf(usuarioId),
            null,
            null,
            null
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                val vehicle = Vehiculo(
                    id = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_ID)),
                    placa = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_PLACA)),
                    marca = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_MARCA)),
                    fechaFabricacion = dateFormat.parse(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_FECHA_FABRICACION)))!!,
                    color = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_COLOR)),
                    precio = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_PRECIO)),
                    disponible = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_DISPONIBLE)) == 1,
                    imageResource = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VEHICULO_IMAGEN))
                )
                vehiculos.add(vehicle)
                Log.d("DatabaseHelper", "Vehículo cargado: ${vehicle.placa}")
            }
        }

        return vehiculos
    }

    // Update
    fun updateVehicle(vehicle: Vehiculo, userId: String): Int {
        val db = dbHelper.writableDatabase
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_VEHICULO_PLACA, vehicle.placa)
            put(DatabaseHelper.COLUMN_VEHICULO_MARCA, vehicle.marca)
            put(DatabaseHelper.COLUMN_VEHICULO_FECHA_FABRICACION, dateFormat.format(vehicle.fechaFabricacion))
            put(DatabaseHelper.COLUMN_VEHICULO_COLOR, vehicle.color)
            put(DatabaseHelper.COLUMN_VEHICULO_PRECIO, vehicle.precio)
            put(DatabaseHelper.COLUMN_VEHICULO_DISPONIBLE, if (vehicle.disponible) 1 else 0)
            put(DatabaseHelper.COLUMN_VEHICULO_IMAGEN, vehicle.imageResource)
        }

        return db.update(
            DatabaseHelper.TABLE_VEHICULO,
            values,
            "${DatabaseHelper.COLUMN_VEHICULO_ID} = ? AND ${DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID} = ?",
            arrayOf(vehicle.id, userId)
        )
    }

    // Delete
    fun deleteVehicle(vehicleId: String, userId: String): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_VEHICULO,
            "${DatabaseHelper.COLUMN_VEHICULO_ID} = ? AND ${DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID} = ?",
            arrayOf(vehicleId, userId)
        )
    }

}
