package com.example.stardewvalley.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.stardewvalley.R
import com.example.stardewvalley.ui.components.TextoGorditoConBorde
import com.example.stardewvalley.ui.theme.StardewTexto
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun MercaJojaScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("StardewPrefs", Context.MODE_PRIVATE) }
    val farmId = prefs.getInt("selected_farm_id", -1)

    // Estado global de dinero del jugador (Persistido por granja)
    var miDineroText by remember { mutableStateOf(prefs.getString("mi_dinero_$farmId", "0") ?: "0") }
    val miDinero = miDineroText.toIntOrNull() ?: 0

    // Para forzar la recomposición al completar un proyecto
    var refreshCount by remember { mutableStateOf(0) }

    val fondoJoja = remember { ContextCompat.getDrawable(context, R.drawable.fondo_mercajoja) }
    val lingoteOro = remember { ContextCompat.getDrawable(context, R.drawable.lingoteoro) }
    val cartelVolver = remember { ContextCompat.getDrawable(context, R.drawable.cartel_mercajoja) }

    // Añadimos background negro para evitar el flash blanco mientras carga la imagen
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        // Fondo de mosaico azul corporativo
        Image(
            painter = rememberDrawablePainter(fondoJoja),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Título: Proyectos Joja
            TextoGorditoConBorde(
                texto = "Proyectos Joja",
                tamanio = 45f,
                colorRelleno = Color.White,
                colorBorde = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta de Dinero Actual (Estilo Imagen)
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3B8B).copy(alpha = 0.9f)),
                border = BorderStroke(2.dp, Color(0xFFFFD700)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberDrawablePainter(lingoteOro),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Column {
                        Text(
                            text = "Tu Dinero Actual",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        TextField(
                            value = miDineroText,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    miDineroText = it
                                    prefs.edit().putString("mi_dinero_$farmId", it).apply()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.White,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val projects = listOf(
                JojaProjectData("Vagonetas", "Repara el sistema de vagonetas que circula entre la Parada de autobús, Las Montañas y Pueblo Pelícano.", 15000),
                JojaProjectData("Bateo mineral", "Elimina el peñasco brillante junto a la entrada de Las minas.", 20000),
                JojaProjectData("Puente", "Repara el puente roto de las montañas al este de Las minas, permitiendo el acceso a la Cantera.", 25000),
                JojaProjectData("Invernadero", "Repara las viejas ruinas de La granja para convertirlas en un Invernadero.", 35000),
                JojaProjectData("Autobús", "Repara el autobús que lleva al Desierto de Calico.", 40000)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(projects) { project ->
                    val isCompleted = remember(project.name, farmId, refreshCount) {
                        prefs.getBoolean("jo_completado_${project.name}_$farmId", false)
                    }

                    JojaProjectItem(
                        project = project,
                        dineroActual = miDinero,
                        isCompleted = isCompleted,
                        onComplete = {
                            if (miDinero >= project.price) {
                                val nuevo = miDinero - project.price
                                miDineroText = nuevo.toString()
                                prefs.edit()
                                    .putString("mi_dinero_$farmId", nuevo.toString())
                                    .putBoolean("jo_completado_${project.name}_$farmId", true)
                                    .apply()
                                refreshCount++
                            }
                        }
                    )
                }
            }

            // Botón VOLVER (Vuelve a LoginScreen Activity)
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(80.dp)
                    .clickable { 
                        val intent = Intent(context, LoginScreen::class.java)
                        // Limpiamos la pila para volver al inicio
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberDrawablePainter(cartelVolver),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Text(
                    text = "VOLVER",
                    color = StardewTexto,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun JojaProjectItem(
    project: JojaProjectData,
    dineroActual: Int,
    isCompleted: Boolean,
    onComplete: () -> Unit
) {
    val falta = (project.price - dineroActual).coerceAtLeast(0)
    val puedePagar = dineroActual >= project.price
    val progreso = (dineroActual.toFloat() / project.price.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3B8B).copy(alpha = 0.85f)),
        border = BorderStroke(2.dp, Color(0xFF3F51B5)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${project.price}g",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = project.description,
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barra de progreso y Estado interactivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(14.dp)
                        .background(Color.DarkGray, RoundedCornerShape(7.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (isCompleted) 1f else progreso)
                            .fillMaxHeight()
                            .background(
                                if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFFD700),
                                RoundedCornerShape(7.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isCompleted) {
                    Text(
                        text = "¡COMPLETADO!",
                        color = Color(0xFF4CAF50),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (puedePagar) {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("COMPLETAR", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Faltan: ${falta}g",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class JojaProjectData(val name: String, val description: String, val price: Int)
