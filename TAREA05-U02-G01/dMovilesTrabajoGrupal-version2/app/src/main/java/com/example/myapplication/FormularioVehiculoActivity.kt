package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import repo.VehiculoRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class FormularioVehiculoActivity : AppCompatActivity() {
    private var modoEdicion = false
    private var vehiculoOriginal: Vehiculo? = null
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formulario_vehiculo)

        inicializarVistas()
        configurarModoEdicion()
    }

    private fun inicializarVistas() {
        val etFechaFabricacion = findViewById<TextView>(R.id.etFechaFabricacion)
        val etColor = findViewById<Spinner>(R.id.spinnerColor)
        val btnShowDatePicker = findViewById<Button>(R.id.btnShowDatePicker)
        val btnGuardar = findViewById<Button>(R.id.btnGuardarVehiculo)
        //val btnEliminar = findViewById<Button>(R.id.btnEliminarVehiculo)

        configurarSpinnerColor(etColor)
        configurarSelectorFecha(btnShowDatePicker, etFechaFabricacion)
        configurarBotonGuardar(btnGuardar)
        //configurarBotonEliminar(btnEliminar)
    }

    private fun configurarModoEdicion() {
        modoEdicion = intent.getBooleanExtra("isEditMode", false)
        position = intent.getIntExtra("position", -1)
        vehiculoOriginal = intent.getSerializableExtra("vehiculo") as? Vehiculo
        //val btnEliminar = findViewById<Button>(R.id.btnEliminarVehiculo)

//        if (modoEdicion && vehiculoOriginal != null) {
//            cargarDatosVehiculo()// Mostrar el botón de eliminar
//            btnEliminar.visibility
//        } else {
//            btnEliminar.visibility
//        }
    }

    @SuppressLint("SetTextI18n")
    private fun cargarDatosVehiculo() {
        vehiculoOriginal?.let { vehiculo ->
            findViewById<EditText>(R.id.etPlaca).setText(vehiculo.placa)
            findViewById<EditText>(R.id.etMarca).setText(vehiculo.marca)
            findViewById<TextView>(R.id.etFechaFabricacion).text =
                SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(vehiculo.fechaFabricacion)

            // Establecer el color en el spinner
            val spinnerColor = findViewById<Spinner>(R.id.spinnerColor)
            val colorPosition = (spinnerColor.adapter as ArrayAdapter<String>)
                .getPosition(vehiculo.color)
            spinnerColor.setSelection(colorPosition)

            findViewById<EditText>(R.id.etCosto).setText(vehiculo.precio.toString())
            findViewById<Switch>(R.id.cbActivo).isChecked = vehiculo.disponible
        }
    }

    private fun configurarSpinnerColor(spinner: Spinner) {
        val colores = arrayOf("blanco", "negro", "azul")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colores)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun configurarSelectorFecha(btnShowDatePicker: Button, etFechaFabricacion: TextView) {
        btnShowDatePicker.setOnClickListener {
            val calendario = Calendar.getInstance()

            // Si estamos en modo edición, establecer la fecha actual del vehículo
            if (modoEdicion && vehiculoOriginal != null) {
                calendario.time = vehiculoOriginal!!.fechaFabricacion
            }

            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    if (year < 2000) {
                        Toast.makeText(this, "El año debe ser 2000 o posterior", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val fechaSeleccionada = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth,
                            month + 1,
                            year
                        )
                        etFechaFabricacion.text = fechaSeleccionada
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

    private fun validarDatosVehiculo(): Boolean {
        val etPlaca = findViewById<EditText>(R.id.etPlaca)
        val placa = etPlaca.text.toString().trim()
        val errores = mutableListOf<String>()

        // Validaciones de placa
        if (!validarPlaca(placa, errores)) {
            Toast.makeText(this, errores.joinToString("\n"), Toast.LENGTH_LONG).show()
            return false
        }

        // Validar costo
        val etCosto = findViewById<EditText>(R.id.etCosto)
        val costo = etCosto.text.toString().toDoubleOrNull()
        if (costo == null || costo <= 0) {
            Toast.makeText(this, "El costo debe ser un número positivo", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun validarPlaca(placa: String, errores: MutableList<String>): Boolean {
        val formatoValido = Regex("^[A-Z]{3}-\\d{4}$")
        if (!formatoValido.matches(placa)) {
            errores.add("El formato debe ser 'XXX-NNNN', donde X son letras y N son números.")
            return false
        }

        if (Regex("^[DFQÑ]").containsMatchIn(placa)) {
            errores.add("La placa no puede iniciar con las letras DFQÑ.")
            return false
        }

        // Validar consecutivos
        val letras = placa.substring(0, 3)
        val numeros = placa.substring(4, 8)
        if (tieneConsecutivos(letras) || tieneConsecutivos(numeros)) {
            errores.add("La placa no debe contener letras o números consecutivos en orden.")
            return false
        }

        if (placa.contains("O") && placa.contains("0")) {
            errores.add("La placa no puede incluir el número 0 si tiene la letra O.")
            return false
        }

        return true
    }

    private fun tieneConsecutivos(cadena: String): Boolean {
        for (i in 0 until cadena.length - 1) {
            if (cadena[i + 1].code - cadena[i].code == 1 ||
                cadena[i].code - cadena[i + 1].code == 1
            ) {
                return true
            }
        }
        return false
    }

    private fun configurarBotonGuardar(btnGuardar: Button) {
        btnGuardar.setOnClickListener {
            if (!validarDatosVehiculo()) {
                return@setOnClickListener
            }

            try {
                val vehiculoActualizado = crearVehiculoDesdeFormulario()
                val resultIntent = Intent().apply {
                    putExtra("vehiculo", vehiculoActualizado)
                    putExtra("position", position)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al guardar el vehículo: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun crearVehiculoDesdeFormulario(): Vehiculo {
        val etPlaca = findViewById<EditText>(R.id.etPlaca)
        val etMarca = findViewById<EditText>(R.id.etMarca)
        val etFechaFabricacion = findViewById<TextView>(R.id.etFechaFabricacion)
        val spinnerColor = findViewById<Spinner>(R.id.spinnerColor)
        val etCosto = findViewById<EditText>(R.id.etCosto)
        val switchActivo = findViewById<Switch>(R.id.cbActivo)

        val fechaFabricacion = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .parse(etFechaFabricacion.text.toString())
            ?: throw IllegalArgumentException("Fecha inválida")

        return if (modoEdicion) {
            vehiculoOriginal?.copy(
                placa = etPlaca.text.toString().trim(),
                marca = etMarca.text.toString().trim(),
                fechaFabricacion = fechaFabricacion,
                color = spinnerColor.selectedItem.toString(),
                precio = etCosto.text.toString().toDouble(),
                disponible = switchActivo.isChecked
            ) ?: throw IllegalStateException("Vehículo original no encontrado")
        } else {
            Vehiculo(
                id = UUID.randomUUID().toString(),
                placa = etPlaca.text.toString().trim(),
                marca = etMarca.text.toString().trim(),
                fechaFabricacion = fechaFabricacion,
                color = spinnerColor.selectedItem.toString(),
                precio = etCosto.text.toString().toDouble(),
                disponible = switchActivo.isChecked,
                imageResource = R.drawable.hol
            )
        }
    }

    private fun configurarBotonEliminar(btnEliminar: Button) {
        btnEliminar.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("position", position)
            }
            setResult(Activity.RESULT_FIRST_USER, resultIntent)
            finish()
        }
    }

}