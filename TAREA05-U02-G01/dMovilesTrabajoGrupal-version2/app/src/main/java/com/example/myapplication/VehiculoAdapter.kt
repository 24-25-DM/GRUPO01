package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Locale

class VehiculoAdapter(
    context: Context,
    private val vehiculos: MutableList<Vehiculo>,
    private val onEdit: (Vehiculo, Int) -> Unit, // Callback para editar
    private val onDelete: (Int) -> Unit, // Callback para eliminar
) : ArrayAdapter<Vehiculo>(context, 0, vehiculos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_vehiculo,
            parent,
            false
        )

        val vehiculo = vehiculos[position]

        // Establecer las características
        view.findViewById<TextView>(R.id.placa).text = vehiculo.placa
        view.findViewById<TextView>(R.id.marca).text = vehiculo.marca
        // Convertir la fecha de fabricación a texto
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        view.findViewById<TextView>(R.id.fechaFabricacion).text =
            dateFormat.format(vehiculo.fechaFabricacion)
        view.findViewById<TextView>(R.id.color).text = vehiculo.color
        view.findViewById<TextView>(R.id.costo).text = vehiculo.precio.toString()
        view.findViewById<TextView>(R.id.activo).text = if (vehiculo.disponible) "Sí" else "No"

        // Configurar imagen si está disponible
        vehiculo.imageResource?.let {
            view.findViewById<ImageView>(R.id.imagenVehiculo).setImageResource(it)
        }

        // Configurar botones
        view.findViewById<Button>(R.id.btnEditar).setOnClickListener {
            onEdit(vehiculo, position)
        }

        view.findViewById<Button>(R.id.btnEliminar).setOnClickListener {
            onDelete(position) // Llama al callback de eliminación
        }
        return view
    }
}