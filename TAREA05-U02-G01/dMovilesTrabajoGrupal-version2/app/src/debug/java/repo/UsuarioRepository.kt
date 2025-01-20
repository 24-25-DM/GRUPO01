package repo

import android.content.ContentValues
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.Usuario
import java.security.MessageDigest

class UsuarioRepository(private val dbHelper: DatabaseHelper) {

    fun insertUsuario(usuario: Usuario): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USUARIO_ID, usuario.id)
            put(DatabaseHelper.COLUMN_USUARIO_NOMBRE, usuario.nombreUsuario)
            put(DatabaseHelper.COLUMN_USUARIO_CONTRASENIA, usuario.contrasenia)
        }
        return db.insert(DatabaseHelper.TABLE_USUARIO, null, values)
    }

    fun getUsuarioById(usuarioId: String): Usuario? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIO,
            null,
            "${DatabaseHelper.COLUMN_USUARIO_ID} = ?",
            arrayOf(usuarioId),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val usuario = Usuario(
                id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_ID)),
                nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_NOMBRE)),
                contrasenia = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_CONTRASENIA))
            )
            cursor.close()
            usuario
        } else {
            cursor.close()
            null
        }
    }

    fun validateUser(username: String, password: String): Usuario? {
        val db = dbHelper.readableDatabase
        val hashedPassword = hashPassword(password)

        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIO,
            null,
            "${DatabaseHelper.COLUMN_USUARIO_NOMBRE} = ? AND ${DatabaseHelper.COLUMN_USUARIO_CONTRASENIA} = ?",
            arrayOf(username, hashedPassword),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Usuario(
                    id = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_ID)),
                    nombreUsuario = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_NOMBRE)),
                    contrasenia = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USUARIO_CONTRASENIA))
                )
            } else null
        }
    }

    fun userExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIO,
            null,
            "${DatabaseHelper.COLUMN_USUARIO_NOMBRE} = ?",
            arrayOf(username),
            null,
            null,
            null
        )

        return cursor.use { it.count > 0 }
    }

    fun addUser(nombreUsuario: String, contrasenia: String): Boolean {
        val db = dbHelper.writableDatabase

        // Verificar si el usuario ya existe
        if (userExists(nombreUsuario)) {
            return false
        }

        // Obtener el siguiente ID disponible
        val nextId = dbHelper.getNextUserId().toString()

        // Crear el nuevo usuario con el ID generado
        val newUser = Usuario(
            id = nextId,
            nombreUsuario = nombreUsuario,
            contrasenia = hashPassword(contrasenia)
        )

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USUARIO_ID, newUser.id)
            put(DatabaseHelper.COLUMN_USUARIO_NOMBRE, newUser.nombreUsuario)
            put(DatabaseHelper.COLUMN_USUARIO_CONTRASENIA, newUser.contrasenia)
        }

        val result = db.insert(DatabaseHelper.TABLE_USUARIO, null, values)

        if (result != -1L) {
            // Si el usuario se creó exitosamente, agregar los vehículos por defecto
            dbHelper.insertarVehiculosPorDefecto(db, newUser.id)
            return true
        }
        return false
    }

    fun hashPassword(password: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

}
