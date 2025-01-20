package com.example.myapplication

data class PredeterminatedData(
    val usuarios: List<Usuario>,
    val vehiculosPorUsuario: Map<String, List<Vehiculo>>
)
