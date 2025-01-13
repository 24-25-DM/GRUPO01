package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SecondActivity : AppCompatActivity() {
    private lateinit var dataStorage: DataStorage
    private lateinit var vehiculoAdapter: VehiculoAdapter
    private var vehiculos = mutableListOf<Vehiculo>()

    companion object {
        const val REQUEST_AGREGAR_VEHICULO = 1
        const val REQUEST_EDITAR_VEHICULO = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dos)

        inicializarStorage()
        configurarUI()

    }

    private fun inicializarStorage() {
        dataStorage = DataStorage(this)
        cargarVehiculos()
    }

    private fun cargarVehiculos() {
        vehiculos.clear()
        vehiculos.addAll(dataStorage.getVehiculos())
    }

    private fun configurarUI() {
        // Configurar el adapter
        vehiculoAdapter = VehiculoAdapter(
            this,
            vehiculos,
            onEdit = { vehiculo, position -> editarVehiculo(vehiculo, position) },
            onDelete = { position -> eliminarVehiculo(position) }
        )

        // Configurar ListView
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = vehiculoAdapter

        // Configurar botón para agregar vehículo
        val btnAgregarVehiculo = findViewById<Button>(R.id.btnAgregarVehiculo)
        btnAgregarVehiculo.setOnClickListener {
            val intent = Intent(this, FormularioVehiculoActivity::class.java)
            startActivityForResult(intent, REQUEST_AGREGAR_VEHICULO)
        }

        // Botón para cerrar sesión
        val btnCerrarSesion = findViewById<TextView>(R.id.cerrarSesionID)
        btnCerrarSesion.setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun editarVehiculo(vehiculo: Vehiculo, position: Int) {
        val intent = Intent(this, FormularioVehiculoActivity::class.java)
        intent.apply {
            putExtra("isEditMode", true)
            putExtra("vehiculo", vehiculo)
            putExtra("position", position)
        }
        startActivityForResult(intent, REQUEST_EDITAR_VEHICULO)
    }

    private fun eliminarVehiculo(position: Int) {
        if (position >= 0 && position < vehiculos.size) {
            val vehiculo = vehiculos[position]
            vehiculos.removeAt(position)
            dataStorage.deleteVehiculo(vehiculo.id)
            vehiculoAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Vehículo eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            resultCode == Activity.RESULT_OK && requestCode == REQUEST_AGREGAR_VEHICULO -> {
                val nuevoVehiculo = data?.getSerializableExtra("vehiculo") as? Vehiculo
                nuevoVehiculo?.let {
                    vehiculos.add(it)
                    dataStorage.addVehiculo(it)
                    vehiculoAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Vehículo agregado", Toast.LENGTH_SHORT).show()
                }
            }
            resultCode == Activity.RESULT_OK && requestCode == REQUEST_EDITAR_VEHICULO -> {
                val vehiculoEditado = data?.getSerializableExtra("vehiculo") as? Vehiculo
                val position = data?.getIntExtra("position", -1) ?: -1

                if (vehiculoEditado != null && position >= 0) {
                    vehiculos[position] = vehiculoEditado
                    dataStorage.updateVehiculo(vehiculoEditado)
                    vehiculoAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Vehículo actualizado", Toast.LENGTH_SHORT).show()
                }
            }
            resultCode == Activity.RESULT_FIRST_USER -> {
                val position = data?.getIntExtra("position", -1) ?: -1
                if (position >= 0 && position < vehiculos.size) {
                    eliminarVehiculo(position)
                }
            }
        }
    }
}