package com.example.stardewvalley.ui.screen

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.stardewvalley.R
import com.google.android.gms.maps3d.model.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource


@Composable
fun EleccionGranjaScreen(navController: NavController, onFinalizarEleccion: () -> Unit) {
    val context = LocalContext.current
    // Usamos remember para no estar pidiendo el SharedPreferences en cada recomposición
    val prefs = remember { context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE) }

    // Recuperamos el ID una sola vez fuera del AndroidView
    val farmId = prefs.getInt("selected_farm_id", -1)

    AndroidView(
        factory = { ctx ->
            val view = LayoutInflater.from(ctx).inflate(R.layout.screen_elecciones, null)

            // Lógica para Centro Cívico
            view.findViewById<FrameLayout>(R.id.btnCentroCivico).setOnClickListener {
                if (farmId != -1) {
                    prefs.edit().putString("ruta_$farmId", "centro_civico").apply()
                    onFinalizarEleccion() // <--- ¡Importante para navegar!
                }
            }

            // Lógica para MercaJoja
            view.findViewById<FrameLayout>(R.id.btnJoja).setOnClickListener {
                if (farmId != -1) {
                    prefs.edit().putString("ruta_$farmId", "joja").apply()
                    onFinalizarEleccion() // <--- Agregado aquí también
                }
            }

            view
        },
        modifier = Modifier.fillMaxSize()
    )
}