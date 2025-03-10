package com.example.myapplication

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.myapplication.modelo.Usuario
import com.example.myapplication.modelo.Vehiculo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "vehiculos_usuarios.db"
        const val DATABASE_VERSION = 2

        // Tabla Usuario
        const val TABLE_USUARIO = "usuarios"
        const val COLUMN_USUARIO_ID = "id"
        const val COLUMN_USUARIO_NOMBRE = "nombreUsuario"
        const val COLUMN_USUARIO_CONTRASENIA = "contrasenia"
        const val COLUMN_USUARIO_ULTIMO_INGRESO = "ultimoIngreso"

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
        private const val LAST_DEFAULT_USER_ID = 3

        // Tabla Log
        const val TABLE_LOG = "log"
        const val COLUMN_LOG_ID = "id"
        const val COLUMN_LOG_ACTION = "actions"
        const val COLUMN_LOG_USUARIO_ID = "usuario_id"
        const val COLUMN_LOG_FECHA = "fecha"
        const val COLUMN_LOG_DETALLES = "detalles"
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            // Crear tabla Usuario
            val createUsuarioTable = """
        CREATE TABLE $TABLE_USUARIO (
            $COLUMN_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_USUARIO_NOMBRE TEXT NOT NULL,
            $COLUMN_USUARIO_CONTRASENIA TEXT NOT NULL,
            $COLUMN_USUARIO_ULTIMO_INGRESO TEXT DEFAULT NULL
        )
        """.trimIndent()
            db.execSQL(createUsuarioTable)

            // Crear tabla Vehiculo second
            val createVehiculoTable = """
        CREATE TABLE $TABLE_VEHICULO (
            $COLUMN_VEHICULO_ID INTEGER PRIMARY KEY,
            $COLUMN_VEHICULO_PLACA TEXT NOT NULL,
            $COLUMN_VEHICULO_MARCA TEXT NOT NULL,
            $COLUMN_VEHICULO_FECHA_FABRICACION TEXT NOT NULL,
            $COLUMN_VEHICULO_COLOR TEXT NOT NULL,
            $COLUMN_VEHICULO_PRECIO REAL NOT NULL,
            $COLUMN_VEHICULO_DISPONIBLE INTEGER NOT NULL,
            $COLUMN_VEHICULO_IMAGEN INTEGER NOT NULL,
            $COLUMN_VEHICULO_USUARIO_ID INTEGER NOT NULL,
            FOREIGN KEY ($COLUMN_VEHICULO_USUARIO_ID) REFERENCES $TABLE_USUARIO($COLUMN_USUARIO_ID)
        )
        """.trimIndent()
            db.execSQL(createVehiculoTable)

            // Crear tabla Log
            val createLogTable = """
        CREATE TABLE $TABLE_LOG (
            $COLUMN_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_LOG_ACTION TEXT NOT NULL,
            $COLUMN_LOG_USUARIO_ID INTEGER NOT NULL,
            $COLUMN_LOG_FECHA TEXT NOT NULL,
            $COLUMN_LOG_DETALLES TEXT,
            FOREIGN KEY ($COLUMN_LOG_USUARIO_ID) REFERENCES $TABLE_USUARIO($COLUMN_USUARIO_ID)
        )
        """.trimIndent()
            db.execSQL(createLogTable)

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
                1,
                "Luis",
                "44a2ff61a610836592152ca0eb7e07847110586694212dbafda566d37363437d"
            ),
            Usuario(
                2,
                "Cristian",
                "e6c1af9b645640bb62e3ad9eaf0f8c7dde67ab9c7b20daf8535298336c11d5bd"
            ),
            Usuario(
                3,
                "Anthony",
                "c26447b5df3186ae36fb403c3b35c05d8484e593c822ac097251974a45d0ad54"
            )
        )

        users.forEach { user ->
            val values = ContentValues().apply {
                put(COLUMN_USUARIO_ID, user.id)
                put(COLUMN_USUARIO_NOMBRE, user.nombreUsuario)
                put(COLUMN_USUARIO_CONTRASENIA, user.contrasenia)
                // Inicialmente no tienen fecha de último ingreso
                put(COLUMN_USUARIO_ULTIMO_INGRESO, user.ultimoIngreso)
            }
            db.insert(TABLE_USUARIO, null, values)

            // Insertar vehículos por defecto para cada usuario
            insertarVehiculosPorDefecto(db, user.id)
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun insertarVehiculosPorDefecto(db: SQLiteDatabase, id: Int) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val defaultVehicles = arrayOf(
            Vehiculo(
                0,
                "ABC-1234",
                "Toyota",
                dateFormat.parse("01/01/2020")!!,
                "Blanco",
                20000.0,
                true,
                R.drawable.ic_vehicle.toString(),
                0
            ),
            Vehiculo(
                0,
                "XYZ-7898",
                "Honda",
                dateFormat.parse("15/08/2018")!!,
                "Negro",
                15000.0,
                false,
                R.drawable.ic_vehicle.toString(),
                0
            ),
            Vehiculo(
                0,
                "LMN-4567",
                "Ford",
                dateFormat.parse("10/12/2021")!!,
                "Azul",
                18000.0,
                true,
                R.drawable.ic_vehicle.toString(),
                0
            )
        )

        // Asignar los vehículos a cada usuario
        defaultVehicles.forEach { vehicle ->
            val values = ContentValues().apply {
                put(COLUMN_VEHICULO_PLACA, vehicle.placa)
                put(COLUMN_VEHICULO_MARCA, vehicle.marca)
                put(
                    COLUMN_VEHICULO_FECHA_FABRICACION,
                    dateFormat.format(vehicle.fechaFabricacion!!)
                )
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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LOG")
    }

    fun getAllUsuarios(): List<Usuario> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USUARIO,
            null,
            null,
            null,
            null,
            null,
            null
        )

        val usuarios = mutableListOf<Usuario>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_NOMBRE))
            val contrasenia =
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USUARIO_CONTRASENIA))

            val ultimoIngreso = cursor.getColumnIndex(COLUMN_USUARIO_ULTIMO_INGRESO).let {
                if (it != -1 && !cursor.isNull(it)) cursor.getString(it) else null
            }

            // Obtener vehículos asociados al usuario
            val vehiculos = getVehiculosByUsuario(id)

            // Crear el objeto Usuario con la lista de vehículos
            usuarios.add(Usuario(id, nombre, contrasenia, ultimoIngreso, vehiculos))
        }
        cursor.close()
        return usuarios
    }

    fun getAllVehiculos(): List<Vehiculo> {
        val vehiculos = mutableListOf<Vehiculo>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_VEHICULO,
            null,
            null,
            null,
            null,
            null,
            null
        )

        cursor.use {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            while (it.moveToNext()) {
                vehiculos.add(
                    Vehiculo(
                        id = it.getInt(it.getColumnIndexOrThrow(COLUMN_VEHICULO_ID)),
                        placa = it.getString(it.getColumnIndexOrThrow(COLUMN_VEHICULO_PLACA)),
                        marca = it.getString(it.getColumnIndexOrThrow(COLUMN_VEHICULO_MARCA)),
                        fechaFabricacion = dateFormat.parse(
                            it.getString(
                                it.getColumnIndexOrThrow(
                                    COLUMN_VEHICULO_FECHA_FABRICACION
                                )
                            )
                        ) ?: Date(),
                        color = it.getString(it.getColumnIndexOrThrow(COLUMN_VEHICULO_COLOR)),
                        precio = it.getDouble(it.getColumnIndexOrThrow(COLUMN_VEHICULO_PRECIO)),
                        disponible = it.getInt(it.getColumnIndexOrThrow(COLUMN_VEHICULO_DISPONIBLE)) == 1,
                        imageResource = it.getInt(it.getColumnIndexOrThrow(COLUMN_VEHICULO_IMAGEN))
                            .toString(),
                        usuarioId = it.getInt(it.getColumnIndexOrThrow(COLUMN_VEHICULO_USUARIO_ID))
                    )
                )
            }
        }

        return vehiculos
    }

    @SuppressLint("SimpleDateFormat")
    fun getVehiculosByUsuario(usuarioId: Int): List<Vehiculo> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_VEHICULO,
            null,
            "$COLUMN_VEHICULO_USUARIO_ID = ?",
            arrayOf(usuarioId.toString()),
            null,
            null,
            null
        )

        val vehiculos = mutableListOf<Vehiculo>()

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VEHICULO_ID))
            val placa = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICULO_PLACA))
            val marca = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICULO_MARCA))
            val fechaFabricacionStr =
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICULO_FECHA_FABRICACION))
            val color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICULO_COLOR))
            val precio = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_VEHICULO_PRECIO))
            val disponible =
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VEHICULO_DISPONIBLE)) == 1
            val imageResource =
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICULO_IMAGEN))

            // Parsear la fecha de forma más robusta
            val fechaFabricacion = parseMultipleFormatDate(fechaFabricacionStr)

            vehiculos.add(
                Vehiculo(
                    id,
                    placa,
                    marca,
                    fechaFabricacion,
                    color,
                    precio,
                    disponible,
                    imageResource,
                    usuarioId
                )
            )
        }
        cursor.close()
        return vehiculos
    }

    // Método para parsear fechas en múltiples formatos
    private fun parseMultipleFormatDate(dateString: String): Date? {
        if (dateString.isEmpty()) return null

        // Formatos comunes a intentar
        val formats = arrayOf(
            "dd/MM/yyyy",
            "EEE MMM dd HH:mm:ss 'GMT'Z yyyy",
            "EEE MMM dd HH:mm:ss z yyyy"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.US)
                return sdf.parse(dateString)
            } catch (e: Exception) {
                // Intentar con el siguiente formato
                continue
            }
        }

        // Si nada funciona, intenta crear una fecha a partir del String directamente
        try {
            @Suppress("DEPRECATION")
            return Date(dateString)
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "No se pudo parsear la fecha: $dateString", e)
            return null
        }
    }
}