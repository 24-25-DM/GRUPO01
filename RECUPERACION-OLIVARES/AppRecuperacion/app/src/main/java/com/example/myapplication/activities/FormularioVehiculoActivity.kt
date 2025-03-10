@file:Suppress("DEPRECATION")

package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.modelo.Vehiculo
import java.text.SimpleDateFormat
import java.util.*

class FormularioVehiculoActivity : AppCompatActivity() {

    private var modoEdicion = false
    private var vehiculoOriginal: Vehiculo? = null
    private var position: Int = -1
    private lateinit var etPlaca: EditText
    private lateinit var etMarca: EditText
    private lateinit var etFechaFabricacion: TextView
    private lateinit var spinnerColor: Spinner
    private lateinit var etCosto: EditText
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var cbActivo: Switch
    private var userId: Int = -1

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_formulario_vehiculo)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userId = intent.getIntExtra("USER_ID", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        inicializarVistas()
        configurarModoEdicion()
        cargarDatosVehiculoSiExiste()
    }

    private fun inicializarVistas() {
        etPlaca = findViewById(R.id.etPlaca)
        etMarca = findViewById(R.id.etMarca)
        etFechaFabricacion = findViewById(R.id.etFechaFabricacion)
        spinnerColor = findViewById(R.id.spinnerColor)
        etCosto = findViewById(R.id.etCosto)
        cbActivo = findViewById(R.id.cbActivo)

        val btnShowDatePicker = findViewById<Button>(R.id.btnShowDatePicker)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarVehiculo)

        configurarSpinnerColor()
        configurarSelectorFecha(btnShowDatePicker)
        configurarBotonGuardar(btnGuardar)
    }

    private fun configurarModoEdicion() {
        modoEdicion = intent.getBooleanExtra("isEditMode", false)
        position = intent.getIntExtra("position", -1)
        vehiculoOriginal = intent.getSerializableExtra("vehiculo") as? Vehiculo
    }

    private fun configurarSpinnerColor() {
        val colores = arrayOf("Blanco", "Negro", "Azul")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colores)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerColor.adapter = adapter
    }

    private fun configurarSelectorFecha(btnShowDatePicker: Button) {
        btnShowDatePicker.setOnClickListener {
            val calendario = Calendar.getInstance()
            if (modoEdicion && vehiculoOriginal != null) {
                calendario.time = vehiculoOriginal!!.fechaFabricacion!!
            }
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    if (year < 2000) {
                        Toast.makeText(this, "El año debe ser 2000 o posterior", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        etFechaFabricacion.text = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                        )
                    }
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = Calendar.getInstance().apply {
                    set(2000, 0, 1)
                }.timeInMillis
                show()
            }
        }
    }

    private fun configurarBotonGuardar(btnGuardar: Button) {
        btnGuardar.setOnClickListener {
            if (!validarDatosVehiculo()) return@setOnClickListener

            try {
                val vehiculoActualizado = crearVehiculoDesdeFormulario()
                val resultIntent = Intent().apply {
                    putExtra("vehiculo", vehiculoActualizado)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error al guardar el vehículo: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun cargarDatosVehiculoSiExiste() {
        vehiculoOriginal?.let { vehiculo ->
            etPlaca.setText(vehiculo.placa)
            etMarca.setText(vehiculo.marca)
            etFechaFabricacion.text =
                vehiculo.fechaFabricacion?.let {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .format(it)
                }
            val colores = resources.getStringArray(R.array.colores)
            spinnerColor.setSelection(colores.indexOf(vehiculo.color))
            etCosto.setText(vehiculo.precio.toString())
            cbActivo.isChecked = vehiculo.disponible
        }
    }

    private fun validarDatosVehiculo(): Boolean {
        if (etPlaca.text.isNullOrEmpty()) {
            Toast.makeText(this, "La placa es requerida", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etMarca.text.isNullOrEmpty()) {
            Toast.makeText(this, "La marca es requerida", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etFechaFabricacion.text.isNullOrEmpty() || etFechaFabricacion.text == "Seleccione la fecha de fabricación: ") {
            Toast.makeText(this, "La fecha de fabricación es requerida", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etCosto.text.isNullOrEmpty()) {
            Toast.makeText(this, "El costo es requerido", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun crearVehiculoDesdeFormulario(): Vehiculo {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return if (modoEdicion) {
            vehiculoOriginal?.copy(
                placa = etPlaca.text.toString(),
                marca = etMarca.text.toString(),
                fechaFabricacion = dateFormat.parse(etFechaFabricacion.text.toString())!!,
                color = spinnerColor.selectedItem.toString(),
                precio = etCosto.text.toString().toDoubleOrNull() ?: 0.0,
                disponible = cbActivo.isChecked
            ) ?: throw IllegalStateException("Vehiculo original no encontrado")
        } else {
            Vehiculo(
                id = 0,
                placa = etPlaca.text.toString(),
                marca = etMarca.text.toString(),
                fechaFabricacion = dateFormat.parse(etFechaFabricacion.text.toString())!!,
                color = spinnerColor.selectedItem.toString(),
                precio = etCosto.text.toString().toDoubleOrNull() ?: 0.0,
                disponible = cbActivo.isChecked,
                imageResource = R.drawable.ic_vehicle.toString(),
                usuarioId = 0
            )
        }
    }
}