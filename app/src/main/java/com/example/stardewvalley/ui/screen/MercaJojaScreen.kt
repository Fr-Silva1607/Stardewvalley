package com.example.stardewvalley.ui.screen

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stardewvalley.R

@Composable
fun MercaJojaScreen(navController: NavController, onFinalizarEleccion: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE) }
    val farmId = prefs.getInt("selected_farm_id", -1)

    // Estado global de dinero del jugador
    var miDineroText by remember { mutableStateOf(prefs.getString("mi_dinero_$farmId", "0") ?: "0") }
    val miDinero = miDineroText.toIntOrNull() ?: 0

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_mercajoja),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextoGorditoConBorde("Proyectos Joja", tamanio = 60f)

            // BARRA DE DINERO (SOLICITADA)
            Card(
                modifier = Modifier.padding(16.dp).fillMaxWidth(0.8f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3B8B).copy(alpha = 0.9f)),
                border = BorderStroke(2.dp, Color(0xFFFFD700))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painterResource(R.drawable.lingoteoro), null, modifier = Modifier.size(30.dp))
                    Spacer(Modifier.width(8.dp))
                    TextField(
                        value = miDineroText,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                miDineroText = it
                                prefs.edit().putString("mi_dinero_$farmId", it).apply()
                            }
                        },
                        label = { Text("Tu Dinero Actual", color = Color.White, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }

            val projects = listOf(
                JojaProjectData("Vagonetas", "Repara el sistema de vagonetas que circula entre la Parada de autobús, Las Montañas y Pueblo Pelícano.", 15000),
                JojaProjectData("Bateo mineral", "Elimina el peñasco brillante junto a la entrada de Las minas.", 20000),
                JojaProjectData("Puente", "Repara el puente roto de las montañas al este de Las minas, permitiendo el acceso a la Cantera.", 25000),
                JojaProjectData("Invernadero", "Repara las viejas ruinas de La granja para convertirlas en un Invernadero.", 35000),
                JojaProjectData("Autobús", "Repara el autobús que lleva al Desierto de Calico.", 40000)
            )

            LazyColumn(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(projects) { project ->
                    JojaItemRow(project, miDinero)
                }
            }

            // Botón Volver usando cartel_mercajoja
            Box(
                modifier = Modifier.padding(bottom = 20.dp).clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.cartel_mercajoja), null, modifier = Modifier.width(180.dp))
                Text("VOLVER", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun JojaItemRow(project: JojaProjectData, dineroActual: Int) {
    val falta = (project.price - dineroActual).coerceAtLeast(0)
    
    // Usamos el fondo de cartel para cada item si es posible, o una card azul estilo Joja
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3B8B).copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, Color(0xFF4CAF50))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(project.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                Text("${project.price}g", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold)
            }
            Text(project.description, color = Color.LightGray, fontSize = 11.sp, lineHeight = 14.sp)
            
            Spacer(Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = (dineroActual.toFloat() / project.price.toFloat()).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = if (falta == 0) Color(0xFF4CAF50) else Color(0xFFFFD700),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (falta > 0) "Faltan: ${falta}g" else "¡COMPLETADO!",
                    color = if (falta > 0) Color.White else Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

data class JojaProjectData(val name: String, val description: String, val price: Int)
