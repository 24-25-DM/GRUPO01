package com.example.myapplication.modelo

import java.io.Serializable
import java.util.Date

data class LogEntry(
    val id: Int,
    val action: String,
    val usuarioId: Int,
    val fecha: Date,
    val detalles: String?
) : Serializable