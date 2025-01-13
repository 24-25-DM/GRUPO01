package com.example.myapplication

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.IOException
import java.security.MessageDigest
import java.text.SimpleDateFormat

class DataStorage(private val context: Context) {
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd")
        .setPrettyPrinting()
        .create()

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
                Usuario("Luis", hashString("Valladares")),
                Usuario("Cristian", hashString("Olivares")),
                Usuario("Anthony", hashString("Tipan"))
            ),
            vehiculos = listOf(
                Vehiculo(
                    id = "1",
                    placa = "ABC-1234",
                    marca = "Toyota",
                    fechaFabricacion = SimpleDateFormat("dd/MM/yyyy").parse("01/01/2020")!!,
                    color = "Blanco",
                    precio = 20000.0,
                    disponible = true,
                    imageResource = R.drawable.ic_vehicle
                ),
                Vehiculo(
                    id = "2",
                    placa = "XYZ-7898",
                    marca = "Honda",
                    fechaFabricacion = SimpleDateFormat("dd/MM/yyyy").parse("15/08/2018")!!,
                    color = "Negro",
                    precio = 15000.0,
                    disponible = false,
                    imageResource = R.drawable.ic_vehicle
                ),
                Vehiculo(
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

    fun getVehiculos(): List<Vehiculo> {
        return getData().vehiculos
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
            nombreUsuario = username,
            contrasenia = hashString(password)
        )

        // Agregar el nuevo usuario y guardar
        val updatedUsers = data.usuarios + newUser
        saveData(data.copy(usuarios = updatedUsers))
        return true
    }

    fun addVehiculo(vehiculo: Vehiculo) {
        val data = getData()
        val updatedVehiculos = data.vehiculos + vehiculo
        saveData(data.copy(vehiculos = updatedVehiculos))
    }

    fun updateVehiculo(vehiculo: Vehiculo) {
        val data = getData()
        val updatedVehiculos = data.vehiculos.map {
            if (it.id == vehiculo.id) vehiculo else it
        }
        saveData(data.copy(vehiculos = updatedVehiculos))
    }

    fun deleteVehiculo(vehiculoId: String) {
        val data = getData()
        val updatedVehiculos = data.vehiculos.filter { it.id != vehiculoId }
        saveData(data.copy(vehiculos = updatedVehiculos))
    }

}