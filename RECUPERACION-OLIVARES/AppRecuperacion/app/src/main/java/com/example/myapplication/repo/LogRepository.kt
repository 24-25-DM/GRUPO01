package com.example.myapplication.repo

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.modelo.LogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogRepository(private val dbHelper: DatabaseHelper) {

    companion object {
        const val ACTION_LOGIN = "INGRESO"
        const val ACTION_CREATE = "CREACION"
        const val ACTION_UPDATE = "ACTUALIZACION"
        const val ACTION_DELETE = "ELIMINACION"
    }

    fun addLogEntry(action: String, usuarioId: Int, detalles: String? = null): Long {
        val db = dbHelper.writableDatabase
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_LOG_ACTION, action)
            put(DatabaseHelper.COLUMN_LOG_USUARIO_ID, usuarioId)
            put(DatabaseHelper.COLUMN_LOG_FECHA, currentDate)
            put(DatabaseHelper.COLUMN_LOG_DETALLES, detalles)
        }

        return db.insert(DatabaseHelper.TABLE_LOG, null, values)
    }

    private fun cursorToLogEntry(cursor: Cursor): LogEntry? {
        return try {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_ID))
            val action = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_ACTION))
            val usuarioId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_USUARIO_ID))
            val fechaStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_FECHA))
            val detalles = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_DETALLES))

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fecha = dateFormat.parse(fechaStr) ?: Date()

            LogEntry(id, action, usuarioId, fecha, detalles)
        } catch (e: Exception) {
            Log.e("LogRepository", "Error al convertir cursor a log: ${e.message}")
            null
        }
    }

    fun getAllLogs(): List<LogEntry> {
        val logs = mutableListOf<LogEntry>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_LOG,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_LOG_FECHA} DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val log = cursorToLogEntry(it)
                log?.let { entry -> logs.add(entry) }
            }
        }

        return logs
    }

    fun getLogsByUsuarioId(usuarioId: Int): List<LogEntry> {
        val logs = mutableListOf<LogEntry>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseHelper.TABLE_LOG,
            null,
            "${DatabaseHelper.COLUMN_LOG_USUARIO_ID} = ?",
            arrayOf(usuarioId.toString()),
            null,
            null,
            "${DatabaseHelper.COLUMN_LOG_FECHA} DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                val log = cursorToLogEntry(it)
                log?.let { entry -> logs.add(entry) }
            }
        }

        return logs
    }

}