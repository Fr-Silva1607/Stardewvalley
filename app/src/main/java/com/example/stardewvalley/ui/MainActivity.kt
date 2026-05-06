package com.example.stardewvalley.ui
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.stardewvalley.ui.screen.LoginScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // El código DEBE ir aquí adentro para que se ejecute al abrir la app
        val intent = Intent(this, LoginScreen::class.java)
        startActivity(intent)
        finish()
        // Cerramos MainActivity para que el usuario no regrese a una pantalla vacía

    }
}