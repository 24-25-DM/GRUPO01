package com.example.myapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import repo.VehiculoRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID

class SecondActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var vRepo: VehiculoRepository
    private lateinit var listView: ListView
    private lateinit var adapter: VehicleAdapter
    private lateinit var userId: String
    private val vehicles = mutableListOf<Vehiculo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dos)

        // Inicializar componentes
        dbHelper = DatabaseHelper(this)
        vRepo = VehiculoRepository(dbHelper)
        listView = findViewById(R.id.listView)

        userId = intent.getStringExtra("USER_ID") ?: ""
        Log.d("SecondActivity", "USER_ID recibido: $userId")

        // Configurar el botón de cerrar sesión
        findViewById<TextView>(R.id.cerrarSesionID).setOnClickListener {
            finish()
        }

        // Configurar el botón de agregar vehículo
        findViewById<Button>(R.id.btnAgregarVehiculo).setOnClickListener {
            showVehicleForm()
        }

        // Inicializar y configurar el adaptador
        setupListView()

        // Cargar vehículos
        loadVehicles()

        // Botón para cerrar sesión
        val btnCerrarSesion = findViewById<TextView>(R.id.cerrarSesionID)
        btnCerrarSesion.setOnClickListener {
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

    }

    private fun setupListView() {
        adapter = VehicleAdapter(this, vehicles)
        listView.adapter = adapter

        // Configurar los clicks en los items
        adapter.setOnEditClickListener { vehicle ->
            showVehicleForm(vehicle)
        }

        adapter.setOnDeleteClickListener { vehicle ->
            showDeleteConfirmation(vehicle)
        }
    }

    private fun loadVehicles() {
        vehicles.clear()
        val userVehicles = vRepo.getVehiculosByUsuarioId(userId)
        Log.d("SecondActivity", "Vehículos cargados: ${userVehicles.size}")
        vehicles.addAll(userVehicles)
        adapter.notifyDataSetChanged()
    }

    private fun showVehicleForm(vehicle: Vehiculo? = null) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.activity_formulario_vehiculo)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Inicializar componentes del formulario
        val etPlaca = dialog.findViewById<EditText>(R.id.etPlaca)
        val etMarca = dialog.findViewById<EditText>(R.id.etMarca)
        val tvFechaFabricacion = dialog.findViewById<TextView>(R.id.etFechaFabricacion)
        val btnShowDatePicker = dialog.findViewById<Button>(R.id.btnShowDatePicker)
        val spinnerColor = dialog.findViewById<Spinner>(R.id.spinnerColor)
        val etCosto = dialog.findViewById<EditText>(R.id.etCosto)
        val cbActivo = dialog.findViewById<Switch>(R.id.cbActivo)
        val btnGuardar = dialog.findViewById<Button>(R.id.btnGuardarVehiculo)
        val btnEliminar = dialog.findViewById<Button>(R.id.btnEliminarVehiculo)

        // Configurar el spinner de colores
        val colors = arrayOf("Blanco", "Negro", "Azul")
        val colorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colors)
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColor.adapter = colorAdapter

        // Si es edición, llenar el formulario con los datos del vehículo
        vehicle?.let {
            etPlaca.setText(it.placa)
            etMarca.setText(it.marca)
            tvFechaFabricacion.text = SimpleDateFormat("dd/MM/yyyy").format(it.fechaFabricacion)
            spinnerColor.setSelection(colors.indexOf(it.color))
            etCosto.setText(it.precio.toString())
            cbActivo.isChecked = it.disponible
            //btnEliminar.visibility = View.GONE
        }

        // Configurar el selector de fecha
        btnShowDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = "$day/${month + 1}/$year"
                    tvFechaFabricacion.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Configurar el botón de guardar
        btnGuardar.setOnClickListener {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            val newVehicle = Vehiculo(
                id = vehicle?.id ?: UUID.randomUUID().toString(),
                placa = etPlaca.text.toString(),
                marca = etMarca.text.toString(),
                fechaFabricacion = dateFormat.parse(tvFechaFabricacion.text.toString())!!,
                color = spinnerColor.selectedItem.toString(),
                precio = etCosto.text.toString().toDoubleOrNull() ?: 0.0,
                disponible = cbActivo.isChecked,
                imageResource = R.drawable.ic_vehicle
            )

            if (vehicle == null) {
                // Agregar nuevo vehículo
                vRepo.insertVehiculo(newVehicle, userId)
            } else {
                // Actualizar vehículo existente
                vRepo.updateVehicle(newVehicle, userId)
            }

            loadVehicles()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(vehicle: Vehiculo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Vehículo")
            .setMessage("¿Estás seguro de que deseas eliminar este vehículo?")
            .setPositiveButton("Sí") { _, _ ->
                vRepo.deleteVehicle(vehicle.id, userId)
                loadVehicles()
            }
            .setNegativeButton("No", null)
            .show()
    }

    inner class VehicleAdapter(
        context: Context,
        private val vehicles: List<Vehiculo>
    ) : ArrayAdapter<Vehiculo>(context, 0, vehicles) {

        private var onEditClickListener: ((Vehiculo) -> Unit)? = null
        private var onDeleteClickListener: ((Vehiculo) -> Unit)? = null

        fun setOnEditClickListener(listener: (Vehiculo) -> Unit) {
            onEditClickListener = listener
        }

        fun setOnDeleteClickListener(listener: (Vehiculo) -> Unit) {
            onDeleteClickListener = listener
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vehicle = getItem(position)!!
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_vehiculo, parent, false)

            // Configurar los elementos de la vista
            view.findViewById<ImageView>(R.id.imagenVehiculo).setImageResource(vehicle.imageResource)
            view.findViewById<TextView>(R.id.placa).text = vehicle.placa
            view.findViewById<TextView>(R.id.marca).text = vehicle.marca
            view.findViewById<TextView>(R.id.fechaFabricacion).text =
                SimpleDateFormat("dd/MM/yyyy").format(vehicle.fechaFabricacion)
            view.findViewById<TextView>(R.id.color).text = vehicle.color
            view.findViewById<TextView>(R.id.costo).text = "$.${vehicle.precio}"
            view.findViewById<TextView>(R.id.activo).text =
                if (vehicle.disponible) "Disponible" else "No disponible"

            // Configurar botones
            view.findViewById<Button>(R.id.btnEditar).setOnClickListener {
                onEditClickListener?.invoke(vehicle)
            }

            view.findViewById<Button>(R.id.btnEliminar).setOnClickListener {
                onDeleteClickListener?.invoke(vehicle)
            }

            return view
        }
    }
}