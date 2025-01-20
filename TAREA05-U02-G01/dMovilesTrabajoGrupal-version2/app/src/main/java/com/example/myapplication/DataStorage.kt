package com.example.myapplication

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import repo.UsuarioRepository
import repo.VehiculoRepository
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat

class DataStorage(private val context: Context) {
    val dbHelper = DatabaseHelper(context)
    val usuarioRepo = UsuarioRepository(dbHelper)
    val vehiculoRepo = VehiculoRepository(dbHelper)

    private val gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd").setPrettyPrinting().create()

    private val filename = "predeterminated_data.json"

    init {
        // Inicializar datos predeterminados si no existen
        if (!dataFileExists()) {
            createDefaultData()
        }
    }

    private fun dataFileExists(): Boolean {
        return context.getFileStreamPath(filename).exists()
    }

    private fun createDefaultData() {
        val defaultData = PredeterminatedData(
            usuarios = listOf(
                Usuario(
                    "1", "Luis", "44a2ff61a610836592152ca0eb7e07847110586694212dbafda566d37363437d"
                ), Usuario(
                    "2",
                    "Cristian",
                    "e6c1af9b645640bb62e3ad9eaf0f8c7dde67ab9c7b20daf8535298336c11d5bd"
                ), Usuario(
                    "3",
                    "Anthony",
                    "c26447b5df3186ae36fb403c3b35c05d8484e593c822ac097251974a45d0ad54"
                )
            ), vehiculosPorUsuario = mapOf(
                "1" to listOf(
                    Vehiculo(
                        id = "1",
                        placa = "ABC-1234",
                        marca = "Toyota",
                        fechaFabricacion = SimpleDateFormat("dd/MM/yyyy").parse("01/01/2020")!!,
                        color = "Blanco",
                        precio = 20000.0,
                        disponible = true,
                        imageResource = R.drawable.ic_vehicle
                    ), Vehiculo(
                        id = "2",
                        placa = "XYZ-7898",
                        marca = "Honda",
                        fechaFabricacion = SimpleDateFormat("dd/MM/yyyy").parse("15/08/2018")!!,
                        color = "Negro",
                        precio = 15000.0,
                        disponible = false,
                        imageResource = R.drawable.ic_vehicle
                    ), Vehiculo(
                        id = "3",
                        placa = "LMN-4567",
                        marca = "Ford",
                        fechaFabricacion = SimpleDateFormat("dd/MM/yyyy").parse("10/12/2021")!!,
                        color = "Azul",
                        precio = 18000.0,
                        disponible = true,
                        imageResource = R.drawable.ic_vehicle
                    )
                )
            )
        )
        saveData(defaultData)
    }

    fun getData(): PredeterminatedData {
        return try {
            val jsonString = context.openFileInput(filename).bufferedReader().use { it.readText() }
            gson.fromJson(jsonString, PredeterminatedData::class.java)
        } catch (e: IOException) {
            Log.e("DataStorage", "Error reading data", e)
            createDefaultData()
            getData()
        }
    }

    fun saveData(data: PredeterminatedData) {
        try {
            val jsonString = gson.toJson(data)
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            Log.e("DataStorage", "Error saving data", e)
        }
    }

    fun validateUser(username: String, password: String): Boolean {
        val hashedPassword = hashString(password)
        return getData().usuarios.any {
            it.nombreUsuario == username && it.contrasenia == hashedPassword
        }
    }

    fun getVehiculosForUser(username: String): List<Vehiculo> {
        val data = getData()
        return data.vehiculosPorUsuario[username] ?: listOf()
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun registerUser(username: String, password: String): Boolean {
        val data = getData()

        // Verificar si el usuario ya existe
        if (data.usuarios.any { it.nombreUsuario == username }) {
            return false
        }

        // Crear nuevo usuario
        val newUser = Usuario(
            nombreUsuario = username, contrasenia = hashString(password)
        )

        // Agregar el nuevo usuario y guardar
        val updatedUsers = data.usuarios + newUser
        saveData(data.copy(usuarios = updatedUsers))

        val usuario = Usuario(nombreUsuario = "john_doe", contrasenia = "password123")
        usuarioRepo.insertUsuario(usuario)

        val vehiculo = Vehiculo(
            placa = "ABC123",
            marca = "Toyota",
            fechaFabricacion = SimpleDateFormat("dd/MM/yyyy").parse("10/12/2021")!!,
            color = "Rojo",
            precio = 20000.0,
            disponible = true,
            imageResource = R.drawable.ic_vehicle
        )
        vehiculoRepo.insertVehiculo(vehiculo, usuario.id)

        return true
    }

    fun addVehiculo(username: String, vehiculo: Vehiculo): Boolean {
        return try {
            val data = getData()
            val userVehiculos = data.vehiculosPorUsuario[username]?.toMutableList() ?: mutableListOf()
            userVehiculos.add(vehiculo)

            val updatedVehiculos = data.vehiculosPorUsuario.toMutableMap()
            updatedVehiculos[username] = userVehiculos

            saveData(data.copy(vehiculosPorUsuario = updatedVehiculos))
            true
        } catch (e: Exception) {
            Log.e("DataStorage", "Error adding vehicle", e)
            false
        }
    }

    fun updateVehiculo(username: String, vehiculo: Vehiculo) {
        val data = getData()
        val userVehiculos = data.vehiculosPorUsuario[username]?.map {
            if (it.id == vehiculo.id) vehiculo else it
        } ?: listOf()

        val updatedVehiculos = data.vehiculosPorUsuario.toMutableMap()
        updatedVehiculos[username] = userVehiculos

        saveData(data.copy(vehiculosPorUsuario = updatedVehiculos))
    }

    fun deleteVehiculo(username: String, vehiculoId: String) {
        val data = getData()
        val userVehiculos = data.vehiculosPorUsuario[username]?.filter {
            it.id != vehiculoId
        } ?: listOf()

        val updatedVehiculos = data.vehiculosPorUsuario.toMutableMap()
        updatedVehiculos[username] = userVehiculos

        saveData(data.copy(vehiculosPorUsuario = updatedVehiculos))
    }

    private fun validateVehiculo(vehiculo: Vehiculo): Boolean {
        return vehiculo.placa.isNotBlank() &&
                vehiculo.marca.isNotBlank() &&
                vehiculo.precio > 0
    }

}