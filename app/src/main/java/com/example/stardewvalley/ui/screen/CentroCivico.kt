package com.example.stardewvalley.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.example.stardewvalley.util.StardewCalculator
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.example.stardewvalley.R
import androidx.compose.ui.unit.dp
import com.example.stardewvalley.navigation.Screen
import com.example.stardewvalley.ui.theme.* // Importa todos los colores




@Composable
fun CentroCivicoScreen(navController: NavHostController) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    // --- LÓGICA DE DATOS (Se mantiene igual) ---
    val horasDeVidaReal = try {
        val jsonString = loadJsonFromAssets(context, "StardewValletData.json")
        val config = Gson().fromJson(jsonString, Map::class.java)
        val segundos = (config["duracion_dia_segundos"] as? Double)?.toInt() ?: 860
        StardewCalculator.calcularTiempoRealAnio(segundos)
    } catch (e: Exception) {
        26.7 // Valor por defecto si hay error
    }

    // --- DISEÑO DE LA PANTALLA ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StardewSkyBlue) // Fondo celeste
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(

            ) {
                // 1. LA IMAGEN DE FONDO
                Image(
                    painter = painterResource(id = R.drawable.cartelcivico),
                    contentDescription = "Cartel",
                    modifier = Modifier
                        .size(
                            width = with(density) { 3500.toDp() }, // Convierte 800px a DP
                            height = with(density) { 1000.toDp() }
                        ),
                    contentScale = ContentScale.FillBounds
                )

                // 2. EL TEXTO (Encima de la imagen)
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 70.dp), // Margen interno para que no toque los bordes de madera
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ruta: Centro Cívico",
                        color = StardewWood,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Si eliges la ruta del Centro Cívico, el día 5 de primavera antes de las 10:00 AM " +
                                "debes pasar por la parte de arriba del  pueblo. Completar los lotes en un año te llevará aproximadamente " +
                                "${String.format("%.1f", horasDeVidaReal)} horas de tu vida real.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 50.dp),
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp)) // Espacio grande antes de los botones

            // --- BOTÓN CONTINUAR ---
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth(0.7f) // Botón ancho
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StardewGreen // Usamos tu color verde
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("CONTINUAR", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StardewRed
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CANCELAR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    // --- POP-UP ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    // 1. Cerramos el diálogo
                    showDialog = false

                    // 2. ACTIVAMOS LA BANDERA (Esto es lo que cambia el botón del Home)
                    Screen.yaVioTemporadas = true

                    // 3. NAVEGAMOS A TEMPORADAS
                    navController.navigate(Screen.Temporadas.route) {
                        // Borramos el cartel del historial para que no se pueda volver
                        popUpTo(Screen.CentroCivico.route) { inclusive = true }
                    }
                }) {
                    Text("Entendido")
                }
            },
            title = { Text("Aviso de Ruta") },
            text = { Text("Si eliges esta ruta, más adelante la puedes cambiar con Mercajoja, pero perderas todo el procreso.") }
        )
    }
}

fun loadJsonFromAssets(context: android.content.Context, fileName: String): String {
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}