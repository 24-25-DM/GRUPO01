@file:Suppress("DEPRECATION", "SameParameterValue")

package com.example.myapplication.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapplication.repo.VehiculoRepository
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.Manifest
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.R
import com.example.myapplication.modelo.Vehiculo

class SecondActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var vRepo: VehiculoRepository
    private lateinit var listView: ListView
    private lateinit var adapter: VehicleAdapter
    private var userId: Int = -1
    private val vehicles = mutableListOf<Vehiculo>()

    @Suppress("PrivatePropertyName")
    private val CAMERA_PERMISSION_CODE = 100
    @Suppress("PrivatePropertyName")
    private val IMAGE_CAPTURE_CODE = 102
    private var imageUri: Uri? = null
    private var rutaImagen: String? = null
    private var currentVehiclePosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dos)


        // Inicializar componentes
        dbHelper = DatabaseHelper(this)
        vRepo = VehiculoRepository(dbHelper)
        listView = findViewById(R.id.listView)

        userId = intent.getIntExtra("USER_ID", -1)
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

    private fun requestCameraPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }


    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = crearImagen()

        photoFile?.let { file ->
            try {
                imageUri = FileProvider.getUriForFile(
                    this,
                    "com.example.myapplication.fileprovider", // coincidir con el authorities en el manifest
                    file
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivityForResult(intent, IMAGE_CAPTURE_CODE)
            } catch (e: Exception) {
                Log.e("CameraError", "Error al abrir la cámara: ${e.message}")
                Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun crearImagen(): File? {
        // Verificar permisos de almacenamiento
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                this,
                "Se requieren permisos de almacenamiento para guardar fotos",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val nombreArchivo = "JPEG_${timeStamp}_"
        val directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null

        return try {
            File.createTempFile(nombreArchivo, ".jpg", directorio).apply {
                rutaImagen = absolutePath
            }
        } catch (e: IOException) {
            Log.e("CamaraError", "Error al crear archivo", e)
            null
        }
    }

    @SuppressLint("ExifInterface")
    private fun loadImageWithCorrectOrientation(imagePath: String): android.graphics.Bitmap? {
        try {
            // Obtener las dimensiones del bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)

            // Calcular el tamaño de muestreo para no cargar imágenes demasiado grandes
            options.apply {
                inJustDecodeBounds = false
                inSampleSize = calculateInSampleSize(this, 800, 800) // Tamaño máximo deseado
            }

            // Decodificar el bitmap con el tamaño de muestreo
            val bitmap = BitmapFactory.decodeFile(imagePath, options) ?: return null

            // Obtener la orientación desde los metadatos EXIF
            val exif = android.media.ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                android.media.ExifInterface.TAG_ORIENTATION,
                android.media.ExifInterface.ORIENTATION_UNDEFINED
            )

            // Rotar el bitmap según la orientación
            return when (orientation) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            Log.e("ImageLoading", "Error al cargar/rotar la imagen: ${e.message}")
            return null
        }
    }

    // Función para calcular el tamaño de muestreo adecuado
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    // Función para rotar la imagen
    private fun rotateImage(
        source: android.graphics.Bitmap,
        angle: Float,
    ): android.graphics.Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(angle)
        return android.graphics.Bitmap.createBitmap(
            source, 0, 0, source.width, source.height, matrix, true
        )
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

    @SuppressLint("UseSwitchCompatOrMaterialCode", "SimpleDateFormat", "SetTextI18n")
    private fun showVehicleForm(vehicle: Vehiculo? = null) {
        try {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.activity_formulario_vehiculo)

            // Inicializar componentes del formulario
            val etPlaca = dialog.findViewById<EditText>(R.id.etPlaca)
            val etMarca = dialog.findViewById<EditText>(R.id.etMarca)
            val tvFechaFabricacion = dialog.findViewById<TextView>(R.id.etFechaFabricacion)
            val btnShowDatePicker = dialog.findViewById<Button>(R.id.btnShowDatePicker)
            val spinnerColor = dialog.findViewById<Spinner>(R.id.spinnerColor)
            val etCosto = dialog.findViewById<EditText>(R.id.etCosto)
            val cbActivo = dialog.findViewById<Switch>(R.id.cbActivo)
            val btnGuardar = dialog.findViewById<Button>(R.id.btnGuardarVehiculo)
            val imageViewFoto = dialog.findViewById<ImageView>(R.id.imagenVehiculo)
            val tvPlaceholder = dialog.findViewById<TextView>(R.id.tvPlaceholder)

            // Configurar el spinner de colores
            val colors = arrayOf("Blanco", "Negro", "Azul")
            val colorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colors)
            colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerColor.adapter = colorAdapter

            // Si es edición, llenar el formulario con los datos del vehículo
            vehicle?.let {
                etPlaca.setText(it.placa)
                etMarca.setText(it.marca)
                tvFechaFabricacion.text = it.fechaFabricacion?.let { it1 ->
                    SimpleDateFormat("dd/MM/yyyy").format(
                        it1
                    )
                }
                spinnerColor.setSelection(colors.indexOf(it.color))
                etCosto.setText(it.precio.toString())
                cbActivo.isChecked = it.disponible

                // Cargar la imagen si existe
                imageViewFoto?.let { imageView ->
                    try {
                        if (it.imageResource.startsWith("/")) {
                            // Es una ruta de archivo, usar el método con corrección de rotación
                            val bitmap = loadImageWithCorrectOrientation(it.imageResource)
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap)
                                tvPlaceholder?.visibility = View.GONE
                            } else {
                                imageView.setImageResource(R.drawable.ic_vehicle)
                                tvPlaceholder?.visibility = View.VISIBLE
                            }
                        } else {
                            // Es un ID de recurso o cadena inválida
                            try {
                                val resourceId =
                                    it.imageResource.toIntOrNull() ?: R.drawable.ic_vehicle
                                imageView.setImageResource(resourceId)
                                tvPlaceholder?.visibility =
                                    if (it.imageResource == R.drawable.ic_vehicle.toString())
                                        View.VISIBLE else View.GONE
                            } catch (e: Exception) {
                                imageView.setImageResource(R.drawable.ic_vehicle)
                                tvPlaceholder?.visibility = View.VISIBLE
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SecondActivity", "Error al cargar la imagen: ${e.message}")
                        imageView.setImageResource(R.drawable.ic_vehicle)
                        tvPlaceholder?.visibility = View.VISIBLE
                    }
                    imageView.visibility = View.VISIBLE
                }
            } ?: run {
                // Si es un nuevo vehículo, mostrar imagen por defecto
                imageViewFoto?.setImageResource(R.drawable.ic_vehicle)
                imageViewFoto?.visibility = View.VISIBLE
                tvPlaceholder?.visibility = View.VISIBLE
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
                // Añadir aquí la validación
                if (etPlaca.text.isNullOrEmpty() || etMarca.text.isNullOrEmpty() ||
                    tvFechaFabricacion.text.isNullOrEmpty() || etCosto.text.isNullOrEmpty()
                ) {
                    Toast.makeText(this, "Todos los campos son requeridos", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                // Si pasa la validación, continúa con la creación del vehículo
                val dateFormat = SimpleDateFormat("dd/MM/yyyy")
                val newVehicle = Vehiculo(
                    id = vehicle?.id ?: 0,
                    placa = etPlaca.text.toString(),
                    marca = etMarca.text.toString(),
                    fechaFabricacion = dateFormat.parse(tvFechaFabricacion.text.toString())!!,
                    color = spinnerColor.selectedItem.toString(),
                    precio = etCosto.text.toString().toDoubleOrNull() ?: 0.0,
                    disponible = cbActivo.isChecked,
                    imageResource = vehicle?.imageResource ?: R.drawable.ic_vehicle.toString(),
                    usuarioId = 0
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

            dialog.setOnDismissListener {
                try {
                    loadVehicles()
                } catch (e: Exception) {
                    Log.e("SecondActivity", "Error al cargar vehículos", e)
                }
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("SecondActivity", "Error al mostrar el formulario", e)
            Toast.makeText(this, "Error al abrir el formulario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation(vehicle: Vehiculo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Vehículo")
            .setMessage("¿Estás seguro de que deseas eliminar este vehículo?")
            .setPositiveButton("Sí") { _, _ ->
                vRepo.deleteVehicle(vehicle.id.toString(), userId)
                loadVehicles()
            }
            .setNegativeButton("No", null)
            .show()
    }

    inner class VehicleAdapter(
        context: Context,
        vehicles: List<Vehiculo>,
    ) : ArrayAdapter<Vehiculo>(context, 0, vehicles) {

        private var onEditClickListener: ((Vehiculo) -> Unit)? = null
        private var onDeleteClickListener: ((Vehiculo) -> Unit)? = null

        fun setOnEditClickListener(listener: (Vehiculo) -> Unit) {
            onEditClickListener = listener
        }

        fun setOnDeleteClickListener(listener: (Vehiculo) -> Unit) {
            onDeleteClickListener = listener
        }

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vehicle = getItem(position)!!
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_vehiculo, parent, false)

            // Configurar los elementos de la vista
            val imageView = view.findViewById<ImageView>(R.id.imagenVehiculo)
            try {
                if (vehicle.imageResource.startsWith("/")) {
                    // Es una ruta de archivo, usar el nuevo método para cargar con rotación corregida
                    val bitmap = loadImageWithCorrectOrientation(vehicle.imageResource)
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        // Es un recurso drawable
                        val resourceId =
                            vehicle.imageResource.toIntOrNull() ?: R.drawable.ic_vehicle
                        imageView.setImageResource(resourceId)
                    }
                } else {
                    // Es un recurso drawable
                    val resourceId =
                        vehicle.imageResource.toIntOrNull() ?: R.drawable.ic_vehicle
                    imageView.setImageResource(resourceId)
                }
            } catch (e: Exception) {
                // Si hay algún error, mostrar la imagen por defecto
                Log.e("VehicleAdapter", "Error al cargar imagen: ${e.message}")
                imageView.setImageResource(R.drawable.ic_vehicle)
            }

            // Configurar el botón de tomar foto
            view.findViewById<Button>(R.id.btnTomarFoto).setOnClickListener {
                currentVehiclePosition = position
                requestCameraPermissions()
            }

            view.findViewById<TextView>(R.id.placa).text = vehicle.placa
            view.findViewById<TextView>(R.id.marca).text = vehicle.marca
            view.findViewById<TextView>(R.id.fechaFabricacion).text =
                vehicle.fechaFabricacion?.let { SimpleDateFormat("dd/MM/yyyy").format(it) }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            try {
                val vehicle = vehicles[currentVehiclePosition]
                rutaImagen?.let { ruta ->
                    vehicle.imageResource = ruta
                    vRepo.updateVehicle(vehicle, userId)
                    loadVehicles()
                }
            } catch (e: Exception) {
                Log.e("CameraError", "Error al procesar la imagen: ${e.message}")
                Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }
}