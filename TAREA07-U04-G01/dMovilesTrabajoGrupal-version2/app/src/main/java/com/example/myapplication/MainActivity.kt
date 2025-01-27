package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import repo.UsuarioRepository
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var uRepo: UsuarioRepository

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uRepo = UsuarioRepository(DatabaseHelper(this))
        configuracionPantallaPrincipal()

        // Solicitar permisos al inicio
        requestRequiredPermissions()
    }

    private fun requestRequiredPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun configuracionPantallaPrincipal() {
        // Configurar insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        val usernameInput = findViewById<EditText>(R.id.usuario_input)
        val passwordInput = findViewById<EditText>(R.id.contrasenia_input)
        val loginButton = findViewById<Button>(R.id.login_btn)
        val registerLink = findViewById<TextView>(R.id.registrarse)

        // Configurar botón de login
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            // Validar campos vacíos
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Intentar login
            val user = uRepo.validateUser(username, password)

            if (user != null) {
                Log.d("MainActivity", "Login exitoso para usuario: ${user.nombreUsuario}")
                Toast.makeText(this, "Bienvenido ${user.nombreUsuario}!", Toast.LENGTH_SHORT).show()
                // Iniciar SecondActivity
                val intent = Intent(this, SecondActivity::class.java)
                intent.putExtra("USER_ID", user.id)
                startActivity(intent)
                finish()
            } else {
                Log.d("MainActivity", "Login fallido para usuario: $username")
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar link de registro
        registerLink.setOnClickListener {
            configurarPantallaRegistro()
        }
    }

    private fun configurarPantallaRegistro() {
        setContentView(R.layout.activity_registrarse)

        // Inicializar vistas de registro
        val usernameInput = findViewById<EditText>(R.id.usuario_registro)
        val passwordInput = findViewById<EditText>(R.id.contrasenia_registro)
        val registerButton = findViewById<Button>(R.id.registro_btn)
        val loginLink = findViewById<TextView>(R.id.volver_login)

        // Configurar botón de registro
        registerButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Validar campos vacíos
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar si el usuario ya existe
            if (uRepo.userExists(username)) {
                Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (uRepo.addUser(username, password)) {
                Log.d("MainActivity", "Usuario registrado exitosamente: $username")
                Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                showLoginScreen()
            } else {
                Log.d("MainActivity", "Error al registrar usuario: $username")
                Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
            }
        }

        // Configurar link para volver al login
        loginLink.setOnClickListener {
            showLoginScreen()
        }
    }

    private fun showLoginScreen() {
        setContentView(R.layout.activity_main)
        configuracionPantallaPrincipal()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Si estamos en la pantalla de registro, volver al login
        if (findViewById<EditText>(R.id.usuario_registro) != null) {
            showLoginScreen()
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                } else {
                    // Si los permisos son necesarios para el funcionamiento de la app
                    // puedes mostrar un diálogo explicativo
                    AlertDialog.Builder(this)
                        .setTitle("Permisos necesarios")
                        .setMessage("Esta aplicación necesita permisos de cámara y almacenamiento para funcionar correctamente. Por favor, concede los permisos en la configuración de la aplicación.")
                        .setPositiveButton("Ir a Configuración") { _, _ ->
                            // Abrir configuración de la app
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        }
    }
}
