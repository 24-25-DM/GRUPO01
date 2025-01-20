package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "vehiculos_usuarios.db"
        const val DATABASE_VERSION = 1

        // Tabla Usuario
        const val TABLE_USUARIO = "usuarios"
        const val COLUMN_USUARIO_ID = "id"
        const val COLUMN_USUARIO_NOMBRE = "nombreUsuario"
        const val COLUMN_USUARIO_CONTRASENIA = "contrasenia"

        // Tabla Vehiculo
        const val TABLE_VEHICULO = "vehiculos"
        const val COLUMN_VEHICULO_ID = "id"
        const val COLUMN_VEHICULO_PLACA = "placa"
        const val COLUMN_VEHICULO_MARCA = "marca"
        const val COLUMN_VEHICULO_FECHA_FABRICACION = "fechaFabricacion"
        const val COLUMN_VEHICULO_COLOR = "color"
        const val COLUMN_VEHICULO_PRECIO = "precio"
        const val COLUMN_VEHICULO_DISPONIBLE = "disponible"
        const val COLUMN_VEHICULO_IMAGEN = "imageResource"
        const val COLUMN_VEHICULO_USUARIO_ID = "usuarioId"

        // Secuencia para IDs de usuario
        private const val SEQUENCE_TABLE = "sqlite_sequence"
        private const val LAST_DEFAULT_USER_ID = 3
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Crear tabla Usuario
            val createUsuarioTable = """
            CREATE TABLE $TABLE_USUARIO (
                $COLUMN_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USUARIO_NOMBRE TEXT NOT NULL,
                $COLUMN_USUARIO_CONTRASENIA TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createUsuarioTable)

            // Crear tabla Vehiculo
            val createVehiculoTable = """
            CREATE TABLE $TABLE_VEHICULO (
                $COLUMN_VEHICULO_ID TEXT PRIMARY KEY,
                $COLUMN_VEHICULO_PLACA TEXT NOT NULL,
                $COLUMN_VEHICULO_MARCA TEXT NOT NULL,
                $COLUMN_VEHICULO_FECHA_FABRICACION TEXT NOT NULL,
                $COLUMN_VEHICULO_COLOR TEXT NOT NULL,
                $COLUMN_VEHICULO_PRECIO REAL NOT NULL,
                $COLUMN_VEHICULO_DISPONIBLE INTEGER NOT NULL,
                $COLUMN_VEHICULO_IMAGEN INTEGER NOT NULL,
                $COLUMN_VEHICULO_USUARIO_ID TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_VEHICULO_USUARIO_ID) REFERENCES $TABLE_USUARIO($COLUMN_USUARIO_ID)
            )
        """.trimIndent()
            db.execSQL(createVehiculoTable)

            // Insertar usuarios por defecto
            insertarUsuariosPorDefecto(db)

            // Configurar la secuencia para el próximo ID
            configurarSecuenciaUsuarios(db)

            Log.d("DatabaseHelper", "Base de datos creada exitosamente")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error al crear la base de datos", e)
        }
    }

    private fun configurarSecuenciaUsuarios(db: SQLiteDatabase) {
        // Primero eliminamos cualquier valor existente para la tabla de usuarios
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = '$TABLE_USUARIO'")

        // Luego insertamos el valor correcto
        db.execSQL("INSERT INTO sqlite_sequence (name, seq) VALUES ('$TABLE_USUARIO', $LAST_DEFAULT_USER_ID)")
    }

    private fun insertarUsuariosPorDefecto(db: SQLiteDatabase) {
        val users = arrayOf(
            Usuario(
                "1",
                "Luis",
                "44a2ff61a610836592152ca0eb7e07847110586694212dbafda566d37363437d"
            ),
            Usuario(
                "2",
                "Cristian",
                "e6c1af9b645640bb62e3ad9eaf0f8c7dde67ab9c7b20daf8535298336c11d5bd"
            ),
            Usuario(
                "3",
                "Anthony",
                "c26447b5df3186ae36fb403c3b35c05d8484e593c822ac097251974a45d0ad54"
            )
        )

        users.forEach { user ->
            val values = ContentValues().apply {
                put(COLUMN_USUARIO_ID, user.id)
                put(COLUMN_USUARIO_NOMBRE, user.nombreUsuario)
                put(COLUMN_USUARIO_CONTRASENIA, user.contrasenia)
            }
            db.insert(TABLE_USUARIO, null, values)

            // Insertar vehículos por defecto para cada usuario
            insertarVehiculosPorDefecto(db, user.id)
        }
    }

     fun insertarVehiculosPorDefecto(db: SQLiteDatabase, id: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val defaultVehicles = arrayOf(
            Vehiculo(
                "1",
                "ABC-1234",
                "Toyota",
                dateFormat.parse("01/01/2020")!!,
                "Blanco",
                20000.0,
                true,
                R.drawable.ic_vehicle
            ),
            Vehiculo(
                "2",
                "XYZ-7898",
                "Honda",
                dateFormat.parse("15/08/2018")!!,
                "Negro",
                15000.0,
                false,
                R.drawable.ic_vehicle
            ),
            Vehiculo(
                "3",
                "LMN-4567",
                "Ford",
                dateFormat.parse("10/12/2021")!!,
                "Azul",
                18000.0,
                true,
                R.drawable.ic_vehicle
            )
        )

        // Asignar los vehículos a cada usuario
         defaultVehicles.forEach { vehicle ->
             val uniqueId = "${id}_${vehicle.id}"
             val values = ContentValues().apply {
                 put(COLUMN_VEHICULO_ID, uniqueId)
                 put(COLUMN_VEHICULO_PLACA, vehicle.placa)
                 put(COLUMN_VEHICULO_MARCA, vehicle.marca)
                 put(COLUMN_VEHICULO_FECHA_FABRICACION, dateFormat.format(vehicle.fechaFabricacion))
                 put(COLUMN_VEHICULO_COLOR, vehicle.color)
                 put(COLUMN_VEHICULO_PRECIO, vehicle.precio)
                 put(COLUMN_VEHICULO_DISPONIBLE, if (vehicle.disponible) 1 else 0)
                 put(COLUMN_VEHICULO_IMAGEN, vehicle.imageResource)
                 put(COLUMN_VEHICULO_USUARIO_ID, id)
             }
             db.insert(TABLE_VEHICULO, null, values)
         }
    }

    fun getNextUserId(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX($COLUMN_USUARIO_ID) FROM $TABLE_USUARIO", null)
        return cursor.use {
            if (it.moveToFirst()) {
                it.getInt(0) + 1
            } else {
                LAST_DEFAULT_USER_ID + 1
            }
        }
    }

    fun getNextVehicleId(userId: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT MAX(CAST(SUBSTR($COLUMN_VEHICULO_ID, LENGTH('${userId}_') + 1) AS INTEGER)) " +
                    "FROM $TABLE_VEHICULO " +
                    "WHERE $COLUMN_VEHICULO_ID LIKE '${userId}_%'",
            null
        )

        return cursor.use {
            val nextNumber = if (it.moveToFirst() && !it.isNull(0)) {
                it.getInt(0) + 1
            } else {
                1
            }
            "${userId}_$nextNumber"
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_VEHICULO")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIO")
        db?.let { onCreate(it) }
    }
}
