package com.example.myapplication

import android.annotation.SuppressLint
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

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var dataStorage: DataStorage

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataStorage = DataStorage(this)
        configurarPantallaPrincipal()
    }

    private fun configurarPantallaPrincipal() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val usuarioInput = findViewById<EditText>(R.id.usuario_input)
        val contraseniaInput = findViewById<EditText>(R.id.contrasenia_input)
        val botonLogin = findViewById<Button>(R.id.login_btn)
        val registrarse = findViewById<TextView>(R.id.registrarse)

        botonLogin.setOnClickListener {
            val usuario = usuarioInput.text.toString()
            val contrase = contraseniaInput.text.toString()

            if (usuario.isEmpty() || contrase.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (dataStorage.validateUser(usuario, contrase)) {
                Log.i("Login", "Login exitoso: Usuario: $usuario")
                val intent = Intent(this, SecondActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.i("Login", "Login fallido: Usuario o contraseña incorrectos")
                Toast.makeText(this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }

        registrarse.setOnClickListener {
            configurarPantallaRegistro()
        }
    }

    private fun configurarPantallaRegistro() {
        setContentView(R.layout.activity_registrarse)

        val botonRegistrarse = findViewById<Button>(R.id.registro_btn)
        val usuarioRegistro = findViewById<EditText>(R.id.usuario_registro)
        val contraseniaRegistro = findViewById<EditText>(R.id.contrasenia_registro)

        botonRegistrarse.setOnClickListener {
            val usuario = usuarioRegistro.text.toString()
            val contrase = contraseniaRegistro.text.toString()

            if (usuario.isEmpty() || contrase.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (dataStorage.registerUser(usuario, contrase)) {
                Log.i("Registro", "Usuario registrado: Usuario: $usuario")
                Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                setContentView(R.layout.activity_main)
                configurarPantallaPrincipal()
            } else {
                Log.i("Registro", "Usuario ya existe")
                Toast.makeText(this, "El usuario ya existe", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(R.layout.activity_main)
        configurarPantallaPrincipal()
    }
}
