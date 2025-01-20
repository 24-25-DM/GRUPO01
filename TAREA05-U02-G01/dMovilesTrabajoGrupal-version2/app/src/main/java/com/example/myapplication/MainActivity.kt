package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import repo.UsuarioRepository

class MainActivity : AppCompatActivity() {
    private lateinit var uRepo: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uRepo = UsuarioRepository(DatabaseHelper(this))
        configuracionPantallaPrincipal()
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

    override fun onBackPressed() {
        // Si estamos en la pantalla de registro, volver al login
        if (findViewById<EditText>(R.id.usuario_registro) != null) {
            showLoginScreen()
        } else {
            super.onBackPressed()
        }
    }
}
