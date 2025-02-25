package com.example.myapplication

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myapplication.dto.VehiculoDTO
import com.example.myapplication.dto.toDTO
import com.example.myapplication.dto.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class SyncManager(
    private val dbHelper: DatabaseHelper,
    private val apiService: ApiService,
    private val networkManager: NetworkManager,
) {
    companion object {
        private const val TAG = "SyncManager"
    }

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun syncData(): Result<SyncResult.Success> = withContext(Dispatchers.IO) {
        try {
            if (!networkManager.isNetworkAvailable()) {
                Log.d(TAG, "No hay conexión a internet disponible")
                return@withContext Result.failure(RuntimeException("No network connection"))
            }

            // Sincronización de usuarios
            val usuariosLocales = dbHelper.getAllUsuarios()
            for (usuario in usuariosLocales) {
                try {
                    val usuarioDTO = usuario.toDTO()
                    val syncResponse = apiService.syncUsuarios(usuarioDTO)
                    handleResponse(syncResponse, "usuario")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al sincronizar usuario ${usuario.id}", e)
                }
            }

            val vehiculosLocales = dbHelper.getAllVehiculos()
            for (vehiculo in vehiculosLocales) {
                try {
                    val vehiculoDTO = vehiculo.toDTO()
                    val syncResponse = apiService.syncVehiculos(vehiculoDTO)
                    handleResponse(syncResponse, "vehículo")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al sincronizar vehículo ${vehiculo.id}", e)
                }
            }

            // Obtener datos del servidor
            val serverUsuarios = try {
                apiService.getUsuarios().body() ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener usuarios del servidor", e)
                emptyList()
            }

            val serverVehiculos = try {
                apiService.getVehiculos().body() ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener vehículos del servidor", e)
                emptyList()
            }

            // Actualizar base de datos local con datos del servidor
            dbHelper.writableDatabase.use { db ->
                db.beginTransaction()
                try {
                    // Actualizar usuarios
                    db.delete(DatabaseHelper.TABLE_USUARIO, null, null) // Limpiar tabla
                    serverUsuarios.forEach { usuarioDTO ->
                        val usuario = usuarioDTO.toEntity()
                        insertarUsuario(db, usuario)
                    }

                    // Actualizar vehículos
                    db.delete(DatabaseHelper.TABLE_VEHICULO, null, null)
                    serverVehiculos.forEach { vehiculoDTO ->
                        val vehiculo = vehiculoDTO.toEntity()
                        insertarVehiculo(db, vehiculo)
                    }

                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }

            Result.success(SyncResult.Success(
                syncedCount = serverUsuarios.size + serverVehiculos.size
            ))
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización", e)
            Result.failure(e)
        }
    }

    private fun insertarVehiculo(db: SQLiteDatabase, vehiculo: Vehiculo) {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_VEHICULO_ID, vehiculo.id)
            put(DatabaseHelper.COLUMN_VEHICULO_PLACA, vehiculo.placa)
            put(DatabaseHelper.COLUMN_VEHICULO_MARCA, vehiculo.marca)
            put(DatabaseHelper.COLUMN_VEHICULO_FECHA_FABRICACION, vehiculo.fechaFabricacion?.toString() ?: "")
            put(DatabaseHelper.COLUMN_VEHICULO_COLOR, vehiculo.color)
            put(DatabaseHelper.COLUMN_VEHICULO_PRECIO, vehiculo.precio)
            put(DatabaseHelper.COLUMN_VEHICULO_DISPONIBLE, if (vehiculo.disponible) 1 else 0)
            put(DatabaseHelper.COLUMN_VEHICULO_IMAGEN, vehiculo.imageResource)
            put(DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID, vehiculo.usuarioId)
        }
        db.insert(DatabaseHelper.TABLE_VEHICULO, null, values)
    }

    private fun actualizarUsuario(db: SQLiteDatabase, usuario: Usuario) {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USUARIO_NOMBRE, usuario.nombreUsuario)
            put(DatabaseHelper.COLUMN_USUARIO_CONTRASENIA, usuario.contrasenia)
            // Agregar cualquier otro campo que necesite ser actualizado
        }

        db.update(
            DatabaseHelper.TABLE_USUARIO,
            values,
            "${DatabaseHelper.COLUMN_USUARIO_ID} = ?",
            arrayOf(usuario.id.toString())
        )
    }

    private fun insertarUsuario(db: SQLiteDatabase, usuario: Usuario) {
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USUARIO_ID, usuario.id)
            put(DatabaseHelper.COLUMN_USUARIO_NOMBRE, usuario.nombreUsuario)
            put(DatabaseHelper.COLUMN_USUARIO_CONTRASENIA, usuario.contrasenia)
        }

        db.insert(DatabaseHelper.TABLE_USUARIO, null, values)
    }

    private fun actualizarVehiculosEnDB(vehiculosDTO: List<VehiculoDTO>) {
        dbHelper.writableDatabase.use { db ->
            db.beginTransaction()
            try {
                // Agrupar vehículos por usuario para procesamiento eficiente
                val vehiculos = vehiculosDTO.map { it.toEntity() }
                val vehiculosPorUsuario = vehiculos.groupBy { it.usuarioId }

                vehiculosPorUsuario.forEach { (usuarioId, vehiculosUsuario) ->
                    actualizarVehiculosDeUsuario(db, usuarioId, vehiculosUsuario)
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun actualizarVehiculosDeUsuario(
        db: SQLiteDatabase,
        usuarioId: Int,
        vehiculos: List<Vehiculo>
    ) {
        // Eliminar vehículos antiguos del usuario
        db.delete(
            DatabaseHelper.TABLE_VEHICULO,
            "${DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID} = ?",
            arrayOf(usuarioId.toString())
        )

        // Insertar vehículos actualizados
        vehiculos.forEach { vehiculo ->
            val values = ContentValues().apply {
                put(DatabaseHelper.COLUMN_VEHICULO_ID, vehiculo.id)
                put(DatabaseHelper.COLUMN_VEHICULO_PLACA, vehiculo.placa)
                put(DatabaseHelper.COLUMN_VEHICULO_MARCA, vehiculo.marca)
                put(DatabaseHelper.COLUMN_VEHICULO_FECHA_FABRICACION,
                    vehiculo.fechaFabricacion?.let {
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                            it
                        )
                    })
                put(DatabaseHelper.COLUMN_VEHICULO_COLOR, vehiculo.color)
                put(DatabaseHelper.COLUMN_VEHICULO_PRECIO, vehiculo.precio)
                put(DatabaseHelper.COLUMN_VEHICULO_DISPONIBLE, if (vehiculo.disponible) 1 else 0)
                put(DatabaseHelper.COLUMN_VEHICULO_IMAGEN, vehiculo.imageResource)
                put(DatabaseHelper.COLUMN_VEHICULO_USUARIO_ID, vehiculo.usuarioId)
            }

            db.insert(DatabaseHelper.TABLE_VEHICULO, null, values)
        }
    }

    private fun <T> handleResponse(response: Response<T>, tipo: String) {
        if (!response.isSuccessful) {
            Log.e(TAG, "Error sincronizando $tipo al servidor: ${response.code()}")
            Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
        }
    }

}